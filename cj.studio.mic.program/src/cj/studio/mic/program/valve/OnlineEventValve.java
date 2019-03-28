package cj.studio.mic.program.valve;

import java.util.Map;
import java.util.Set;

import cj.studio.ecm.Scope;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IInputChannel;
import cj.studio.ecm.net.IOutputChannel;
import cj.studio.ecm.net.io.MemoryContentReciever;
import cj.studio.ecm.net.io.MemoryInputChannel;
import cj.studio.ecm.net.io.MemoryOutputChannel;
import cj.studio.gateway.socket.pipeline.IAnnotationInputValve;
import cj.studio.gateway.socket.pipeline.IIPipeline;
import cj.studio.gateway.socket.pipeline.IOutputSelector;
import cj.studio.gateway.socket.pipeline.IOutputer;
import cj.studio.gateway.socket.util.SocketContants;
import cj.studio.mic.program.IOnlineTable;
import cj.studio.mic.ultimate.INodeTreeService;
import cj.ultimate.gson2.com.google.gson.Gson;

@CjService(name = "onlineEventValve", scope = Scope.multiton)
public class OnlineEventValve implements IAnnotationInputValve {
	@CjServiceRef(refByName = "micplugin.ntService")
	INodeTreeService ntree;
	@CjServiceRef(refByName = "online")
	IOnlineTable table;
	@CjServiceRef(refByName = "$.output.selector")
	IOutputSelector selector;

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

		if ("tcp".equals(pipeline.prop(SocketContants.__pipeline_fromProtocol))) {
			Map<String, Object> onlineEntity = ntree.offline(inputName);
			if (onlineEntity != null) {
				Set<String> users = table.enumUser();
				for (String user : users) {
					String inputchannel = table.getUserOnPipeline(user);
					notifyUserNodeOffline(inputchannel, onlineEntity);
				}
			}
		} else {
			table.offPipeline(inputName);
		}
		pipeline.nextOnInactive(inputName, this);
	}

	@Override
	public int getSort() {
		return 3;
	}

	private void notifyUserNodeOffline(String userChannel, Map<String, Object> onlineEntity) throws CircuitException {
		IOutputer output = selector.select(userChannel);
		IInputChannel in = new MemoryInputChannel();
		Frame f = new Frame(in, String.format("notify /node/offline.service mic/1.0"));
		f.content().accept(new MemoryContentReciever());
		in.begin(f);
		byte[] b = new Gson().toJson(onlineEntity).getBytes();
		in.done(b, 0, b.length);

		IOutputChannel out = new MemoryOutputChannel();
		Circuit c = new Circuit(out, "mic/1.0 200 OK");
		output.send(f, c);
		output.releasePipeline();
	}
}
