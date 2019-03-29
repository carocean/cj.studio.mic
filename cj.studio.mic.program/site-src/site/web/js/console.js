$(document).ready(function(){
	var result=$('#console_result');
	var inputCmd=$('#input_cmd');
	inputCmd.on('keyup',function(e){
		if(e.keyCode!=13){
			return;
		}
		var val=$(this).val();
		$(this).val('');
		var li=result.find('>li.cmd_pair').first().clone();
		li.removeAttr('style');
		li.find('.cmd_line .cmd_text').html(val);
		result.append(li);
		
		if(!connector.isopen){
			alert('连接未打开')
			return;
		}
		
		var pos=$('.container > .workbench > .desktop > .column .layout-main .column-context > .main-column-lets > .portlet[position]');
		var path=pos.attr('path');
		var uuid=pos.attr('uuid');
		
		var cjtoken=getCookie('cjtoken');
		var frame={
				heads:{
					url:'/mic/views/cmd.service',
					command:'exe',
					protocol:'ws/1.0'
				},
				params:{
					cjtoken:cjtoken,
					uuid:uuid,
					path:path,
					cmdline:val
				}
			};
		connector.ws.sendFrame(frame);//发送验证
	})
});