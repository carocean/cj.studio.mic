package cj.studio.mic.program.view;

import java.util.Set;

import cj.studio.ecm.CJSystem;
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
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.studio.gateway.socket.pipeline.IOutputSelector;
import cj.studio.gateway.socket.pipeline.IOutputer;
import cj.studio.gateway.socket.util.SocketContants;
import cj.studio.mic.program.IOnlineTable;
import cj.studio.mic.ultimate.INodeOnRemoveEvent;
import cj.studio.mic.ultimate.INodeTreeService;
import cj.studio.mic.ultimate.TNode;
import cj.ultimate.gson2.com.google.gson.Gson;

@CjService(name = "/node.service")
public class Node implements IGatewayAppSiteWayWebView {
	@CjServiceRef(refByName = "micplugin.ntService")
	INodeTreeService ntree;
	@CjServiceRef(refByName = "$.output.selector")
	IOutputSelector selector;
	@CjServiceRef(refByName = "online")
	IOnlineTable table;

	@Override
	public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
		String location = frame.parameter("location");
		String title = frame.parameter("title");
		String uuid = frame.parameter("uuid");
		String desc = frame.parameter("desc");
		String micient = frame.parameter("micient");
		if (ntree.getFolder(location) == null) {
			CJSystem.logging().warn(getClass(),
					String.format("节点：%s(%s)@%s,要注册的路径不存在：%s", title, desc, uuid, location));
			IOutputer output = selector.select(frame);
			notifyErrorToNode(output, micient, location);
			return;
		}

		TNode n = ntree.getNode(String.format("%s.%s", location, uuid));
		if (n != null) {
			CJSystem.logging().info(getClass(), "更新节点：" + location + n.getUuid() + "[" + n.getTitle() + "]");
			ntree.updateNode(location, n);
			ntree.online(n, frame.head(SocketContants.__frame_fromPipelineName));
			Set<String> users = table.enumUser();
			for (String user : users) {
				String inputchannel = table.getUserOnPipeline(user);
				notifyUserNodeOnline(inputchannel, n);
			}
			return;
		}
		n = new TNode(uuid, title, desc, location, micient);
		INodeOnRemoveEvent event = new NodeOnRemoveEvent();
		ntree.addNode(n, event);
		ntree.online(n, frame.head(SocketContants.__frame_fromPipelineName));
		Set<String> users = table.enumUser();
		for (String user : users) {
			String inputchannel = table.getUserOnPipeline(user);
			notifyUserNodeOnline(inputchannel, n);
		}
		CJSystem.logging().info(getClass(), "新增节点：" + location + n.getUuid() + "[" + n.getTitle() + "]");
	}

	private void notifyUserNodeOnline(String userChannel, TNode n) throws CircuitException {
		IOutputer output = selector.select(userChannel);
		IInputChannel in = new MemoryInputChannel();
		Frame f = new Frame(in, String.format("notify /node/online.service mic/1.0"));
		f.content().accept(new MemoryContentReciever());
		in.begin(f);
		byte[] b = new Gson().toJson(n).getBytes();
		in.done(b, 0, b.length);

		IOutputChannel out = new MemoryOutputChannel();
		Circuit c = new Circuit(out, "mic/1.0 200 OK");
		output.send(f, c);
		output.releasePipeline();
	}

	private void notifyUserNodeOnRemoved(String userChannel, String uuid) throws CircuitException {
		IOutputer output = selector.select(userChannel);
		IInputChannel in = new MemoryInputChannel();
		Frame f = new Frame(in, String.format("notify /node/onremoved.service mic/1.0"));
		f.content().accept(new MemoryContentReciever());
		f.parameter("uuid", uuid);
		in.begin(f);
		byte[] b = new byte[0];
		in.done(b, 0, b.length);

		IOutputChannel out = new MemoryOutputChannel();
		Circuit c = new Circuit(out, "mic/1.0 200 OK");
		output.send(f, c);
		output.releasePipeline();
	}

	private void notifyErrorToNode(IOutputer output, String micient, String location) throws CircuitException {
		IInputChannel in = new MemoryInputChannel();
		Frame f = new Frame(in, String.format("notify /%s/error/register-location-error.service mic/1.0", micient));
		f.parameter("location", location);
		f.content().accept(new MemoryContentReciever());
		in.begin(f);
		in.done(new byte[0], 0, 0);

		IOutputChannel out = new MemoryOutputChannel();
		Circuit c = new Circuit(out, "mic/1.0 200 OK");
		output.send(f, c);
		output.releasePipeline();
	}

	class NodeOnRemoveEvent implements INodeOnRemoveEvent {

		@Override
		public void onRemoved(String uuid) throws CircuitException {
			Set<String> users = table.enumUser();
			for (String user : users) {
				String inputchannel = table.getUserOnPipeline(user);
				notifyUserNodeOnRemoved(inputchannel, uuid);
			}
		}

	}
}
