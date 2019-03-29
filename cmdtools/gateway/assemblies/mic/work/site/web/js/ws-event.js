var connector={
		isopen:false,
		ws:{}
}
$(document).ready(function(){
	var connLabel=$('.container > .workbench > .header > .topbar > .items>li[conn]');
	
	function onmessage(frame){
		if(frame.isFrame){
			//console.log('...onmessage--是否侦:'+frame.isFrame+'; '+frame.heads.command+' '+frame.heads.url+' '+frame.heads.protocol);
			var cntstr=frame.content;
			var cnt;
			if(typeof cntstr=='undefined'||cntstr==''||cntstr==null){
				cnt={};
			}else{
				cnt=$.parseJSON(frame.content);
			}
			frame.content=cnt;
			if('notify'==frame.heads.command){
				if(frame.heads.url.indexOf('/node/online.service')>-1){
					var item=$('.container > .workbench > .desktop > .column .column-left > .proj-region > .pr-tree > .pr-folders > .pr-folder > .pr-objs > .pr-obj > .pr-methods > .pr-method['+'uuid=\"'+frame.content.uuid+'\"][path=\"'+frame.content.path+'\"]');
					if(item.length==0){
						showNode(frame);
						return;
					}
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
				if(frame.heads.url.indexOf('/node/response.service')>-1){
					var responseText=frame.content.response;
					$('#console_result > li.cmd_pair:last-child>.cmd_result>.response').html(responseText);
					return;
				}
				if(frame.heads.url.indexOf('/node/onremoved.service')>-1){
					var selected=$('.container > .workbench > .desktop > .column .column-left > .proj-region > .pr-tree > .pr-folders > .pr-folder > .pr-objs > .pr-obj > .pr-methods > .pr-method[uuid=\"'+frame.params.uuid+'\"]');
					selected.remove();
					var lets=$('.container > .workbench > .desktop > .column .layout-main .column-context > .main-column-lets');
					if(lets.find('.portlet[position][uuid=\"'+frame.params.uuid+'\"]').length>0){
						lets.hide();
					}
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
					cjtoken:cjtoken
				}
			};
		connLabel.ws.sendFrame(frame);//发送验证
		connector.isopen=true;
	}
	function onclose(e){
		connLabel.html('未连接');
		connLabel.attr('state','isclose');
		connector.isopen=false;
		window.location.href='./';
	}
	function onerror(e){
		console.log(e);
		alert('ws error:'+e);
	}
	var wsServiceuri=connLabel.attr('wsurl');
	var ws=$.ws.open(wsServiceuri,onmessage, onopen, onclose,onerror);
	connLabel.ws=ws;
	connector.ws=ws;
	
	function showNode(frame){
		var npath=frame.content.path;
		while(npath.lastIndexOf('/')==npath.length-1){
			npath=npath.substring(0,npath.length-1);
		}
		var index=npath.lastIndexOf('/');
		var parentPath=npath.substring(0,index);
		var folder=npath.substring(index+1,npath.length);
		var temp=$('.pr-template').clone();
		temp.find('.pr-method').attr('uuid',frame.content.uuid);
		temp.find('.pr-method').attr('path',frame.content.path);
		temp.find('.method-code').attr('title',frame.content.desc);
		temp.find('.method-code').html(frame.content.title);
		temp.find('.method-command').attr('src','img/running.svg');
		var postion=$('.container > .workbench > .desktop > .column .column-left > .proj-region > .pr-tree > .pr-folders > .pr-folder > .pr-objs > .pr-obj[code=\"'+folder+'\"][path=\"'+parentPath+'\"] .pr-methods');
		postion.html(temp.html());
	}
});