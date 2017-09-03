
/*$(document).ready(function(){
	var url="/cfs/logs/cfs_logs";
	$.ajax({
		async:false,
		cache:false,
		contentType:'application/json',
		url:url,
		dataType:'json',
		type:'get',
		error:function(e){
			alert("请求失败");
		},
		success:function(data){
			var json=eval(data);
			page=json.page;//当前的页数
			limit=json.limit;//每页的记录数
			totalCount=json.total;//总记录数
			currentPageCount=json.documents.length;//当前页的记录数
			
			
			 * <tr style="background:#CCEEFF" id=logid><td>log.ID</td><td>全流程信贷</td><td></td><td></td><td></td><td>QLCXD2016051100000005</td><td>(总行)哈尔滨银行总行</td><td></td><td>2016-05-23</td><td>扫描</td></tr>
			 
			
			for(var i=0;i<currentPageCount;i++){
				//每次新建tr标签的id
				var logid='log'+i;
				var logDTO=json.documents[i];
				
				var BIS_SYS_CODE=logDTO.BIS_SYS_CODE;
				var BIS_FUN_CODE=logDTO.BIS_FUN_CODE;
				var IMG_NAME=logDTO.IMG_NAME;
				var ORG_CODE=logDTO.ORG_CODE;
				var OPERATE_PERSON=logDTO.OPERATE_PERSON;
				var FLW_CODE=logDTO.FLW_CODE;
				var CUSTOM_CODE=logDTO.CUSTOM_CODE;
				var OPERATE_DATE=logDTO.OPERATE_DATE;
				var BATCH_CODE=logDTO.BATCH_CODE;
				//序列代码列
				var ROW_NUM=page*limit+i+1;
				//为空的情况，设置为“”
				if(BIS_SYS_CODE==null||BIS_SYS_CODE==""){
					BIS_SYS_CODE="";
					}
					if(BIS_FUN_CODE==null||BIS_FUN_CODE==""){
					BIS_FUN_CODE="";
					}
					if(IMG_NAME==null||IMG_NAME==""){
					IMG_NAME="";
					}
					if(ORG_CODE==null||ORG_CODE==""){
					ORG_CODE="";
					}
					if(OPERATE_PERSON==null||OPERATE_PERSON==""){
					OPERATE_PERSON="";
					}
					if(FLW_CODE==null||FLW_CODE==""){
					FLW_CODE="";
					}
					if(CUSTOM_CODE==null||CUSTOM_CODE==""){
					CUSTOM_CODE="";
					}
					if(OPERATE_DATE==null||OPERATE_DATE==""){
					OPERATE_DATE="";
					}
					if(BATCH_CODE==null||BATCH_CODE==""){
					BATCH_CODE="";
					}
			//拼接字符串，用于插入到页面中，遍历	
			var insert="<tr style='background:#CCEEFF' id='"+logid+"'><td>"+ROW_NUM+"</td><td>"+BIS_SYS_CODE+"</td><td>"+CUSTOM_CODE+"</td><td>"+FLW_CODE+"</td><td>"+IMG_NAME+"</td><td>"+BATCH_CODE+"</td><td>"+ORG_CODE+"</td><td>"+OPERATE_PERSON+"</td><td>"+OPERATE_DATE+"</td><td>扫描</td></tr>";
				//alert(insert);
				var choose="tr:eq("+(5+i)+")";
			//$(choose).after(insert);
			$(choose).after(insert);
			}
			
		
			
		}
	
	});
	
	
	
});*/






function selectCFSLog(){
	$("#pageIndex").text("0");
	$("#totalPageCount").text("1");
	$("#totalCount").text("0");
	
	
	var BIS_SYS_CODE=$.trim(document.getElementById("BIS_SYS_CODE").value);
	var ORG_CODE=$.trim(document.getElementById("ORG_CODE").value);
	var OPERATE_PERSON=$.trim(document.getElementById("OPERATE_PERSON").value);
	var BATCH_CODE=$.trim(document.getElementById("BATCH_CODE").value);
	var CUSTOM_CODE=$.trim(document.getElementById("CUSTOM_CODE").value);
	var FLW_CODE=$.trim(document.getElementById("FLW_CODE").value);
	var IMG_NAME=$.trim(document.getElementById("IMG_NAME").value);
	var OPERATE_DATE=$.trim(document.getElementById("OPERATE_DATE").value);
	//var page=document.getElementById("page").value;
	var page="0";
	
	//alert(page);
	$("#after").removeAttr('disabled');
	$("#last").removeAttr('disabled');
	$("#first").attr({'disabled':'disabled'});
	$("#before").attr({'disabled':'disabled'});
	
		/*if(BIS_SYS_CODE==null||BIS_SYS_CODE==""){
		BIS_SYS_CODE="";
		}
		
		if(IMG_NAME==null||IMG_NAME==""){
		IMG_NAME="";
		}
		if(ORG_CODE==null||ORG_CODE==""){
		ORG_CODE="";
		}
		if(OPERATE_PERSON==null||OPERATE_PERSON==""){
		OPERATE_PERSON="";
		}
		if(FLW_CODE==null||FLW_CODE==""){
		FLW_CODE="";
		}
		if(CUSTOM_CODE==null||CUSTOM_CODE==""){
		CUSTOM_CODE="";
		}
		if(OPERATE_DATE==null||OPERATE_DATE==""){
		OPERATE_DATE="";
		}
		if(BATCH_CODE==null||BATCH_CODE==""){
		BATCH_CODE="";
		}*/
	
	
	var condition={
			"BIS_SYS_CODE":BIS_SYS_CODE,
			"ORG_CODE":ORG_CODE,
			"OPERATE_PERSON":OPERATE_PERSON,
			"BATCH_CODE":BATCH_CODE,
			"CUSTOM_CODE":CUSTOM_CODE,
			"FLW_CODE":FLW_CODE,
			"IMG_NAME":IMG_NAME,
			"OPERATE_DATE":OPERATE_DATE,
			"page":page
	}
	var url="/cfs/logs/cfs_logs";
	$.ajax({
		
		async:false,
		cache:false,
		contentType:'application/json',
		url:url,
		dataType:'json',
		type:'get',
		data:condition,    
		error:function(e){
			alert("请求失败");
		},
		success:function(data){
			$("#insertLog").empty();
			
			var wind=document.body.clientWidth;

			var wid=wind-50;
			var td_wid1=wid*95/1490;
			var td_wid2=wid*170/1490;
			var td_wid3=wid*200/1490;
			var td_wid4=wid*250/1490;
			
			
//			var title="<tr class='back' align='center'><td width='95'>序列代码</td><td width='95'>系统号</td><td width='140'>客户号</td><td width='130'>业务号</td><td width='200'>影像名称</td><td width='130'>机构名称</td><td width='130'>用户名称</td><td width='130'>操作时间</td><td width='130'>备注</td></tr>";
//			var title="<tr class='back' align='center'><td width='95'><div class='code_div'>序列代码<div></td><td width='95'><div class='code_div'>系统号</div></td><td width='170'><div class='content_div'>客户号</div></td><td width='170'><div class='content_div'>业务号</div></td><td width='250'><div class='content2_div'>影像名称</div></td><td width='170'><div class='content_div'>机构名称</div></td><td width='170'><div class='content_div'>用户名称</div></td><td width='170'><div class='content_div'>操作时间</div></td><td width='200'><div class='content3_div'>备注</div></td></tr>";
			var title="<tr class='back' align='center'><td width='"+td_wid1+"'><div class='code_div'>序列代码<div></td><td width='"+td_wid1+"'><div class='code_div'>系统号</div></td><td width='"+td_wid2+"'><div class='content_div'>客户号</div></td><td width='"+td_wid2+"'><div class='content_div'>业务号</div></td><td width='"+td_wid4+"'><div class='content2_div'>影像名称</div></td><td width='"+td_wid1+"'><div class='content_div'>机构名称</div></td><td width='"+td_wid1+"'><div class='content_div'>用户名称</div></td><td width='"+td_wid1+"'><div class='content_div'>操作时间</div></td><td width='"+td_wid3+"'><div class='content3_div'>备注</div></td></tr>";
			$("#insertLog").append(title);
			var json=eval(data);
			//alert(json.hits.hits.length);
			page2=json.page;//当前的页数
			limit=json.limit;//每页的记录数
			total=json.total;//总记录数
			//totalCount=json.total;//总记录数
			//currentPageCount=json.documents.length;//当前页的记录数
			if(parseInt(total)>=20){
				
				currentPageCount=20;//当前页的记录数
			}else{
				currentPageCount=parseInt(total);//当前页的记录数
				
			}
			
			/*
			 * <tr style="background:#CCEEFF" id=logid><td>log.ID</td><td>全流程信贷</td><td></td><td></td><td></td><td>QLCXD2016051100000005</td><td>(总行)哈尔滨银行总行</td><td></td><td>2016-05-23</td><td>扫描</td></tr>
			 */
			
			//alert(totalCount);
			for(var i=0;i<currentPageCount;i++){
				//每次新建tr标签的id
				var logid='log'+i;
				var logDTO=json.documents[i];
				
				var BIS_SYS_CODE=logDTO.BIS_SYS_CODE;
				var BIS_FUN_CODE=logDTO.BIS_FUN_CODE;
				var IMG_NAME=logDTO.IMG_NAME;
				var ORG_CODE=logDTO.ORG_CODE;
				var OPERATE_PERSON=logDTO.OPERATE_PERSON;
				var FLW_CODE=logDTO.FLW_CODE;
				var CUSTOM_CODE=logDTO.CUSTOM_CODE;
				var OPERATE_DATE=logDTO.OPERATE_DATE;
				var BATCH_CODE=logDTO.BATCH_CODE;
				var REMARK=logDTO.REMARK;
				//序列代码列
				var ROW_NUM=page2*limit+i+1;
				//为空的情况，设置为“”
				if(BIS_SYS_CODE==null||BIS_SYS_CODE==""){
					BIS_SYS_CODE="";
					}
					if(BIS_FUN_CODE==null||BIS_FUN_CODE==""){
					BIS_FUN_CODE="";
					}
					if(IMG_NAME==null||IMG_NAME==""){
					IMG_NAME="";
					}
					if(ORG_CODE==null||ORG_CODE==""){
					ORG_CODE="";
					}
					if(OPERATE_PERSON==null||OPERATE_PERSON==""){
					OPERATE_PERSON="";
					}
					if(FLW_CODE==null||FLW_CODE==""){
					FLW_CODE="";
					}
					if(CUSTOM_CODE==null||CUSTOM_CODE==""){
					CUSTOM_CODE="";
					}
					if(OPERATE_DATE==null||OPERATE_DATE==""){
					OPERATE_DATE="";
					}
					if(BATCH_CODE==null||BATCH_CODE==""){
					BATCH_CODE="";
					}
					if(REMARK==null||REMARK==""){
						REMARK="";
					}
			//拼接字符串，用于插入到页面中，遍历	
					//var insert="<tr align='center' style='background:#CCEEFF' id='"+logid+"'><td>"+ROW_NUM+"</td><td>"+BIS_SYS_CODE+"</td><td>"+CUSTOM_CODE+"</td><td>"+FLW_CODE+"</td><td>"+IMG_NAME+"</td><td>"+ORG_CODE+"</td><td>"+OPERATE_PERSON+"</td><td>"+OPERATE_DATE+"</td><td>"+REMARK+"</td></tr>";
					//var title="<tr class='back' align='center'><td width='95'><div class='code_div'>序列代码<div></td><td width='95'><div class='content_div'>系统号</div></td><td width='140'><div class='content_div'>客户号</div></td><td width='130'><div class='content_div'>业务号</div></td><td width='200'><div class='content2_div'>影像名称</div></td><td width='130'><div class='content_div'>机构名称</div></td><td width='130'><div class='content_div'>用户名称</div></td><td width='130'><div class='content_div'>操作时间</div></td><td width='130'><div class='content_div'>备注</div></td></tr>";
					var insert="<tr align='center' style='background:#CCEEFF' id='"+logid+"'><td><div class='code_div'>"+ROW_NUM+"</div></td><td><div title='"+BIS_SYS_CODE+"' class='code_div'>"+BIS_SYS_CODE+"</div></td><td><div title='"+CUSTOM_CODE+"' class='content_div'>"+CUSTOM_CODE+"</div></td><td><div title='"+FLW_CODE+"' class='content_div'>"+FLW_CODE+"</div></td><td><div title='"+IMG_NAME+"' class='content2_div'>"+IMG_NAME+"</div></td><td><div class='content_div' title='"+ORG_CODE+"'>"+ORG_CODE+"</div></td><td><div class='content_div' title='"+OPERATE_PERSON+"'>"+OPERATE_PERSON+"</div></td><td><div class='content_div' title='"+OPERATE_DATE+"'>"+OPERATE_DATE+"</div></td><td><div class='content_div' title='"+REMARK+"'>"+REMARK+"</div></td></tr>";
			//var insert="<tr style='background:#CCEEFF' id='"+logid+"'><td>"+ROW_NUM+"</td><td>"+BIS_SYS_CODE+"</td><td>"+CUSTOM_CODE+"</td><td>"+FLW_CODE+"</td><td>"+IMG_NAME+"</td><td>"+BATCH_CODE+"</td><td>"+ORG_CODE+"</td><td>"+OPERATE_PERSON+"</td><td>"+OPERATE_DATE+"</td><td>扫描</td></tr>";
				//alert(insert);
			//	var choose="tr:eq("+(5+i)+")";
			//$(choose).after(insert);
			//$(choose).after(insert);
			$("#insertLog").append(insert);
			$(".code_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid1});
			$(".content_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid2});
			$(".content2_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid4});
			$(".content3_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid3});
			}//for
			
			$("#totalCount").text(total);
//			$("#totalCount").text(totalCount);
			var totalPageCount;
			if(parseInt(total)%parseInt(limit)==0){//
				totalPageCount=parseInt(total)/parseInt(limit);
				//totalPageCount=parseInt(totalCount)/parseInt(limit);
			}else{
				totalPageCount=Math.ceil(parseInt(total)/parseInt(limit));
//				totalPageCount=Math.ceil(parseInt(totalCount)/parseInt(limit));
			}
			$("#totalPageCount").text(totalPageCount);
			$("#pageIndex").text(parseInt(page)+1);
			if(parseInt(totalPageCount)==1){
					$("#last").attr({'disabled':'disabled'});
	$("#after").attr({'disabled':'disabled'});
			}
		
		}//success
		
		
	});//ajax
		
	
}//function



function jump_to(num1,num2){
	var num;
	if(parseInt(num2)==0){
		num=parseInt(num1)-1;
		if(parseInt(num)<0){
			num=0;
		}
	}
	if(parseInt(num2)==1){
		num=parseInt(num1)-2;
		if(parseInt(num)<0){
			num=0;
		}
	}
	if(parseInt(num2)==2){
		num=parseInt(num1);
	}
	
	
	//alert(num);
	$("#first").removeAttr('disabled');
	$("#before").removeAttr('disabled');
	$("#after").removeAttr('disabled');
	$("#last").removeAttr('disabled');
	
	var totalPageCount=document.getElementById("totalPageCount").innerHTML;
	if(parseInt(totalPageCount)==1){
		$("#first").attr({'disabled':'disabled'});
		$("#before").attr({'disabled':'disabled'});
		$("#last").attr({'disabled':'disabled'});
		$("#after").attr({'disabled':'disabled'});
	}
	var pageCurrent=document.getElementById("page");
	
	if(parseInt(pageCurrent)==0){
		$("#first").attr({'disabled':'disabled'});
$("#before").attr({'disabled':'disabled'});
	}
	if(parseInt(pageCurrent)==parseInt(totalPageCount)-1){
					$("#last").attr({'disabled':'disabled'});
$("#after").attr({'disabled':'disabled'});
	}
	//alert(num);
	var regxp=/^\d+$/;
	if(!regxp.test(num)){
	alert("请输入正确的数字");
	return false;
	}
	//alert(totalPageCount);
	if(parseInt(num)<=0){
		num=0;
		$("#first").attr({'disabled':'disabled'});
		$("#before").attr({'disabled':'disabled'});
	}else if(parseInt(num)+1>=parseInt(totalPageCount)){
		num=totalPageCount-1;
		$("#last").attr({'disabled':'disabled'});
		$("#after").attr({'disabled':'disabled'});
		//alert("aaaa");
	}
	//$("#page").text(num);
	//$("#page").html(num);
	document.getElementById("page").value=num;
	//alert($("#page").text());
	//$("#sub").click();
	//selectCFSLog();
	
	var BIS_SYS_CODE=$.trim(document.getElementById("BIS_SYS_CODE").value);
	var ORG_CODE=$.trim(document.getElementById("ORG_CODE").value);
	var OPERATE_PERSON=$.trim(document.getElementById("OPERATE_PERSON").value);
	var BATCH_CODE=$.trim(document.getElementById("BATCH_CODE").value);
	var CUSTOM_CODE=$.trim(document.getElementById("CUSTOM_CODE").value);
	var FLW_CODE=$.trim(document.getElementById("FLW_CODE").value);
	var IMG_NAME=$.trim(document.getElementById("IMG_NAME").value);
	var OPERATE_DATE=$.trim(document.getElementById("OPERATE_DATE").value);
	/*var BIS_SYS_CODE=document.getElementById("BIS_SYS_CODE").value;
	var ORG_CODE=document.getElementById("ORG_CODE").value;
	var OPERATE_PERSON=document.getElementById("OPERATE_PERSON").value;
	var BATCH_CODE=document.getElementById("BATCH_CODE").value;
	var CUSTOM_CODE=document.getElementById("CUSTOM_CODE").value;
	var FLW_CODE=document.getElementById("FLW_CODE").value;
	var IMG_NAME=document.getElementById("IMG_NAME").value;
	var OPERATE_DATE=document.getElementById("OPERATE_DATE").value;*/
	//var page=$("#page").text();
	var page=document.getElementById("page").value;
	
	
	//alert(page);
	
		/*if(BIS_SYS_CODE==null||BIS_SYS_CODE==""){
		BIS_SYS_CODE="";
		}
		
		if(IMG_NAME==null||IMG_NAME==""){
		IMG_NAME="";
		}
		if(ORG_CODE==null||ORG_CODE==""){
		ORG_CODE="";
		}
		if(OPERATE_PERSON==null||OPERATE_PERSON==""){
		OPERATE_PERSON="";
		}
		if(FLW_CODE==null||FLW_CODE==""){
		FLW_CODE="";
		}
		if(CUSTOM_CODE==null||CUSTOM_CODE==""){
		CUSTOM_CODE="";
		}
		if(OPERATE_DATE==null||OPERATE_DATE==""){
		OPERATE_DATE="";
		}
		if(BATCH_CODE==null||BATCH_CODE==""){
		BATCH_CODE="";
		}*/
	
	
	var condition={
			"BIS_SYS_CODE":BIS_SYS_CODE,
			"ORG_CODE":ORG_CODE,
			"OPERATE_PERSON":OPERATE_PERSON,
			"BATCH_CODE":BATCH_CODE,
			"CUSTOM_CODE":CUSTOM_CODE,
			"FLW_CODE":FLW_CODE,
			"IMG_NAME":IMG_NAME,
			"OPERATE_DATE":OPERATE_DATE,
			"page":page
	}
	var url="/cfs/logs/cfs_logs";
	$.ajax({
		
		async:false,
		cache:false,
		contentType:'application/json',
		url:url,
		dataType:'json',
		type:'get',
		data:condition,    
		error:function(e){
			alert("请求失败");
		},
		success:function(data){
			$("#insertLog").empty();
			var wind=document.body.clientWidth;

			var wid=wind-50;
			var td_wid1=wid*95/1490;
			var td_wid2=wid*170/1490;
			var td_wid3=wid*200/1490;
			var td_wid4=wid*250/1490;
			var title="<tr class='back' align='center'><td width='"+td_wid1+"'><div class='code_div'>序列代码<div></td><td width='"+td_wid1+"'><div class='code_div'>系统号</div></td><td width='"+td_wid2+"'><div class='content_div'>客户号</div></td><td width='"+td_wid2+"'><div class='content_div'>业务号</div></td><td width='"+td_wid4+"'><div class='content2_div'>影像名称</div></td><td width='"+td_wid1+"'><div class='content_div'>机构名称</div></td><td width='"+td_wid1+"'><div class='content_div'>用户名称</div></td><td width='"+td_wid1+"'><div class='content_div'>操作时间</div></td><td width='"+td_wid3+"'><div class='content3_div'>备注</div></td></tr>";
			//var title="<tr class='back' align='center'><td width='95'><div class='code_div'>序列代码<div></td><td width='95'><div class='code_div'>系统号</div></td><td width='170'><div class='content_div'>客户号</div></td><td width='170'><div class='content_div'>业务号</div></td><td width='250'><div class='content2_div'>影像名称</div></td><td width='170'><div class='content_div'>机构名称</div></td><td width='170'><div class='content_div'>用户名称</div></td><td width='170'><div class='content_div'>操作时间</div></td><td width='200'><div class='content3_div'>备注</div></td></tr>";
//			var title="<tr class='back' align='center'><td width='95'><div class='code_div'>序列代码<div></td><td width='95'><div class='content_div'>系统号</div></td><td width='140'><div class='content_div'>客户号</div></td><td width='130'><div class='content_div'>业务号</div></td><td width='200'><div class='content2_div'>影像名称</div></td><td width='130'><div class='content_div'>机构名称</div></td><td width='130'><div class='content_div'>用户名称</div></td><td width='130'><div class='content_div'>操作时间</div></td><td width='130'><div class='content_div'>备注</div></td></tr>";
	//		var title="<tr style='background:#99FFFF' align='center'><td width='95'>序列代码</td><td width='95'>客户号</td><td width='140'>系统号</td><td width='130'>业务号</td><td width='200'>影像名称</td><td width='130'>机构名称</td><td width='130'>用户名称</td><td width='130'>操作时间</td><td width='130'>备注</td></tr>";
			$("#insertLog").append(title);
			var json=eval(data);
			page2=json.page; //当前的页数
			//alert(page2);
			limit=json.limit;//每页的记录数
			total=json.total;//总记录数
		//	totalCount=json.total;//总记录数
			currentPageCount=json.documents.length;//当前页的记录数
			
			/*
			 * <tr style="background:#CCEEFF" id=logid><td>log.ID</td><td>全流程信贷</td><td></td><td></td><td></td><td>QLCXD2016051100000005</td><td>(总行)哈尔滨银行总行</td><td></td><td>2016-05-23</td><td>扫描</td></tr>
			 */
			
			//alert(totalCount);
			for(var i=0;i<currentPageCount;i++){
				//每次新建tr标签的id
				var logid='log'+i;
				var logDTO=json.documents[i];
				
				var BIS_SYS_CODE=logDTO.BIS_SYS_CODE;
				var BIS_FUN_CODE=logDTO.BIS_FUN_CODE;
				var IMG_NAME=logDTO.IMG_NAME;
				var ORG_CODE=logDTO.ORG_CODE;
				var OPERATE_PERSON=logDTO.OPERATE_PERSON;
				var FLW_CODE=logDTO.FLW_CODE;
				var CUSTOM_CODE=logDTO.CUSTOM_CODE;
				var OPERATE_DATE=logDTO.OPERATE_DATE;
				var BATCH_CODE=logDTO.BATCH_CODE;
				var REMARK=logDTO.REMARK;
				//序列代码列
				var ROW_NUM=page2*limit+i+1;
				//为空的情况，设置为“”
				if(BIS_SYS_CODE==null||BIS_SYS_CODE==""){
					BIS_SYS_CODE="";
					}
					if(BIS_FUN_CODE==null||BIS_FUN_CODE==""){
					BIS_FUN_CODE="";
					}
					if(IMG_NAME==null||IMG_NAME==""){
					IMG_NAME="";
					}
					if(ORG_CODE==null||ORG_CODE==""){
					ORG_CODE="";
					}
					if(OPERATE_PERSON==null||OPERATE_PERSON==""){
					OPERATE_PERSON="";
					}
					if(FLW_CODE==null||FLW_CODE==""){
					FLW_CODE="";
					}
					if(CUSTOM_CODE==null||CUSTOM_CODE==""){
					CUSTOM_CODE="";
					}
					if(OPERATE_DATE==null||OPERATE_DATE==""){
					OPERATE_DATE="";
					}
					if(BATCH_CODE==null||BATCH_CODE==""){
					BATCH_CODE="";
					}
			//拼接字符串，用于插入到页面中，遍历	
					var insert="<tr align='center' style='background:#CCEEFF' id='"+logid+"'><td><div class='code_div'>"+ROW_NUM+"</div></td><td><div title='"+BIS_SYS_CODE+"' class='code_div'>"+BIS_SYS_CODE+"</div></td><td><div title='"+CUSTOM_CODE+"' class='content_div'>"+CUSTOM_CODE+"</div></td><td><div title='"+FLW_CODE+"' class='content_div'>"+FLW_CODE+"</div></td><td><div title='"+IMG_NAME+"' class='content2_div'>"+IMG_NAME+"</div></td><td><div class='content_div' title='"+ORG_CODE+"'>"+ORG_CODE+"</div></td><td><div class='content_div' title='"+OPERATE_PERSON+"'>"+OPERATE_PERSON+"</div></td><td><div class='content_div' title='"+OPERATE_DATE+"'>"+OPERATE_DATE+"</div></td><td><div class='content_div' title='"+REMARK+"'>"+REMARK+"</div></td></tr>";
//		var insert="<tr align='center' style='background:#CCEEFF' id='"+logid+"'><td><div class='code_div'>"+ROW_NUM+"</div></td><td><div class='content_div'>"+BIS_SYS_CODE+"</div></td><td><div class='content_div'>"+CUSTOM_CODE+"</div></td><td><div class='content_div'>"+FLW_CODE+"</div></td><td><div class='content2_div'>"+IMG_NAME+"</div></td><td><div class='content_div'>"+ORG_CODE+"</div></td><td><div class='content_div'>"+OPERATE_PERSON+"</div></td><td><div class='content_div'>"+OPERATE_DATE+"</div></td><td><div class='content_div'>"+REMARK+"</div></td></tr>";
//		var insert="<tr align='center' style='background:#CCEEFF' id='"+logid+"'><td>"+ROW_NUM+"</td><td>"+BIS_SYS_CODE+"</td><td>"+CUSTOM_CODE+"</td><td>"+FLW_CODE+"</td><td>"+IMG_NAME+"</td><td>"+ORG_CODE+"</td><td>"+OPERATE_PERSON+"</td><td>"+OPERATE_DATE+"</td><td>"+REMARK+"</td></tr>";
			//var insert="<tr style='background:#CCEEFF' id='"+logid+"'><td>"+ROW_NUM+"</td><td>"+BIS_SYS_CODE+"</td><td>"+CUSTOM_CODE+"</td><td>"+FLW_CODE+"</td><td>"+IMG_NAME+"</td><td>"+BATCH_CODE+"</td><td>"+ORG_CODE+"</td><td>"+OPERATE_PERSON+"</td><td>"+OPERATE_DATE+"</td><td>扫描</td></tr>";
			$("#insertLog").append(insert);
			$(".code_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid1});
			$(".content_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid2});
			$(".content2_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid4});
			$(".content3_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid3});
			}//for
			
			$("#totalCount").text(total);
//			$("#totalCount").text(totalCount);
			var totalPageCount;
			if(parseInt(total)%parseInt(limit)==0){
				totalPageCount=parseInt(total)/parseInt(limit);
//				totalPageCount=parseInt(totalCount)/parseInt(limit);
			}else{
				totalPageCount=Math.ceil(parseInt(total)/parseInt(limit));
//				totalPageCount=Math.ceil(parseInt(totalCount)/parseInt(limit));
			}
			$("#totalPageCount").text(totalPageCount);
			$("#pageIndex").text(parseInt(page)+1);
			
			
			
			
		}//success
		
		
	});//ajax
}

function show_calendar(){
	Calendar.setup({
		inputField : "OPERATE_DATE", // id of the input field
		ifFormat : "%Y-%m-%d", // format of the input field
		showsTime : true, // will display a time selector
		button : "f_trigger_b", // trigger for the calendar (button ID)
		singleClick : false, // double-click mode
		step : 1
	// show all years in drop-down boxes (instead of every other year as default)
	});
	}

	window.onresize=function(){
		var wind=document.body.clientWidth;

		var wid=wind-50;
		var td_wid1=wid*95/1490;
		var td_wid2=wid*170/1490;
		var td_wid3=wid*200/1490;
		var td_wid4=wid*250/1490;
		
		
		$(".code_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid1});
		$(".content_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid2});
		$(".content2_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid4});
		$(".content3_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid3});
	}

/*$(window).resize(function(){
	var wind=document.body.clientWidth;

	var wid=wind-50;
	var td_wid1=wid*95/1490;
	var td_wid2=wid*170/1490;
	var td_wid3=wid*200/1490;
	var td_wid4=wid*250/1490;
	
	
	$(".code_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid1});
	$(".content_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid2});
	$(".content2_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid4});
	$(".content3_div").css({"margin":"0","padding":"0","overflow":"hidden","text-overflow":"ellipsis","white-space":"nowrap","width":td_wid3});
});*/


/*function jump_to(frm,num){
	var regxp=/^\d+$/;
	if(!regxp.test(num)){
	alert("请输入正确的数字");
	return false;
	}else{
	 page_nav(frm,num);
	}
}
*/