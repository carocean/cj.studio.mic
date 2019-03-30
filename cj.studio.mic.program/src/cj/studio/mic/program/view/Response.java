package cj.studio.mic.program.view;

import java.util.HashMap;
import java.util.Map;

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
import cj.ultimate.gson2.com.google.gson.Gson;

@CjService(name = "/response.service")
public class Response implements IGatewayAppSiteWayWebView {
	@CjServiceRef(refByName = "$.output.selector")
	IOutputSelector selector;
	@CjServiceRef(refByName = "online")
	IOnlineTable online;

	@Override
	public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
		frame.content().accept(new MemoryContentReciever() {
			@Override
			public void done(byte[] b, int pos, int length) throws CircuitException {
				super.done(b, pos, length);
				byte[] all=readFully();
				String user=frame.parameter("user");
				String channel=online.getUserOnPipeline(user);
				IOutputer output=selector.select(channel);
				IInputChannel input = new MemoryInputChannel();
				Frame f = new Frame(input,"notify /node/response.service mic/1.0");
				f.content().accept(new MemoryContentReciever());
				input.begin(f);
				Map<String,Object> map=new HashMap<>();
				frame.content().readFully();
				map.put("response",new String(all));
				byte[] data=new Gson().toJson(map).getBytes();
				input.done(data, 0, data.length);
		
				IOutputChannel out=new MemoryOutputChannel();
				Circuit c=new Circuit(out, "mic/1.0 200 OK");
				output.send(f, c);
				output.releasePipeline();
			}
		});

	}
}
