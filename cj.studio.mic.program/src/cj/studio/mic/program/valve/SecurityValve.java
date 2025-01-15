package cj.studio.mic.program.valve;

import cj.netos.uc.port.IAppManangerSelfServicePorts;
import cj.netos.uc.util.Encript;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.Scope;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.http.CookieHelper;
import cj.studio.ecm.net.http.HttpCircuit;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.ecm.net.session.ISession;
import cj.studio.ecm.util.ObjectHelper;
import cj.studio.gateway.socket.pipeline.*;
import cj.studio.openport.client.IRequestAdapter;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;
import cj.ultimate.util.StringUtil;
import io.netty.handler.codec.http.Cookie;

import java.util.*;

@CjService(name = "securityValve", scope = Scope.multiton)
public class SecurityValve implements IAnnotationInputValve {
    @CjServiceRef(refByName = "$openports.cj.studio.openport.client.IRequestAdapter")//IRequestAdapter是请求适配器
    IRequestAdapter requestAdapter;
    @CjServiceRef(refByName = "$.output.selector")
    IOutputSelector selector;
    @CjServiceRef(refByName = "$openports.cj.studio.openport.client.IRequestAdapter")
    IRequestAdapter adapter;
    @CjServiceRef(refByName = "$openports.cj.netos.uc.port.IAppManangerSelfServicePorts")
    IAppManangerSelfServicePorts appManangerSelfServicePorts;
    @CjServiceSite
    IServiceSite site;
    @Override
    public void onActive(String inputName, IIPipeline pipeline) throws CircuitException {
        pipeline.nextOnActive(inputName, this);
    }

    @Override
    public void flow(Object request, Object response, IIPipeline pipeline) throws CircuitException {
        if (request instanceof HttpFrame) {
            HttpFrame frame = (HttpFrame) request;
            if (frame.relativePath().startsWith("/public")) {
                pipeline.nextFlow(request, response, this);
                return;
            }
            ISession session = frame.session();
            if (session == null) {// 放过资源
                pipeline.nextFlow(request, response, this);
                return;
            }
            String principals = (String) session.attribute("uc.principals");
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) session.attribute("uc.roles");
            boolean hasTestRole = false;
            if (roles != null) {
                for (String r : roles) {
                    if ("platform:mic:members".equals(r)) {
                        hasTestRole = true;
                        break;
                    }
                }
            }
            if (StringUtil.isEmpty(principals) || !hasTestRole) {
                HttpCircuit c = (HttpCircuit) response;
                c.status("302");
                c.message("redirect url.");
                c.head("Location", "./public/login.html");
                return;
            }
            String cjtoken = (String) session.attribute("uc.token");
            Set<Cookie> cookies = frame.cookie("cjtoken");
            if (cookies.isEmpty() && !StringUtil.isEmpty(cjtoken)) {
                CookieHelper.appendCookie((Circuit) response, "cjtoken", cjtoken);
            }
            if (!cookies.isEmpty() && !StringUtil.isEmpty(cjtoken)) {
                Iterator<Cookie> iterator = cookies.iterator();
                String c = "";
                if (iterator.hasNext()) {
                    Cookie cookie = iterator.next();
                    try {
                        c = (String) ObjectHelper.get(cookie, "value");
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (!cjtoken.equals(c)) {
                    CookieHelper.appendCookie((Circuit) response, "cjtoken", cjtoken);
                }
            }
            pipeline.nextFlow(request, response, this);
            return;
        }
        //下面是ws协议，用来处理来自mic客户端的命令
        Frame frame = (Frame) request;
        Circuit circuit = (Circuit) response;
        if (frame.relativePath().startsWith("/public")) {
            pipeline.nextFlow(request, response, this);
            return;
        }
        String cjtoken = frame.parameter("cjtoken");
        if (StringUtil.isEmpty(cjtoken)) {
            IOutputer out = selector.select(frame);
            out.closePipeline();
            throw new CircuitException("801", "缺少令牌，访问被拒绝");
        }
        //由于此处仅拦截ws的请求，因此只需要处理客户端网关向mic的注册即可，如果注册不成功则关闭连接，如果
        //成功就放行，不必再验证其它ws请求，因此ws连接已授信。
        if("register".equalsIgnoreCase(frame.command())&&frame.url().startsWith("/mic/node.service")) {
            doMicRegisterCommand(frame, circuit, pipeline);
        }else{
            doOtherWsRequest(frame, circuit, pipeline);
        }
    }
    private void doOtherWsRequest(Frame frame, Circuit circuit, IIPipeline pipeline) throws CircuitException {
        String cjtoken = frame.parameter("cjtoken");
        String appId = site.getProperty("app-id");
        String appKey = site.getProperty("app-key");
        String appSecret = site.getProperty("app-secret");
        String nonce = Encript.md5(UUID.randomUUID().toString());
        String sign = Encript.md5(String.format("%s%s%s", appKey, nonce, appSecret));
        String retvalue = adapter.request("get", "http/1.1", new HashMap<String, String>() {
            {
                put("Rest-Command", "verification");
                put("app-id", appId);
                put("app-key", appKey);
                put("app-nonce", nonce);
                put("app-sign", sign);
            }
        }, new HashMap<String, String>() {
            {
                put("token", cjtoken);
            }
        }, null);

        Map<String, Object> resp = new Gson().fromJson(retvalue, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        if (200.0 != (double) resp.get("status")) {
            throw new CircuitException(resp.get("status") + "", "uc响应错误：" + resp.get("message"));
        }

        String dataText = (String) resp.get("dataText");
        Map<String, Object> entry = new Gson().fromJson(dataText, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        String person = (String) entry.get("person");//得到统一用户
        circuit.attribute("uc.principals", person);
        pipeline.nextFlow(frame, circuit, this);

    }

    private void doMicRegisterCommand(Frame frame, Circuit circuit, IIPipeline pipeline) throws CircuitException {
        String cjtoken = frame.parameter("cjtoken");
        String appId = frame.parameter("appId");
        String appKey = frame.parameter("appKey");
        String nonce = frame.parameter("nonce");
        String sign = frame.parameter("sign");
        String retvalue = adapter.request("get", "http/1.1", new HashMap<String, String>() {
            {
                put("Rest-Command", "verification");
                put("app-id", appId);
                put("app-key", appKey);
                put("app-nonce", nonce);
                put("app-sign", sign);
            }
        }, new HashMap<String, String>() {
            {
                put("token", cjtoken);
            }
        }, null);

        Map<String, Object> resp = new Gson().fromJson(retvalue, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        if (200.0 != (double) resp.get("status")) {
            throw new CircuitException(resp.get("status") + "", "uc响应错误：" + resp.get("message"));
        }

        String dataText = (String) resp.get("dataText");
        Map<String, Object> entry = new Gson().fromJson(dataText, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        String person = (String) entry.get("person");//得到统一用户
        circuit.attribute("uc.principals", person);
        pipeline.nextFlow(frame, circuit, this);
    }

    @Override
    public void onInactive(String inputName, IIPipeline pipeline) throws CircuitException {
        pipeline.nextOnInactive(inputName, this);
    }

    @Override
    public int getSort() {
        return 2;
    }

}
