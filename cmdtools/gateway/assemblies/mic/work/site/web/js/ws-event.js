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
					var prFolder=item.parents('.pr-folder');
					var parent=prFolder.attr('path');
					var code=prFolder.attr('code');
					var fullPath=parent+code;
					$.get('./views/getNodeCount.service',{path:fullPath},function(data){
						var countE=prFolder.find('.folder-main>.folder-title>.folder-count>span');
						countE.html(data);
					}).error(function(e){
						alert(e.responseText);
					});
					item.find('.method-command').attr('src','img/running.svg');
					$('.container>.workbench>.desktop>.column  .layout-main .column-context >.main-column-lets>.portlet[position]>img').attr('src','img/running.svg');
					var lets=$('.container>.workbench>.desktop>.column  .layout-main .column-context >.main-column-lets');
					var str='>.portlet[position][uuid=\"'+frame.content.uuid+'\"][path=\"'+frame.content.path+'\"]>img';
					lets.find(str).attr('src','img/running.svg');
					return;
				}
				if(cnt!=null&&frame.heads.url.indexOf('/node/offline.service')>-1){
					var item=$('.container > .workbench > .desktop > .column .column-left > .proj-region > .pr-tree > .pr-folders > .pr-folder > .pr-objs > .pr-obj > .pr-methods > .pr-method['+'uuid=\"'+frame.content.uuid+'\"][path=\"'+frame.content.path+'\"]');
					var prFolder=item.parents('.pr-folder');
					var parent=prFolder.attr('path');
					var code=prFolder.attr('code');
					var fullPath=parent+code;
					$.get('./views/getNodeCount.service',{path:fullPath},function(data){
						var countE=prFolder.find('.folder-main>.folder-title>.folder-count>span');
						countE.html(data);
					}).error(function(e){
						alert(e.responseText);
					});
					item.find('.method-command').attr('src','img/stoped.svg');
					var lets=$('.container>.workbench>.desktop>.column  .layout-main .column-context >.main-column-lets');
					var str='>.portlet[position][uuid=\"'+frame.content.uuid+'\"][path=\"'+frame.content.path+'\"]>img';
					lets.find(str).attr('src','img/stoped.svg');
					return;
				}
				if(frame.heads.url.indexOf('/node/response.service')>-1){
					var responseText=frame.content.response;
					if(typeof responseText!='undefined'&&responseText!=null&&responseText.indexOf('$prefix{')==0){
						var prefix=responseText.substring('$prefix{'.length,responseText.length);
						prefix=prefix.substring(0,prefix.lastIndexOf('}'));
						var pv=$('.container > .workbench > .desktop > .column .layout-main .column-context > .main-column-lets > .portlet[console] > div > span.prefix_val');
						if('$'==prefix){
							pv.hide();
						}else{
							pv.html(prefix);
							pv.attr('style','display:table-cell;');
						}
						return;
					}
					if(typeof responseText!='undefined'&&responseText!=null&&responseText.indexOf('$openports{')==0){
						var openports=responseText.substring('$openports{'.length,responseText.length);
						openports=openports.substring(0,openports.lastIndexOf('}'));
						var list=JSON.parse(openports);
						var olistUl=$('#openports_list').first();
						var oli=olistUl.find('li').first().clone();
						olistUl.empty();
						for(var i=0;i<list.length;i++){
							var ports=list[i];
							var li=oli.clone();
							var href=li.find('a');
							href.attr('href',ports);
							li.prepend('-&nbsp;&nbsp;');
							href.html(ports);
							olistUl.append(li);
						}
						if (list.length == 0) {
							var li=oli.clone();
							var href=li.find('a');
							href.html('-- 无 --')
						}
						return;
					}
					$('#console_result > li.cmd_pair:last-child>.cmd_result>.response').html(responseText);
					$('html,body').scrollTop($('html,body')[0].scrollHeight);
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
				if(frame.heads.url.indexOf('/node/onCDConsole.service')>-1){
					var consoleName=frame.params.consoleName;
					if('$'==consoleName){
						return;
					}
					$('.container > .workbench > .desktop > .column .layout-main .column-context > .main-column-lets > .portlet[console] > div > span.prefix_val').html(consoleName);
					$('.container > .workbench > .desktop > .column .layout-main .column-context > .main-column-lets > .portlet[console] > div > span.prefix_val').attr('style','display:table-cell;');
					return;
				}
				if(frame.heads.url.indexOf('/node/onByeConsole.service')>-1){
					var consoleName=frame.params.consoleName;
					if('$'==consoleName){
						$('.container > .workbench > .desktop > .column .layout-main .column-context > .main-column-lets > .portlet[console] > div > span.prefix_val').hide();
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
		postion.append(temp.html());

		var prFolder=postion.parents('.pr-folder');
		var parent=prFolder.attr('path');
		var code=prFolder.attr('code');
		var fullPath=parent+code;
		$.get('./views/getNodeCount.service',{path:fullPath},function(data){
			var countE=prFolder.find('.folder-main>.folder-title>.folder-count>span');
			countE.html(data);
		}).error(function(e){
			alert(e.responseText);
		});
	}
});