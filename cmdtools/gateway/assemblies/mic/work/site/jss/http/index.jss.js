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
	var userConsoleSession=imports.head.services.userConsoleSession;
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
		li.attr('title',folder.name+'');
		li.select('.folder-code').html(folder.code+'['+folder.name+']');
		var count=nodeTree.getNodeCountOfFolder(folder.path+folder.code);
		li.select('.folder-count>span').html(count);
		
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
		li.attr('title',folder.name+'');
		li.select('.obj-code').html(folder.code+'['+folder.name+']');
		
		printNodes(folder.getFullName(),li,nodeTree,creator);
		
		objsE.appendChild(li);
	}
}
function printNodes(path,li,nodeTree,creator){
	var methodUL=li.select('.pr-methods').first();
	var methodLi=methodUL.select('>.pr-method').first().clone();
	methodUL.empty();
	var nodes=nodeTree.listNodes(path);
	for(var i=0;i<nodes.length;i++){
		var m=nodes.get(i);
		var li=methodLi.clone();
		li.attr('uuid',m.uuid+'');
		li.attr('path',m.path+'');
		li.attr('title',m.desc+'');
		if(nodeTree.isOnline(m)){
			li.select('.method-command').attr('src','img/running.svg');
		}else{
			li.select('.method-command').attr('src','img/stoped.svg');
		}
		li.select('.method-code').html(m.title+'');
		methodUL.appendChild(li);
	}
}
