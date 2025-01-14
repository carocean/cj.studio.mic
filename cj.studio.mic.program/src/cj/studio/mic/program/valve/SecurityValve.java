package cj.studio.mic.program.valve;

import cj.netos.uc.model.UcRole;
import cj.netos.uc.port.IAuthPort;
import cj.netos.uc.port.ITenantManangerSelfServicePorts;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.Scope;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.http.CookieHelper;
import cj.studio.ecm.net.http.HttpCircuit;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.ecm.net.session.ISession;
import cj.studio.ecm.util.ObjectHelper;
import cj.studio.gateway.socket.pipeline.IAnnotationInputValve;
import cj.studio.gateway.socket.pipeline.IIPipeline;
import cj.studio.gateway.socket.pipeline.IOutputSelector;
import cj.studio.gateway.socket.pipeline.IOutputer;
import cj.studio.openport.client.IRequestAdapter;
import cj.studio.openport.client.Openports;
import cj.ultimate.util.StringUtil;
import io.netty.handler.codec.http.Cookie;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CjService(name = "securityValve", scope = Scope.multiton)
public class SecurityValve implements IAnnotationInputValve {
    @CjServiceRef(refByName = "$openports.cj.studio.openport.client.IRequestAdapter")//IRequestAdapter是请求适配器
    IRequestAdapter requestAdapter;
    @CjServiceRef(refByName = "$.output.selector")
    IOutputSelector selector;

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
                    if ("platform:administrators".equals(r)) {
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
                       c= (String) ObjectHelper.get(cookie,"value");
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
        Frame frame = (Frame) request;
        Circuit circuit = (Circuit) response;
        if (frame.relativePath().startsWith("/public")) {
            pipeline.nextFlow(request, response, this);
            return;
        }
        String cjtoken = frame.parameter("cjtoken");
        if (StringUtil.isEmpty(cjtoken)) {
            throw new CircuitException("801", "缺少令牌，访问被拒绝");
        }
        IAuthPort iucPort = Openports.open(IAuthPort.class, "ports://openport.com/openport/uc.ports", "xx");

        Map<String, Object> map = iucPort.verification(null, cjtoken);
        String user = (String) map.get("user");
        String tenant = (String) map.get("tenant");
        ITenantManangerSelfServicePorts tenantStub = Openports.open(ITenantManangerSelfServicePorts.class, "ports://openport.com/openport/uc.ports", cjtoken);
        if (tenantStub.getTenant(null, tenant) == null) {
            CJSystem.logging().error(getClass(),
                    String.format("租户：%s 不存在。网关：%s%s[%s@%s]", tenant, frame.parameter("location"),
                            frame.parameter("title"), frame.parameter("desc"), frame.parameter("uuid")));
            IOutputer out = selector.select(frame);
            out.closePipeline();
            return;
        }
        circuit.attribute("uc.principals", user);
        pipeline.nextFlow(request, response, this);
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
