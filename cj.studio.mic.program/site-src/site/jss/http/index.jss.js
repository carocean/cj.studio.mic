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

exports.flow = function(f,c,ctx) {
	var doc = ctx.html("/index.html", "utf-8");
	var creator=f.session().attribute('uc.principals');
	var nodeTree=imports.head.services.nodeTree;
	if('true'==f.parameter('onlyPrintPt')){
		printProjectTree(f,doc,nodeTree,creator);
		var tree=doc.select('.pr-tree').first();
		c.content().writeBytes(tree.html().getBytes());
		return;
	}
	printWelcome(doc,f);
	printProjectTree(f,doc,nodeTree,creator);
	c.content().writeBytes(doc.toString().getBytes());
}
function printWelcome(doc,f){
	var connLabel=doc.select('.container > .workbench > .header > .topbar > .items>li[conn]');
	var wsurl=String.format('ws://%s/mic',f.head('Host'));
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
function printProjectTree(f,doc,nodeTree,creator){
	var foldersE=doc.select('.container > .workbench > .desktop > .column .column-left > .proj-region > .pr-tree > .pr-folders').first();
	var cli=foldersE.select('>.pr-folder').first().clone();
	foldersE.empty();
	var folders=nodeTree.listChildFolders('/');
	for(var i=0;i<folders.length;i++){
		var folder=folders.get(i);
		var li=cli.clone();
		li.attr('code',folder.code+'');
		li.attr('path',folder.path+'');
		li.attr('title',folder.code+'');
		li.select('.folder-code').html(folder.name+'');
//		var count=nodeTree.getMethodCountOfFolder(folder.code);
//		li.select('.folder-count>span').html(count);
		
		printServices(folder.getFullName(),li,nodeTree,creator);
		
		foldersE.appendChild(li);
	}
}
function printServices(parent,li,nodeTree,creator){
	var objsE=li.select('.pr-objs').first();
	var cli=objsE.select('>li').first().clone();
	objsE.empty();
	var folders=nodeTree.listChildFolders(parent);
	for(var i=0;i<folders.length;i++){
		var folder=folders.get(i);
		var li=cli.clone();
		li.attr('code',folder.code+'');
		li.attr('path',folder.path+'');
		li.attr('title',folder.code+'');
		li.select('.obj-code').html(folder.name+'');
		
//		printMethods(folderCode,service.code,li,ptStub,creator,rcStub);
		
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
