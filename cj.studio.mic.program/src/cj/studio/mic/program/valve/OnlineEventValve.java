package cj.studio.mic.program.valve;

import cj.studio.ecm.Scope;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.socket.pipeline.IAnnotationInputValve;
import cj.studio.gateway.socket.pipeline.IIPipeline;

@CjService(name = "onlineEventValve", scope = Scope.multiton)
public class OnlineEventValve implements IAnnotationInputValve {

	@Override
	public void onActive(String inputName, IIPipeline pipeline) throws CircuitException {
		System.out.println("-----onActive");
		pipeline.nextOnActive(inputName, this);
	}

	@Override
	public void flow(Object request, Object response, IIPipeline pipeline) throws CircuitException {
		pipeline.nextFlow(request, response, this);
	}

	@Override
	public void onInactive(String inputName, IIPipeline pipeline) throws CircuitException {
		System.out.println("-----onInactive");
		pipeline.nextOnInactive(inputName, this);
	}

	@Override
	public int getSort() {
		return 3;
	}

}
