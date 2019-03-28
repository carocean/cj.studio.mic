$(document).ready(function(){
	var connLabel=$('.container > .workbench > .header > .topbar > .items>li[conn]');
	
	function getCookie(sName)
	{
		var aCookie = document.cookie.split("; ");
		for (var i=0; i < aCookie.length; i++)
		{
		var aCrumb = aCookie[i].split("=");
		if (sName == aCrumb[0])
		return unescape(aCrumb[1]);
		}
		return null;
	}
	
	function onmessage(frame){
		if(frame.isFrame){
			//console.log('...onmessage--是否侦:'+frame.isFrame+'; '+frame.heads.command+' '+frame.heads.url+' '+frame.heads.protocol);
			var cnt=$.parseJSON(frame.content);
			frame.content=cnt;
			if('notify'==frame.heads.command){
				if(frame.heads.url.indexOf('/node/online.service')>-1){
					var item=$('.container > .workbench > .desktop > .column .column-left > .proj-region > .pr-tree > .pr-folders > .pr-folder > .pr-objs > .pr-obj > .pr-methods > .pr-method['+'uuid=\"'+frame.content.uuid+'\"][path=\"'+frame.content.path+'\"]');
					item.find('.method-command').attr('src','img/running.svg');
					$('.container>.workbench>.desktop>.column  .layout-main .column-context >.main-column-lets>.portlet[position]>img').attr('src','img/running.svg');
					var lets=$('.container>.workbench>.desktop>.column  .layout-main .column-context >.main-column-lets');
					var str='>.portlet[position][uuid=\"'+frame.content.uuid+'\"][path=\"'+frame.content.path+'\"]>img';
					lets.find(str).attr('src','img/running.svg');
					return;
				}
				if(cnt!=null&&frame.heads.url.indexOf('/node/offline.service')>-1){
					var item=$('.container > .workbench > .desktop > .column .column-left > .proj-region > .pr-tree > .pr-folders > .pr-folder > .pr-objs > .pr-obj > .pr-methods > .pr-method['+'uuid=\"'+frame.content.uuid+'\"][path=\"'+frame.content.path+'\"]');
					item.find('.method-command').attr('src','img/stoped.svg');
					var lets=$('.container>.workbench>.desktop>.column  .layout-main .column-context >.main-column-lets');
					var str='>.portlet[position][uuid=\"'+frame.content.uuid+'\"][path=\"'+frame.content.path+'\"]>img';
					lets.find(str).attr('src','img/stoped.svg');
					return;
				}
			}
			return;
		}
//		console.log('...onmessage--是否侦:'+frame.isFrame+' 内容：'+frame.content);
	}
	function onopen(e){
		connLabel.html('已连接');
		connLabel.attr('state','isopen');
		var cjtoken=getCookie('cjtoken');
		var frame={
				heads:{
					url:'/mic/public/online.service',
					command:'get',
					protocol:'ws/1.0'
				},
				params:{
					token:cjtoken
				}
			};
		connLabel.ws.sendFrame(frame);//发送验证
	}
	function onclose(e){
		connLabel.html('未连接');
		connLabel.attr('state','isclose');
	}
	function onerror(e){
		alert('ws error:'+e);
	}
	var wsServiceuri=connLabel.attr('wsurl');
	var ws=$.ws.open(wsServiceuri,onmessage, onopen, onclose,onerror);
	connLabel.ws=ws;
	
});