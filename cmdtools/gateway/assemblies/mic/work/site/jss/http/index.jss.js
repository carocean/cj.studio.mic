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
 		rest:'$.rest'
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

exports.flow = function(f,c,ctx) {
	var doc = ctx.html("/index.html", "utf-8");
//	var creator=f.session().attribute('uc.principals');
//	if('true'==f.parameter('onlyPrintPt')){
//		printProjectTree(f,doc,ptStub,creator,rcStub);
//		var tree=doc.select('.pr-tree').first();
//		c.content().writeBytes(tree.html().getBytes());
//		return;
//	}
//	printWelcome(doc,f);
//	printProjectTree(f,doc,ptStub,creator,rcStub);
	c.content().writeBytes(doc.toString().getBytes());
}
function printWelcome(doc,f){
	var connLabel=doc.select('.container > .workbench > .header > .topbar > .items>li[conn]');
	var wsurl=String.format('ws://%s/myChannel',f.head('Host'));
	connLabel.attr('wsurl',wsurl);
	var roleE=doc.select('.container > .workbench > .header > .topbar > .items span[role]').first();
	var roles=f.session().attribute('uc.roles');
	var sb=new StringBuffer();
	for(var i=0;i<roles.length;i++){
		var r=roles.get(i);
		sb.append(r.name);
		sb.append(';');
	}
	roleE.html(sb);
	var codeE=doc.select('.container > .workbench > .header > .topbar > .items span[code]').first();
	codeE.html(f.session().attribute('uc.principals'));
}
function printProjectTree(f,doc,ptStub,creator,rcStub){
	var foldersE=doc.select('.container > .workbench > .desktop > .column .column-left > .proj-region > .pr-tree > .pr-folders').first();
	var cli=foldersE.select('>.pr-folder').first().clone();
	foldersE.empty();
	var folders=ptStub.getFolders();
	for(var i=0;i<folders.length;i++){
		var folder=folders.get(i);
		var li=cli.clone();
		li.attr('id',folder.id);
		li.attr('code',folder.code);
		li.attr('title',StringUtil.isEmpty(folder.name)?'':folder.name);
		li.select('.folder-code').html(folder.code);
		var count=ptStub.getMethodCountOfFolder(folder.code);
		li.select('.folder-count>span').html(count);
		
		printServices(folder.code,li,ptStub,creator,rcStub);
		
		foldersE.appendChild(li);
	}
}
function printServices(folderCode,li,ptStub,creator,rcStub){
	var objsE=li.select('.pr-objs').first();
	var cli=objsE.select('>li').first().clone();
	objsE.empty();
	var services=ptStub.getServices(folderCode);
	for(var i=0;i<services.length;i++){
		var service=services.get(i);
		var li=cli.clone();
		li.attr('id',service.id);
		li.attr('code',service.code);
		li.attr('folder',service.folder);
		li.attr('title',StringUtil.isEmpty(service.name)?'':service.name);
		li.select('.obj-code').html(service.code);
		
		printMethods(folderCode,service.code,li,ptStub,creator,rcStub);
		
		objsE.appendChild(li);
	}
}
function printMethods(folderCode,servicecode,li,ptStub,creator,rcStub){
	var methodUL=li.select('.pr-methods').first();
	var methodLi=methodUL.select('>.pr-method').first().clone();
	methodUL.empty();
	var methods=ptStub.getMethods(folderCode+'.'+servicecode);
	for(var i=0;i<methods.length;i++){
		var m=methods.get(i);
		var headline=rcStub.getMyRequestHeadline(m.id,creator);
		var netpt=rcStub.getMyRequestNetprotocol(m.id,creator);
		var li=methodLi.clone();
		li.attr('id',m.id+'');
		li.attr('code',m.code+'');
		var ptcol='';
		if(netpt!=null){
			ptcol=netpt.protocol;
		}
		li.attr('netprotocol',ptcol);
		if(headline!=null){
			li.select('.method-command').html(headline.cmd+'');
		}
		li.attr('folder',m.folder+'');
		li.attr('service',m.service+'');
		li.attr('title',m.name+'');
		li.select('.method-code').html(m.code+'');
		methodUL.appendChild(li);
	}
}
