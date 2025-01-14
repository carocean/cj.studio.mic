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
 	services:{
 		online:'online',
 		selector:'$.output.selector',
 		remoteAuth:'$openports.cj.studio.openport.client.IRequestAdapter'
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
var StringBuffer = Java.type('java.lang.StringBuffer');
var CircuitException = Java.type('cj.studio.ecm.net.CircuitException');
var SocketContants = Java.type('cj.studio.gateway.socket.util.SocketContants');
var IRequestAdapter = Java.type('cj.studio.openport.client.IRequestAdapter');
var Encript = Java.type('cj.netos.uc.util.Encript');
var UUID = Java.type('java.util.UUID');

exports.flow = function (f, c, ctx) {
    var token = f.parameter('cjtoken');
    var selector = imports.head.services.selector;
    var pipeline_name = f.head(SocketContants.__frame_fromPipelineName);
    var out = selector.select(pipeline_name);
    if (token == null) {//认证不通过，则关闭客端的ws通道，关闭方法是使用ioutputselector.selector(pipeline_name).closePipeline();
        out.closePipeline();
    }
    var remoteAuth = imports.head.services.remoteAuth;

    var site = chip.site();
    var appId = site.getProperty("app-id");
    var appKey = site.getProperty("app-key");
    var appSecret = site.getProperty("app-secret");
    var nonce = Encript.md5(UUID.randomUUID().toString());
    var sign = Encript.md5(String.format("%s%s%s", appKey, nonce, appSecret));

    try {
        var retvalue = remoteAuth.request("get", "http/1.1", {
            'Rest-Command': 'verification',
            'app-id': appId,
            'app-key': appKey,
            'app-nonce': nonce,
            'app-sign': sign
        }, {
            'token': token
        }, null);
        var response=JSON.parse(retvalue);

        if (200!== response["status"]) {
            throw new CircuitException(response.get("status") + "", "uc响应错误：" + response.get("message"));
        }
        var dataText =  response["dataText"];
        var entry=JSON.parse(dataText);
        var user = entry.person;
        var online = imports.head.services.online;
        online.on(user, pipeline_name);
    } catch (e) {
        out.closePipeline();
        print(e);
        return;
    }

}
