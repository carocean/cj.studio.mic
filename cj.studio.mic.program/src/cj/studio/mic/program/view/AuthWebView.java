package cj.studio.mic.program.view;

import cj.netos.uc.model.AppKeyPair;
import cj.netos.uc.model.UcRole;
import cj.netos.uc.port.IAuthPort;
import cj.netos.uc.util.Encript;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjBridge;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.http.CookieHelper;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.studio.gateway.socket.io.XwwwFormUrlencodedContentReciever;
import cj.studio.openport.client.IRequestAdapter;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CjService(name = "/public/auth.service")
public class AuthWebView implements IGatewayAppSiteWayWebView {

    //    @CjServiceRef(refByName = "$openports.cj.netos.uc.port.IAuthPort")
//    IAuthPort auth;
    @CjServiceRef(refByName = "$openports.cj.studio.openport.client.IRequestAdapter")
    IRequestAdapter adapter;
    @CjServiceSite
    IServiceSite site;

    @Override
    public void flow(Frame f, Circuit c, IGatewayAppSiteResource ctx) throws CircuitException {
        f.content().accept(new XwwwFormUrlencodedContentReciever() {

            @Override
            protected void done(Frame f) throws CircuitException {
                final String user = f.parameter("user");
                String pwd = f.parameter("pwd");
                String appId = site.getProperty("app-id");
                String appKey = site.getProperty("app-key");
                String appSecret = site.getProperty("app-secret");
                String nonce = Encript.md5(UUID.randomUUID().toString());
                String sign = Encript.md5(String.format("%s%s%s", appKey, nonce, appSecret));

                String retvalue = adapter.request("get", "http/1.1", new HashMap<String, String>() {
                    {
                        put("Rest-Command", "auth");
                        put("app-id", appId);
                        put("app-key", appKey);
                        put("app-nonce", nonce);
                        put("app-sign", sign);
                    }
                }, new HashMap<String, String>() {
                    {
                        put("accountCode", user);
                        put("password", pwd);
                        put("device", "mic");
                    }
                }, null);

                Map<String, Object> response = new Gson().fromJson(retvalue, new TypeToken<HashMap<String, Object>>() {
                }.getType());
                if (200.0 != (double) response.get("status")) {
                    throw new CircuitException(response.get("status") + "", "uc响应错误：" + response.get("message"));
                }

                String dataText = (String) response.get("dataText");
                Map<String, Object> entry = new Gson().fromJson(dataText, new TypeToken<HashMap<String, Object>>() {
                }.getType());
                Map<String, Object> subject = (Map<String, Object>) entry.get("subject");
                Map<String, Object> token = (Map<String, Object>) entry.get("token");
                String person = (String) subject.get("person");//得到统一用户
                String accessToken = (String) token.get("accessToken");
                HttpFrame frame = (HttpFrame) f;
                frame.session().attribute("uc.token", accessToken);
                frame.session().attribute("uc.principals", person);
                ArrayList<String> roles = (ArrayList<String>) subject.get("roles");
                frame.session().attribute("uc.roles", roles);
            }
        });
    }

}
