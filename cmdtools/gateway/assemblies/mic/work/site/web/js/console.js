$(document).ready(function(){
	var result=$('#console_result');
	var inputCmd=$('#input_cmd');
	inputCmd.on('keyup',function(e){
		if(e.keyCode!=13){
			return;
		}
		var val=$(this).val();
		$(this).val('');
		if('reset'==val){
			$('#console_result>.cmd_pair:not(:hidden)').remove();
			return;
		}
		var li=result.find('>li.cmd_pair').first().clone();
		li.removeAttr('style');
		li.find('.cmd_line .cmd_text').html(val);
		li.find('.cmd_result >.response').empty();
		var prefix_val=$('.input_region > span.prefix_val:not(:hidden)');
		if(prefix_val.length!=0){
			li.find('.cmd_line .prefix').append(prefix_val.clone());
		}
		result.append(li);
		
		if(!connector.isopen){
			alert('连接未打开')
			return;
		}
		
		var pos=$('.container > .workbench > .desktop > .column .layout-main .column-context > .main-column-lets > .portlet[position]');
		var path=pos.attr('path');
		var uuid=pos.attr('uuid');
		var cjtoken=getCookie('cjtoken');
		
		if(val!=null&&val.indexOf('site ')==0){
			var file=$('#upload_file');
			file.off('change');
			file.on('change',function(){
				var fpath=$(this).val();
				if(fpath.lastIndexOf('.jar')<0){
					alert('只能上传jar文件');
				}
				$.ajaxFileUpload({
	                fileElementId: 'upload_file',    //需要上传的文件域的ID，即<input type="file">的ID。
	                url: './uploadFile.service', //后台方法的路径
	                type: 'post',   //当要提交自定义参数时，这个参数要设置成post
	                secureuri: false,   //是否启用安全提交，默认为false。
	                async : true,   //是否是异步
	                data:{cjtoken:cjtoken,
						uuid:uuid,
						path:path,
						cmdline:val},
	                success: function(data) {   //提交成功后自动执行的处理函数，参数data就是服务器返回的数据。
	                    console.log('成功上传文件：'+path);
	                },
	                error: function(data, status, e) {  //提交失败自动执行的处理函数。
	                    alert(e);
	                }
	            });
			})
			file.trigger('click');
			return;
		}
		if(val!=null&&val.indexOf('plugin ')==0){
			var file=$('#upload_file');
			file.off('change');
			file.on('change',function(){
				var fpath=$(this).val();
				if(fpath.lastIndexOf('.jar')<0){
					alert('只能上传jar文件');
				}
				$.ajaxFileUpload({
	                fileElementId: 'upload_file',    //需要上传的文件域的ID，即<input type="file">的ID。
	                url: './uploadFile.service', //后台方法的路径
	                type: 'post',   //当要提交自定义参数时，这个参数要设置成post
	                secureuri: false,   //是否启用安全提交，默认为false。
	                async : true,   //是否是异步
	                data:{cjtoken:cjtoken,
						uuid:uuid,
						path:path,
						cmdline:val},
	                success: function(data) {   //提交成功后自动执行的处理函数，参数data就是服务器返回的数据。
	                	console.log('成功上传文件：'+path);
	                },
	                error: function(data, status, e) {  //提交失败自动执行的处理函数。
	                    console.error(e);
	                    alert(e);
	                }
	            });
			})
			file.trigger('click');
			return;
		}
		
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