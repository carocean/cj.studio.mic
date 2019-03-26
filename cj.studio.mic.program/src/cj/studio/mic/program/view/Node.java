package cj.studio.mic.program.view;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.studio.mic.ultimate.INodeTreeService;
import cj.studio.mic.ultimate.TNode;

@CjService(name = "/node.service")
public class Node implements IGatewayAppSiteWayWebView {
	@CjServiceRef(refByName = "micplugin.ntService")
	INodeTreeService ntree;

	@Override
	public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
		String location = frame.parameter("location");
		String title = frame.parameter("title");
		String uuid = frame.parameter("uuid");
		String desc = frame.parameter("desc");
		
		if (ntree.getFolder(location)==null) {
			CJSystem.logging().warn(getClass(),"不存在路径："+location);
			notifyClient(location);
			return;
		}
		TNode n = ntree.getNode(String.format("%s.%s", location, uuid));
		if (n != null) {
			CJSystem.logging().info(getClass(),"更新节点："+location+n.getUuid()+"["+n.getTitle()+"]");
			ntree.updateNode(location, n);
			return;
		}
		n = new TNode(uuid, title, desc, location);
		ntree.addNode(n);
		CJSystem.logging().info(getClass(),"新增节点："+location+n.getUuid()+"["+n.getTitle()+"]");
	}

	private void notifyClient(String location) {
		
		
	}

}
