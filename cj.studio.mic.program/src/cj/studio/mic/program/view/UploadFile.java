package cj.studio.mic.program.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IInputChannel;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.ecm.net.io.MemoryContentReciever;
import cj.studio.ecm.net.io.MemoryInputChannel;
import cj.studio.ecm.net.io.MemoryOutputChannel;
import cj.studio.ecm.net.io.SimpleInputChannel;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.studio.gateway.socket.io.MultipartFormContentReciever;
import cj.studio.gateway.socket.io.decoder.mutipart.IFieldDataListener;
import cj.studio.gateway.socket.io.decoder.mutipart.IFieldInfo;
import cj.studio.gateway.socket.io.decoder.mutipart.IFormData;
import cj.studio.gateway.socket.io.decoder.mutipart.listener.FileListener;
import cj.studio.gateway.socket.pipeline.IOutputSelector;
import cj.studio.gateway.socket.pipeline.IOutputer;
import cj.studio.mic.program.IOnlineTable;
import cj.studio.mic.ultimate.INodeTreeService;
import cj.studio.mic.ultimate.TNode;
import cj.ultimate.gson2.com.google.gson.Gson;

@CjService(name = "/uploadFile.service")
public class UploadFile implements IGatewayAppSiteWayWebView {
	@CjServiceRef(refByName = "micplugin.ntService")
	INodeTreeService nodeTree;
	@CjServiceRef(refByName = "$.output.selector")
	IOutputSelector selector;
	@CjServiceRef(refByName = "online")
	IOnlineTable online;

	@Override
	public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
		String path = resource.getRealHttpSiteRootPath();
		File f = new File(path);
		path = f.getParentFile().getParentFile().getAbsolutePath();
		String storePath = String.format("%s%stemp%s", path, File.separator, File.separator);

		// 发送site和plugin命令到node
		frame.content().accept(new MultipartFormContentReciever() {
			@Override
			public void begin(Frame frame) {
				super.begin(frame);
			}

			@Override
			protected void done(Frame f, IFormData form) throws CircuitException {
				// TODO Auto-generated method stub
				String[] arr = form.enumFieldName();
				for (String fn : arr) {
					IFieldInfo fi = form.getFieldInfo(fn);
					if (fi.isFile()) {
						frame.parameter(fi.name(), fi.filename());
					} else {
						frame.parameter(fi.name(), fi.value());
					}
				}
				String path = f.parameter("path");
				String uuid = f.parameter("uuid");
				String cmdline = f.parameter("cmdline");
				String user = (String)( (HttpFrame)frame).session().attribute("uc.principals");

				TNode n = nodeTree.getNode(path + "." + uuid);
				if (n == null) {
					sendResponseToUser(user, path, uuid, "节点不存在:" + path + uuid, cmdline);
					return;
				}
				Map<String, Object> entry = nodeTree.getOnlineEntry(n);
				if (entry == null) {
					sendResponseToUser(user, path, uuid, "节点不在线:" + path + uuid, cmdline);
					return;
				}
				String channel = (String) entry.get("channel");
				String upload_file=f.parameter("upload_file");
				String fn=String.format("%s%s", storePath,upload_file);
				sendcmd(channel, cmdline,fn, n.getMiclient(), user);
			}

			@Override
			protected IFieldDataListener createFieldDataListener() {
				File f = new File(storePath);
				if (!f.exists()) {
					f.mkdirs();
				}
				return new FileListener(storePath);
			}
		});

	}

	public void sendResponseToUser(String user, String path, String uuid, String text, String cmdline)
			throws CircuitException {
		if (text == null) {
			text = "";
		}
		String channel = online.getUserOnPipeline(user);
		IOutputer output = selector.select(channel);
		MemoryInputChannel input = new MemoryInputChannel();
		Frame f = new Frame(input, "notify /node/response.service mic/1.0");
		f.content().accept(new MemoryContentReciever());
		f.parameter("uuid", uuid);
		f.parameter("path", path);
		f.parameter("cmdline", cmdline);
		input.begin(f);
		Map<String, String> map = new HashMap<>();
		map.put("response", text);
		byte[] b = new Gson().toJson(map).getBytes();
		input.done(b, 0, b.length);

		MemoryOutputChannel out = new MemoryOutputChannel();
		Circuit c = new Circuit(out, "mic/1.0 200 OK");
		output.send(f, c);
		output.releasePipeline();
	}

	public void sendcmd(String channel, String cmdline,String upload_file, String micient, String user) throws CircuitException {
		File file=new File(upload_file);
		IOutputer output = selector.select(channel);
		IInputChannel input = new SimpleInputChannel();
		Frame f = new Frame(input, String.format("exe /%s/cmdline.service mic/1.0", micient));
		f.content().accept(new MemoryContentReciever());
		f.parameter("cmdline", cmdline);
		f.parameter("user", user);
		f.parameter("fileName",file.getName());
		input.begin(f);

		MemoryOutputChannel out = new MemoryOutputChannel();
		Circuit c = new Circuit(out, "mic/1.0 200 OK");
		output.send(f, c);
		FileInputStream in=null;
		try {
			in=new FileInputStream(file);
			byte[] buf=new byte[8192];
			int read=0;
			while((read=in.read(buf,0,buf.length))>-1) {
				input.writeBytes(buf,0,read);
			}
			input.done(new byte[0], 0, 0);
		} catch (IOException e) {
			if(e instanceof FileNotFoundException) {
				throw new CircuitException("404", e);
			}
			throw new CircuitException("503", e);
		}finally {
			if(in!=null) {
				try {
					in.close();
					file.delete();
				} catch (IOException e) {
				}
			}
		}
		output.releasePipeline();
	}
}
