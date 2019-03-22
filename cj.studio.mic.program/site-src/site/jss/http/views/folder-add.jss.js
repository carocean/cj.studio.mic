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
 		nodeTree:'micplugin.ntService'
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
var StringUtil = Java.type('cj.ultimate.util.StringUtil');
var StringBuffer = Java.type('java.lang.StringBuffer');
var CircuitException=Java.type('cj.studio.ecm.net.CircuitException');
var Gson=Java.type('cj.ultimate.gson2.com.google.gson.Gson');
var TFolder=Java.type('cj.studio.mic.ultimate.TFolder');

exports.flow = function(f,c,ctx) {
	var nodeTree=imports.head.services.nodeTree;
	var code=f.parameter('code');
	var name=f.parameter('name');
	var path=f.parameter('path');
	if(path==null){
		path='/';
	}
	var folder=new TFolder();
	folder.code=code;
	folder.name=name;
	folder.path=path;
	folder.creator=f.session().attribute('uc.principals');
	nodeTree.addFolder(folder);
	var json=new Gson().toJson(folder);
	c.content().writeBytes(json.getBytes());
}

