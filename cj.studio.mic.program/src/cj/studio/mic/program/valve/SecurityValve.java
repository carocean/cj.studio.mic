package cj.studio.mic.program.valve;

import java.util.List;
import java.util.Map;

import cj.studio.backend.uc.bo.Role;
import cj.studio.backend.uc.stub.ITokenStub;
import cj.studio.ecm.Scope;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.http.HttpCircuit;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.ecm.net.session.ISession;
import cj.studio.gateway.socket.pipeline.IAnnotationInputValve;
import cj.studio.gateway.socket.pipeline.IIPipeline;
import cj.studio.gateway.stub.IRest;
import cj.ultimate.util.StringUtil;

@CjService(name = "securityValve", scope = Scope.multiton)
public class SecurityValve implements IAnnotationInputValve {
	@CjServiceRef(refByName="$.rest")
	IRest rest;
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
			List<Role> roles = (List<Role>) session.attribute("uc.roles");
			boolean hasTestRole = false;
			if (roles != null) {
				for (Role r : roles) {
					if ("tests".equals(r.getCode())) {
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
			pipeline.nextFlow(request, response, this);
			return;
		}
		Frame frame=(Frame)request;
		Circuit circuit=(Circuit)response;
		if (frame.relativePath().startsWith("/public")) {
			pipeline.nextFlow(request, response, this);
			return;
		}
		String cjtoken=frame.parameter("cjtoken");
		if(StringUtil.isEmpty(cjtoken)) {
			throw new CircuitException("801", "缺少令牌，访问被拒绝");
		}
		ITokenStub tokenStub=rest.forRemote("rest://backend/uc/").open(ITokenStub.class);
		Map<String, Object> map=tokenStub.parse(cjtoken);
		circuit.attribute("uc.principals",map.get("sub"));
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
