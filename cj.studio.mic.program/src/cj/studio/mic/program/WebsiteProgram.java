package cj.studio.mic.program;

import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.socket.Destination;
import cj.studio.gateway.socket.app.GatewayAppSiteProgram;
import cj.studio.gateway.socket.app.ProgramAdapterType;
import cj.studio.gateway.socket.pipeline.IOutputSelector;
import cj.studio.mic.ultimate.INodeTreeService;

@CjService(name = "$.cj.studio.gateway.app", isExoteric = true)
public class WebsiteProgram extends GatewayAppSiteProgram {
	@CjServiceRef(refByName = "micplugin.ntService")
	INodeTreeService ntree;
	@CjServiceRef(refByName = "$.output.selector")
	IOutputSelector selector;
	@Override
	protected void onstart(Destination dest, String home, ProgramAdapterType type) throws CircuitException {
		ntree.dropNodes();
	}



}
