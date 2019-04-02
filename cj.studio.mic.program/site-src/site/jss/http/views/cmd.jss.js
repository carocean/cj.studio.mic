/*
 * 2016.0829
 * 作者：赵向彬
 * 说明：services是声明本jss要引用的服务
 * <![jss:{
		scope:'runtime',
		extends:'cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView',
		isStronglyJss:true,
		filter:{
	 	} 	
	},
	object:{
	 		name:"test..."
	},
 	services:{
 		nodeTree:'micplugin.ntService',
 		selector:'$.output.selector',
 		online:'online'
 	}
 ]>
 <![desc:{
	ttt:'2323',
	obj:{
		name:'09skdkdk'
		}
* }]>
 */

var String = Java.type('java.lang.String');
var HashMap = Java.type('java.util.HashMap');
var StringUtil = Java.type('cj.ultimate.util.StringUtil');
var StringBuffer = Java.type('java.lang.StringBuffer');
var CircuitException=Java.type('cj.studio.ecm.net.CircuitException');
var Gson=Java.type('cj.ultimate.gson2.com.google.gson.Gson');
var MemoryInputChannel=Java.type('cj.studio.ecm.net.io.MemoryInputChannel');
var Frame=Java.type('cj.studio.ecm.net.Frame');
var MemoryContentReciever=Java.type('cj.studio.ecm.net.io.MemoryContentReciever');
var MemoryOutputChannel=Java.type('cj.studio.ecm.net.io.MemoryOutputChannel');
var Circuit=Java.type('cj.studio.ecm.net.Circuit');
var JavaUtil=Java.type('cj.ultimate.util.JavaUtil');

exports.flow = function(f,c,ctx) {
	var nodeTree=imports.head.services.nodeTree;
	var path=f.parameter('path');
	var uuid=f.parameter('uuid');
	var cmdline=f.parameter('cmdline');
	var user=c.attribute('uc.principals');
	print('执行命令:'+cmdline);
	var n=nodeTree.getNode(path+'.'+uuid);
	if(n==null){
		sendResponseToUser(user,path,uuid,'节点不存在:'+path+uuid,path,uuid,cmdline);
		return;
	}
	var entry=nodeTree.getOnlineEntry(n);
	if(entry==null){
		sendResponseToUser(user,path,uuid,'节点不在线:'+path+uuid,path,uuid,cmdline);
		return;
	}
	var channel=entry.channel;
	try{
		sendcmd(channel,cmdline,n.miclient,user);
	}catch(e){
		var ce=CircuitException.search(e);
		if(ce!=null && ce.getStatus()=='404'){
			notifyUserOffline(user,path,uuid);
			return;
		}
		throw e;
	}	
}
function notifyUserOffline(user,path,uuid){
	var selector=imports.head.services.selector;
	var online=imports.head.services.online;
	var channel=online.getUserOnPipeline(user);
	var output=selector.select(channel);
	var input = new MemoryInputChannel();
	var f = new Frame(input,"notify /node/offline.service mic/1.0");
	f.content().accept(new MemoryContentReciever());
	var map=new HashMap();
	map.put('uuid',uuid);
	map.put('path',path);
	input.begin(f);
	var b=new Gson().toJson(map).getBytes();
	input.done(b, 0, b.length);

	var out=new MemoryOutputChannel();
	var c=new Circuit(out, "mic/1.0 200 OK");
	output.send(f, c);
	output.releasePipeline();
}
function sendResponseToUser(user,path,uuid,text,path,uuid,cmdline){
	if(text==null){
		text='';
	}
	var selector=imports.head.services.selector;
	var online=imports.head.services.online;
	var channel=online.getUserOnPipeline(user);
	var output=selector.select(channel);
	var input = new MemoryInputChannel();
	var f = new Frame(input,"notify /node/response.service mic/1.0");
	f.content().accept(new MemoryContentReciever());
	f.parameter('uuid',uuid);
	f.parameter('path',path);
	f.parameter('cmdline',cmdline);
	input.begin(f);
	var map=new HashMap();
	map.put('response',text);
	var b=new Gson().toJson(map).getBytes();
	input.done(b, 0, b.length);

	var out=new MemoryOutputChannel();
	var c=new Circuit(out, "mic/1.0 200 OK");
	output.send(f, c);
	output.releasePipeline();
}
function sendcmd(channel,cmdline,micient,user){
	var selector=imports.head.services.selector;
	var output=selector.select(channel);
	var input = new MemoryInputChannel();
	var f = new Frame(input, String.format("exe /%s/cmdline.service mic/1.0",micient));
	f.content().accept(new MemoryContentReciever());
	f.parameter('cmdline',cmdline);
	f.parameter('user',user);
	input.begin(f);
	var b=JavaUtil.createByteArray(0);
	input.done(b, 0, b.length);

	var out=new MemoryOutputChannel();
	var c=new Circuit(out, "mic/1.0 200 OK");
	output.send(f, c);
	output.releasePipeline();
}
