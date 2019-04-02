package cj.studio.mic.program.view;

import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.studio.mic.ultimate.INodeTreeService;

@CjService(name="/views/getNodeCount.service")
public class GetNodeCount implements IGatewayAppSiteWayWebView {
	@CjServiceRef(refByName = "micplugin.ntService")
	INodeTreeService ntree;
	@Override
	public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
		String path=frame.parameter("path");
		long count=ntree.getNodeCountOfFolder(path);
		circuit.content().writeBytes(String.format("%s", count).getBytes());
	}

}
