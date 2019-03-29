$(document).ready(function(){
	var mask=$('.container > .workbench > .desktop > .mask');
	mask.on('click',function(){
		var onMasks=$('.container > .workbench > .desktop > .column .layout-main .column-context .on-mask');
		onMasks.hide();
		$(this).hide();
	})
	
	var pannelTools=$('.container > .workbench > .desktop > .column .layout-main .column-context > .panel-tools');
	var addFolder=$('.container > .workbench > .desktop > .column .column-left > .proj-region > .pr-toolbar li[addFolder]');
	addFolder.on('click',function(){
		if(!pannelTools.is('on')){
			if(!mask.is('on')){
				mask.show();
			}
			var template=$(this).parents('ul').siblings('.popup-template');
			var cjtoken=getCookie('cjtoken');
			$.get('./views/folder-editor.service',{cjtoken:cjtoken},function(data){
				var ul=template.find('.popup-rbar-table').first();
				var cli=ul.find('>li').first().clone();
				var folders=$.parseJSON(data);
				if(folders.length==0){
					ul.empty();
					ul.append(cli.clone());
					var html=template.html();
					pannelTools.html(html);
					pannelTools.show();
					return;
				}
				ul.empty();
				for(var i=0;i<folders.length;i++){
					var folder=folders[i];
					var li=cli.clone();
					li.attr('code',folder.code);
					li.attr('path',folder.path);
					li.find('.popup-rbar-cell.code>input').attr('value',folder.code);
					li.find('.popup-rbar-cell.code>input').attr('readonly','readonly');
					li.find('.popup-rbar-cell.code>input').attr('disabled','disabled');
					li.find('.popup-rbar-cell.name>input').attr('value',folder.name==null?'':folder.name);
					ul.append(li);
				}
				var html=template.html();
				pannelTools.html(html);
				pannelTools.show();
			});
		}
	});
	var columnContext=$('.container > .workbench > .desktop > .column .layout-main .column-context');
	var prRegion=$('.container>.workbench>.desktop>.column  .column-left>.proj-region');
	prRegion.undelegate('>.pr-tree>.pr-folders>.pr-folder .popup-arrow','click');
	prRegion.delegate('>.pr-tree>.pr-folders>.pr-folder .popup-arrow','click',function(e){
		e.stopPropagation();
		e.preventDefault();
		if(!pannelTools.is('on')){
			var arrow=$(this).attr('arrow');
			switch(arrow){
			case 'folder':
				if(!mask.is('on')){
					mask.show();
				}
				var template=$(this).parents('.pr-tree').find('>.popup-template');
				var folder=$(this).parents('.pr-folder').attr('code');
				var path=$(this).parents('.pr-folder').attr('path');
				pannelTools.attr('folder',folder);
				pannelTools.attr('path',path);
				var titlebar=template.find('.popup-bar-left>span');
				titlebar.html(folder);
				$(this).siblings('.folder-main').trigger('click');
				$.get('./views/folder-get.service',{path:path+folder},function(data){
					var ul=template.find('.popup-rbar-table').first();
					var cli=ul.find('>li').first().clone();
					var services=$.parseJSON(data);
					if(services.length==0){
						ul.empty();
						ul.append(cli.clone());
						var html=template.html();
						pannelTools.html(html);
						pannelTools.show();
						return;
					}
					ul.empty();
					for(var i=0;i<services.length;i++){
						var service=services[i];
						var li=cli.clone();
						li.attr('path',service.path);
						li.attr('code',service.code);
						li.find('.popup-rbar-cell.code>input').attr('readonly','readonly');
						li.find('.popup-rbar-cell.code>input').attr('disabled','disabled');
						li.find('.popup-rbar-cell.code>input').attr('value',service.code);
						li.find('.popup-rbar-cell.name>input').attr('value',service.name==null?'':service.name);
						ul.append(li);
					}
					var html=template.html();
					pannelTools.html(html);
					pannelTools.show();
				});
				break;
			}
			
		}
	});
	prRegion.undelegate('>.pr-tree>.pr-folders>.pr-folder>.folder-main','click');
	prRegion.delegate('>.pr-tree>.pr-folders>.pr-folder>.folder-main','click',function(){
		var arrow=$(this).find('.folder-arrow');
		var isdown=arrow.attr('isdown');
		var objPad=$(this).siblings('.pr-objs');
		if(isdown=='false'){// 换成向下
			arrow.attr('src','img/arrow-down.svg');
			objPad.show();
			arrow.attr('isdown','true');
		}else{
			arrow.attr('src','img/arrow.svg');
			objPad.hide();
			arrow.attr('isdown','false');
		}
		prRegion.find('.selected').removeClass('selected');
		$(this).addClass('selected');
	});
	prRegion.undelegate('>.pr-tree > .pr-folders > .pr-folder > .pr-objs > .pr-obj>.obj-main','click');
	prRegion.delegate('>.pr-tree > .pr-folders > .pr-folder > .pr-objs > .pr-obj>.obj-main','click',function(){
		var arrow=$(this).find('.obj-arrow');
		var isdown=arrow.attr('isdown');
		var methodPad=$(this).siblings('.pr-methods');
		if(isdown=='false'){// 换成向下
			arrow.attr('src','img/arrow-down.svg');
			methodPad.show();
			arrow.attr('isdown','true');
		}else{
			arrow.attr('src','img/arrow.svg');
			methodPad.hide();
			arrow.attr('isdown','false');
		}
		prRegion.find('.selected').removeClass('selected');
		$(this).addClass('selected');
	});
	prRegion.undelegate('>.pr-tree > .pr-folders > .pr-folder > .pr-objs > .pr-obj > .pr-methods > .pr-method > .method-main','click');
	prRegion.delegate('>.pr-tree > .pr-folders > .pr-folder > .pr-objs > .pr-obj > .pr-methods > .pr-method > .method-main','click',function(){
		$('.main-column-lets').show();
		prRegion.find('.selected').removeClass('selected');
		$(this).addClass('selected');
		var pos=$('.container > .workbench > .desktop > .column .layout-main .column-context > .main-column-lets > .portlet[position]');
		var li=$(this).parents('li.pr-method');
		pos.find('span[path]').html(li.attr('path'));
		pos.find('span[title]').html(li.find('.method-code').html());
		pos.find('span[title]').attr('title',li.attr('title'));
		pos.attr('path',li.attr('path'));
		pos.attr('uuid',li.attr('uuid'));
		pos.find('img').attr('src',li.find('.method-command').attr('src'));
	});
	var tabPanels=$('.portlet .settings>.tab-panels');
	var tabs=$('.portlet .settings > .tabs');
	tabs.children('.tab').on('click',function(){
		tabPanels.find('>.tab-panel').hide();
		tabs.find('.selected').removeClass('selected');
		$(this).addClass('selected');
		var kind=$(this).attr('kind');
		switch(kind){
		case 'parameter':
			tabPanels.find('>.parameter').show();
			break;
		case 'header':
			tabPanels.find('>.header').show();
			break;
		case 'content':
			tabPanels.find('>.content').show();
			break;
		}
	});
	var cntTabPanels=$('.portlet .settings > .tab-panels > .tab-panel.content>.tab-panels');
	var cntRadios=$('.portlet .settings > .tab-panels > .tab-panel.content .radios input');
	var selectedRadio=$('.portlet .settings > .tab-panels > .tab-panel.content .radios input:radio:checked');
	var onSeletedRaio=function(the){
		var mid=$('.container > .workbench > .desktop > .column .layout-main .column-context > .main-column-lets > .portlet[position]').attr('mid');
		cntTabPanels.find('.tab-panel').hide();
		var kind=the.siblings('label').html();
		switch(kind){
		case 'None':
			cntTabPanels.find('>.None').show();
			break;
		case 'Any':
			$.get('./views/request-content-any-get.service',{mid:mid},function(data){
				var obj=$.parseJSON(data);
				var textarea=$('.portlet .settings > .tab-panels > .tab-panel.content > .tab-panels > .tab-panel.Any > textarea');
				if(obj==null){
					textarea.val('');
					return;
				}
				textarea.val(obj.content);
			}).error(function(e){
				alert(e.responseText);
			});
			cntTabPanels.find('>.Any').show();
			break;
		case 'x-www-form-urlencode':
			cntTabPanels.find('>.x-www-form-urlencode').show();
			$.get('./views/request-content-xwww-get.service',{mid:mid},function(data){
				var ul=$('.portlet .settings > .tab-panels .tab-panel.x-www-form-urlencode .tab-table-ul').first();
				var cli=ul.children('.tab-table-row.t-content').first().clone();
				cli.find('input:text').val('');
				cli.removeAttr('id');
				ul.empty();
				var parameters=$.parseJSON(data);
				if(parameters.length==0){
					ul.append(cli.clone());
					return;
				}
				for(var i=0;i<parameters.length;i++){
					var p=parameters[i];
					var li=cli.clone();
					li.attr('id',p.id);
					li.find('.tab-table-cell.key input:text').val(p.key);
					li.find('.tab-table-cell.value input:text').val(p.value);
					li.find('.tab-table-cell.desc input:text').val(p.desc);
					ul.append(li);
				}
				ul.append(cli.clone());
			}).error(function(e){
				alert(e.responseText);
			});
			break;
		case 'Form-Data':
			cntTabPanels.find('>.Form-Data').show();
			$.get('./views/request-content-formdata-get.service',{mid:mid},function(data){
				var ul=$('.portlet .settings > .tab-panels .tab-panel.Form-Data .tab-table-ul').first();
				var cli=ul.children('.tab-table-row.t-content').first().clone();
				cli.find('input:text').val('');
				cli.removeAttr('id');
				ul.empty();
				var parameters=$.parseJSON(data);
				if(parameters.length==0){
					ul.append(cli.clone());
					return;
				}
				for(var i=0;i<parameters.length;i++){
					var p=parameters[i];
					var li=cli.clone();
					li.attr('id',p.id);
					li.find('.tab-table-cell.key input:text').val(p.key);
					li.find('.tab-table-cell.value input:text').val(p.value);
					li.find('.tab-table-cell.desc input:text').val(p.desc);
					ul.append(li);
				}
				ul.append(cli.clone());
			}).error(function(e){
				alert(e.responseText);
			});
			break;
		}
	}
	onSeletedRaio(selectedRadio);
	cntRadios.on('change',function(){
		onSeletedRaio($(this));
	});
	
	tabPanels.undelegate('.tab-table-row','mouseenter');
	tabPanels.delegate('.tab-table-row','mouseenter',function(){
		$(this).find('.op').attr('style','display:table-cell;');
	})
	tabPanels.undelegate('.tab-table-row','mouseleave');
	tabPanels.delegate('.tab-table-row','mouseleave',function(){
		$(this).find('.op').hide();
	})
	cntTabPanels.undelegate('.tab-table-row','mouseenter');
	cntTabPanels.delegate('.tab-table-row','mouseenter',function(){
		$(this).find('.op').attr('style','display:table-cell;');
	})
	cntTabPanels.undelegate('.tab-table-row','mouseleave');
	cntTabPanels.delegate('.tab-table-row','mouseleave',function(){
		$(this).find('.op').hide();
	});
	
	var logoutE=$('.container > .workbench > .header > .topbar > .items > li[logout]');
	logoutE.on('click',function(){
		$.get('./public/logout.service',{},function(){
			window.location.href='./';
		});
	});
	var panelTools=$('.panel-tools');
	panelTools.on('refreshPTree',function refreshPTree(){
		$.get('./',{onlyPrintPt:true},function(html){
			$('.pr-tree').html(html);
		}).error(function(e){
			alert(e.responseText);
		})});

});