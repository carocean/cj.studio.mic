$(document).ready(function(){
	var result=$('#console_result');
	var inputCmd=$('#input_cmd');
	inputCmd.on('keyup',function(e){
		if(e.keyCode!=13){
			return;
		}
		var val=$(this).val();
		var input_result=$('#input_result').html();
		$(this).val('');
		var li=result.find('>li.cmd_pair').first().clone();
		li.removeAttr('style');
		li.find('.cmd_line .cmd_text').html(val);
		li.find('.cmd_result').html(input_result);
		result.append(li);
	})
});