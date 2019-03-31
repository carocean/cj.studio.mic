package cj.studio.mic.program.view;

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
import cj.studio.mic.program.IOnlineTable;
import cj.studio.mic.ultimate.IUserConsoleSession;

@CjService(name = "/onCDConsole.service")
public class OnCDConsole implements IGatewayAppSiteWayWebView {
	@CjServiceRef(refByName = "$.output.selector")
	IOutputSelector selector;
	@CjServiceRef(refByName = "online")
	IOnlineTable online;
	@CjServiceRef(refByName = "micplugin.userConsoleSession")
	IUserConsoleSession userConsoleSession;
	@Override
	public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
		String user=frame.parameter("user");
		String consoleName=frame.parameter("consoleName");
		String channel=online.getUserOnPipeline(user);
		IOutputer output=selector.select(channel);
		IInputChannel input = new MemoryInputChannel();
		Frame f = new Frame(input,"notify /node/onCDConsole.service mic/1.0");
		f.content().accept(new MemoryContentReciever());
		f.parameter("consoleName",consoleName);
		input.begin(f);
		byte[] data=new byte[0];
		input.done(data, 0, data.length);

		IOutputChannel out=new MemoryOutputChannel();
		Circuit c=new Circuit(out, "mic/1.0 200 OK");
		output.send(f, c);
		output.releasePipeline();
		userConsoleSession.saveCurrentConsoleName(user, consoleName);
	}
}
