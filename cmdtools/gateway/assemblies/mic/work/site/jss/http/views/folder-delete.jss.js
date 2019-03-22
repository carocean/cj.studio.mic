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

exports.flow = function(f,c,ctx) {
	var nodeTree=imports.head.services.nodeTree;
	var path=f.parameter('path');
	if(path==null){
		throw new CircuitException('404','缺少参数：path');
	}
	nodeTree.removeFolder(path);
}

