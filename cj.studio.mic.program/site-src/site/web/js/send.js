$(document).ready(function(){
	var connector=$('.portlet .req-url li.state');
	connector.attr('state','isclose');
	function onmessage(frame){
		var content='';
		var responsePanel=$('.portlet > .response > .content');
		if(frame.isFrame){
//			console.log('...onmessage--是否侦:'+frame.isFrame+'; '+frame.heads.command+' '+frame.heads.url+' '+frame.heads.protocol);
			content=frame.content.replace(/\\\"/g,'"');
			responsePanel.html(content);
			return;
		}
		content=frame.content;
		responsePanel.html(content);
//		console.log('...onmessage--是否侦:'+frame.isFrame+' 内容：'+frame.content);
	}
	function onopen(e){
		connector.find('label').html('已连接');
		connector.attr('state','isopen');
	}
	function onclose(e){
		connector.find('label').html('未连接');
		connector.attr('state','isclose');
	}
	function onerror(e){
		alert('ws error:'+e);
	}
	
	connector.on('click',function(){
		if(connector.attr('state')=='isopen'){
			connector.ws.close();
		}else{
			var host=$(this).parents('.req-url').find('li.url input').val();
			var ws=$.ws.open(host,onmessage, onopen, onclose,onerror);
			connector.ws=ws;
		}
	})
	var sender=$('.portlet .headline > ul > li.send');
	sender.on('click',function(){
		var mid=$('.container > .workbench > .desktop > .column .layout-main .column-context > .main-column-lets > .portlet[position]').attr('mid');
		
		var netptCheckedValue=$('.portlet .net-protocols > ul > li input:radio:checked').attr('id');
		if('wsOnBrowser'!=netptCheckedValue){
			//服务器端是异步回路，作业处理完之后通过全局的ws将响应推送回来显示
			$.get('./views/onserver-job-add.service',{mid:mid},function(data){
				
			}).error(function(e){
				alert(e.responseText);
			})
			return;
		}
		//以下是处理wsOnBrowser
		if(typeof connector.ws=='undefined'||connector.attr('state')=='isclose'){
			alert('请先点击连接按钮');
			return;
		}
		$.get('./views/request-frame-get.service',{mid:mid},function(data){
			var obj=$.parseJSON(data);
			obj.frame=$.ws.toFrame(obj.frameRaw);
			if((typeof obj.frame.content!='undefined')&&obj.frame.content.indexOf('\"')>-1){
				obj.frame.contentText=obj.frame.content;
				obj.frame.content=$.parseJSON(obj.frame.content.replace(/\\\"/g,'"'));
			}
			delete obj.frameRaw;
			debugger;
			var url=obj.frame.heads.url;

			//			connector.ws.sendText('command=put\r\nurl='+url+'\r\nprotocol=ws/1.0\r\n\r\nname=cj\r\n\r\n我是中国人')
			
			var frame={heads:{},params:{}};
			
			for(var head in obj.frame.heads){
				frame.heads[head]=obj.frame.heads[head];
			}
			for(var p in obj.frame.params){
				frame.params[p]=obj.frame.params[p];
			}
			frame.content=obj.frame.contentText;
			for(var i=0;i<1;i++){
				connector.ws.sendFrame(frame);
			}
		}).error(function(e){
			alert(e.responseText);
		});
	})
});