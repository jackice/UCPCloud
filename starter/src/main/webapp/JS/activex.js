/*
date:2016年6月5日
author:NKO
descriptioin:针对全流程平台定制化开发js
*/
$(document).ready(function(){
	var SystemCode1=document.getElementById("SystemCode").value;
	var FunctionCode1=document.getElementById("FunctionCode").value;
	var CustomerCode1=document.getElementById("CustomerCode").value;
	var BusinessCode1=document.getElementById("BusinessCode").value;
	var BatchCode1=document.getElementById("BatchCode").value;
	var OrgCode1=document.getElementById("OrgCode").value;
	var UserCode1=document.getElementById("UserCode").value;
	var ImgCode1=document.getElementById("ImgCode").value;
	var AuthorizCode1=document.getElementById("AuthorizCode").value;
	var ScanFlag1=document.getElementById("ScanFlag").value;
	var IsBatch1=document.getElementById("IsBatch").value;
	var CheckFlag1=document.getElementById("CheckFlag").value;
	var FileFlag1=document.getElementById("FileFlag").value;
	var FlowFlag1=document.getElementById("FlowFlag").value;
	var BarCode1=document.getElementById("BarCode1").value;
	var PrintType1=document.getElementById("PrintType").value;
	var ActiveType1=document.getElementById("ActiveType").value;
var a=window.location.href;
	var data={"value":a,
"username":"cfsuser",
"password":"cfsuser",			
"SystemCode":SystemCode1,
			"FunctionCode":FunctionCode1,
			"CustomerCode":CustomerCode1,
			"BusinessCode":BusinessCode1,
			"BatchCode":BatchCode1,
			"OrgCode":OrgCode1,
			"UserCode":UserCode1,
			"ImgCode":ImgCode1,
			"AuthorizCode":AuthorizCode1,
			"ScanFlag":ScanFlag1,
			"IsBatch":IsBatch1,
			"CheckFlag":CheckFlag1,
			"FileFlag":FileFlag1,
			"FlowFlag":FlowFlag1,
			"BarCode":BarCode1,
			"PrintType":PrintType1,
			"ActiveType":ActiveType1

	};
var url="/cfs/scan";
 	$.ajax({
 		async: false,
 		cache: false,
 		contentType: 'application/json',
 		type: 'GET',
 		dataType: 'json',
 		url: url,
 		data:data,
 		error:function(e){
 			alert('失败');
			},
 		success:function loadinfo(data){
 		var json=eval(data);
 		var ActiveType=json.ActiveType;
 		var url=json.url;
 		var ucpType=json.ucpType;
 		var username=json.username;
 		var password=json.password;
 		
 		var SystemCode=json.SystemCode;
 		var FunctionCode=json.FunctionCode;
 		var CustomerCode=json.CustomerCode;
 		var BusinessCode=json.BusinessCode;
 		var BatchCode=json.BatchCode;
 		var OrgCode=json.OrgCode;
 		var UserCode=json.UserCode;
 		var ImgCode=json.ImgCode;
 		var AuthorizCode=json.AuthorizCode;
 		var ScanFlag=json.ScanFlag;
 		var IsBatch=json.IsBatch;
 		var CheckFlag=json.CheckFlag;
 		var FileFlag=json.FileFlag;
 		var FlowFlag=json.FlowFlag;
 		var PrintType=json.PrintType;
 		var BarCode=json.BarCode;
 		var PrintType=json.PrintType;
 		
 		$("#url").val(url);
 		$("#ucpType").val(ucpType);
 		$("#username").val(username);
 		$("#password").val(password);
 		$("#SystemCode").val(SystemCode);
 		$("#FunctionCode").val(FunctionCode);
 		$("#CustomerCode").val(CustomerCode);
 		$("#BusinessCode").val(BusinessCode);
 		$("#BatchCode").val(BatchCode);
 		$("#OrgCode").val(OrgCode);
 		$("#UserCode").val(UserCode);
 		$("#ImgCode").val(ImgCode);
 		$("#AuthorizCode").val(AuthorizCode);
 		$("#ScanFlag").val(ScanFlag);
 		$("#IsBatch").val(IsBatch);
 		$("#CheckFlag").val(CheckFlag);
 		$("#FileFlag").val(FileFlag);
 		$("#FlowFlag").val(FlowFlag);
 		$("#BarCode1").val(BarCode);
 		$("#PrintType").val(PrintType);
 		if(ActiveType=="100"){
 			$("#scanButton").css("display","block");
 				$("#lookButton").css("display","none");
 	$("#print_codeButton").css("display","none");
 			$("#print_code").css("display","none");
			$("#scanButton").click();
 		}else if(ActiveType=="010"){
 		$("#lookButton").css("display","block");	
 		$("#scanButton").css("display","none");
 			$("#print_codeButton").css("display","none"); 			
 		$("#print_code").css("display","none");		
 			$("#lookButton").click();
 		}else if(ActiveType=="001"){		
 		$("#print_codeButton").css("display","block");
 		$("#scanButton").css("display","none");
 		$("#lookButton").css("display","none");		
 		$("#print_code").css("display","block");
 		$("#printTree").css("display","none");	
 		$("#print_codeButton").click();	
 		}else if(ActiveType=="111"){
 	$("#scanButton").css("display","block");
 	$("#lookButton").css("display","block");
 	$("#print_codeButton").css("display","block");	
 	$("#UCPExplorer").css("display","none");
 	$("#scanButton").click();	
 }		
 		}	
 		});
});

function scan_code(){
	$("#tishi").css("display","none");
	
	$("#loadkongjian").css("display","block");
	$("#UCPScannerM").css("display","block");
	$("#UCPScannerM").empty();
	$("#UCPExplorerM").css("display","none");
	$(".print_codes").css("display","none");
	$(".print_tree_code").css("display","none");
 	$("#UCPExplorerM").css("display","none");
 		$("#UCPScannerM").css("display","block");	
	document.getElementById("scanButton").disabled=true;
	document.getElementById("lookButton").disabled=false;
	document.getElementById("print_codeButton").disabled=false;
$(document).ready(function(){
	var url=$("#url").val();
	var ucpType=$("#ucpType").val();
	var username=$("#username").val();
	var password=$("#password").val();
	var SystemCode=$("#SystemCode").val();
	var FunctionCode=$("#FunctionCode").val();
	var CustomerCode=$("#CustomerCode").val();
	var BusinessCode=$("#BusinessCode").val();
	var BatchCode=$("#BatchCode").val();
	var OrgCode=$("#OrgCode").val();
	var UserCode=$("#UserCode").val();
	var ImgCode=$("#ImgCode").val();
	var AuthorizCode=$("#AuthorizCode").val();
	var ScanFlag=$("#ScanFlag").val();
	var IsBatch=$("#IsBatch").val();
	var insert="<div id='obj_Scanner'><object id='UCPScaner' height='660' width='100%' classid='clsid:c3e203ae-df0f-4b01-9479-cc9ff08668d6'></object></div>";
	$("#UCPScannerM").append(insert);
	$(document).ready(function(){		
	document.getElementById("UCPScaner").Startup(url,ucpType,username,password,SystemCode,FunctionCode,CustomerCode,BusinessCode,BatchCode,OrgCode,UserCode,ImgCode,AuthorizCode,ScanFlag,IsBatch);
	});
});
}
function look_code(){
	$("#tishi").css("display","block");
	$("#tishi").val("关闭浏览器之前记得点击同步按钮！");	
	document.getElementById("scanButton").disabled=false;
	document.getElementById("lookButton").disabled=true;
	document.getElementById("print_codeButton").disabled=false;	
	$("#loadkongjian").css("display","block");
	$("#UCPScannerM").css("display","none");
	$("#UCPExplorerM").empty();
	$("#UCPExplorerM").css("display","block");
	$(".print_codes").css("display","none");
	$(".print_tree_code").css("display","none");
	$(document).ready(function(){
	var url=$("#url").val();
	var ucpType=$("#ucpType").val();
	var username=$("#username").val();
	var password=$("#password").val();
	var SystemCode=$("#SystemCode").val();
	var FunctionCode=$("#FunctionCode").val();
	var CustomerCode=$("#CustomerCode").val();
	var BusinessCode=$("#BusinessCode").val();
	var OrgCode=$("#OrgCode").val();
	var UserCode=$("#UserCode").val();
	var ImgCode=$("#ImgCode").val();
	var AuthorizCode=$("#AuthorizCode").val();
	var CheckFlag=$("#CheckFlag").val();
	var FileFlag=$("#FileFlag").val();
	var FlowFlag=$("#FlowFlag").val();
	$("#UCPScannerM").css("display","none");
	$("#UCPExplorerM").empty();
	var insert="<div id='obj_Explorer'><object id='UCPExplorer' height='660' width='100%' classid='clsid:357f69bb-7c8d-4dac-b8f1-da019b7bdda7'></object></div>";
	$("#UCPExplorerM").append(insert);
	if($("#scanButton").css("display")=="none"){
		$("#sscan").remove();
		$("#pprint").remove();
		
	}
$(document).ready(function(){
	document.getElementById("UCPExplorer").Startup(url,ucpType,username,password,SystemCode,FunctionCode,CustomerCode,BusinessCode,OrgCode,UserCode,ImgCode,AuthorizCode,CheckFlag,FileFlag,FlowFlag);
	});
});
}
function print_code(){
	$("#UCPScannerM").css("display","none");
	$("#UCPExplorerM").css("display","none");
	$("#tishi").css("display","none");
	document.getElementById("scanButton").disabled=false;
	document.getElementById("lookButton").disabled=false;
	document.getElementById("print_codeButton").disabled=true;
	$("#loadkongjian").css("display","none");
var PrintType=$("#PrintType").val();
var BarCode=$("#BarCode1").val();
var ActiveType=$("#ActiveType").val();
var typeAndCode={
	"PrintType":PrintType,
	"BarCode":BarCode
};

if($("#scanButton").css("display")=="none"){
	$("#sscan").remove();
}
var url="/cfs/tree_data/cfs_tree";

	$.ajax({
 		async: false,
 		cache: false,
 		contentType: 'application/json',
 		type: 'GET',
 		dataType: 'json',
 		url: url,
 		data:typeAndCode,
 		error:function(e){
			},
 		success:function loadinfo(data){ 		
 			var json=eval(data); 			
 			var PrintType=json.PrintType; 			
 		if(PrintType=="01"||PrintType=="03"){		
 			$(".print_tree_code").css("display","none");
 			$(".print_codes").css("display","block");
 				var BarCode2=json.BarCode;
 				var barLen=BarCode2.length;
 			var PrintType2=json.PrintType;
 			var typeCode=json.typeCode;
 			$("tr").remove(".extra-content");
 			var insert="";
 			if(BarCode2!=""){
 			for(var i=0;i<barLen;i++){
 				insert=insert+"<tr align='center' class='extra-content' style='height:30px'><td width='500px'><input type='checkbox' name='subBox' class='checkbox' value='"+BarCode2[i]+"'></td><td width='2000px'><p id='typeCode'>"+BarCode2[i]+"</p></td></tr>"; 	
 				}
 			}
 			$("#table_show").append(insert);
 			$("#typeCode").text(typeCode);
 			$("#PrintType").val(PrintType2);
 		}else{
 		$(".print_codes").css("display","none");
 		$(".print_tree_code").css("display","block"); 			
 			$("#tree").empty();
 			var insert="";
	    	var documents=json.documents;
	    	for(var i=0;i<documents.length;i++){	    	
	    		var child=documents[i].childrens;//��һ��childrens 13 11 14 12	    		
	    		var sum=0;
	    		for(var j=0;j<child.length;j++){
	    			if(child[j].childrens==null||child[j].msg_name=="其他资料"){
	    			sum++;
	    			}
	    		}	    		
	    		if(sum==child.length){
	    		insert=insert+"<li id='"+documents[i].msg_code+"k'>"+"<span class='folder'  id='"+documents[i].msg_code+"m' ondblclick=\"show('"+documents[i].msg_code+"','"+documents[i].msg_name+"','0')\">"+documents[i].msg_name+"</span>";
	    		}else{	    	
	    		insert=insert+"<li>"+"<span class='folder'  id='"+documents[i]._msg_code+"m'>"+documents[i].msg_name+"</span>";
	    		}	
	    		if(documents[i].childrens!=null){
	    		 insert=insert+getChildrens(documents[i]);
	    		}		    		  	    		  
	    		  insert=insert+"</li>"; 
	    	}
	    	$("#tree").append(insert);	    	
	    	$("#tree").treeview({
	    		persist:"location",
	    		collapsed:true,
	    		unique:false});
	    	
 			}
 		} 		
 	});
}

function getChildrens(documents){
	var insert="";
	insert=insert+"<ul>";
					var children=documents.childrens;
					
	    		for(var m=0;m<children.length;m++){
						if(children[m].childrens==null||children[m].msg_name=="其他资料"){
						insert=insert+"<li>"+"<span class='folder' id='"+children[m].msg_code+"m'  ondblclick=\"show('"+children[m].msg_code+"','"+children[m].msg_name+"','1')\">"+children[m].msg_name+"</span>";
						}else{
						var sum=0;
							for(var n=0;n<children[m].childrens.length;n++){
								
								if(children[m].childrens[n].childrens==null||children[m].childrens[n].msg_name=="其他资料"){
									sum++;
								}
							}
						 if(sum==children[m].childrens.length){
						 		insert=insert+"<li id='"+children[m].msg_code+"k'>"+"<span class='folder' id='"+children[m].msg_code+"m'  ondblclick=\"show('"+children[m].msg_code+"','"+children[m].msg_name+"','0')\">"+children[m].msg_name+"</span>";
						 }else{
						 			insert=insert+"<li>"+"<span class='folder' id='"+children[m].msg_code+"m'>"+children[m].msg_name+"</span>";			 
						 }
						}
	    		if(children[m].childrens!=null){
	    			insert=insert+getChildrens(children[m]);	   
	    				}
	    		}
	    insert=insert+"</ul>";
	    return insert;
	    		
}

function load_kongjian(){
	window.location.href="http://130.1.11.200:8080/imgpf/ucp.msi";
	
}
function download_batchshell(){
	window.location.href="http://130.1.11.200:8080/imgpf/update.exe";
}
function show(id,name,num){
	var treeId="#"+id+"m";
	if(num=="1"){
		var subBox=document.getElementsByName("box");
		var sum=0;	
		for(var i=0;i<subBox.length;i++){
			if(subBox[i].value!=id){
				sum++;
			}
		}		
		if(sum==subBox.length){
		$(treeId).css("background-color","#EEEE00");
		var insert="<tr align='center' id='"+id+"'><td><input type='checkbox' name='box' value='"+id+"'/></td><td width='300'>"+id+"</td><td  width='800'>"+name+"</td></tr>";
		$("#showCode").append(insert);
		}
	}
	if(num=="0"){
		var liEle=document.getElementById(id+"k");
		var ulEle=liEle.childNodes[2];
		var liEle2=ulEle.childNodes;	
		for(var i=0;i<liEle2.length;i++){			
			var liEle3=liEle2[i].childNodes;
			var liName=liEle3[0].innerHTML;
			var liId=liEle3[0].id.split("m")[0];
			show(liId,liName,"1");
		}	
	}	
}

function deleteCode(){	
	var subBox=document.getElementsByName("box");
	var index=0;
	var subLength=subBox.length;
	for(var i=0;i<subLength;i++){
		var parent=subBox[index].parentNode.parentNode.parentNode;
		var child=subBox[index].parentNode.parentNode;
		if(subBox[index].checked){
			var id="#"+subBox[index].value+"m";
			$(id).css("background-color","white");
			parent.removeChild(child);
		}else{
			index=index+1;
		}	
	}
	$("#checkAllCode").prop("checked",false);
}


$(function(){	
	$("#checkAllCode").click(function(){
		$('input[name="box"]').prop("checked",$(this).prop("checked"));
	});
	var $box=$('input[name="box"]');
	
	$box.click(function(){
		$("#checkAll").prop("checked",$box.length == $("input[name='box']:checked").length ? true :false);
	});
	
	$("#checkAll").click(function(){
			$('input[name="subBox"]').prop("checked",$(this).prop("checked"));
	});
	var $subBox=$("input[name='subBox']");
	$subBox.click(function(){
		$("#checkAll").attr("checked",$subBox.length == $("input[name='subBox']:checked").length ? true :false);
	});
});
function selectCode(){
	var inputCode=document.getElementById("inputCode").value;
	var code="#"+inputCode+"m";
	window.location.hash=code;
	$(code).dblclick();
}

function printC(){
	var subBox=document.getElementsByName("box");
	var code="";
	for(var i=0;i<subBox.length;i++){
		if(subBox[i].checked){
			code=code+"TC:"+subBox[i].value+",";
		}
		
	}
	document.getElementById("UCPPrintBarcode").Print(code);	//打印 主要针对IE浏览器
	
}


function toPrint(){
	var box=document.getElementsByName("subBox");
	var PrintType=$("#PrintType").val();
	var code="";
	if(PrintType=="01"){
		code="CC";
	}else if(PrintType=="03"){
		code="BC";	
	}else if(PrintType=="04"){
		code="TC";
	}
	var BarCode="";
	for(var i=0;i<box.length;i++){
		if(box[i].checked){
			if(i!=box.length-1){
				BarCode=BarCode+code+":"+box[i].value+",";	
			}else{
				BarCode=BarCode+code+":"+box[i].value;	
			}
		}
		
	}
	document.getElementById("UCPPrintBarcode").Print(BarCode);	//打印 主要针对IE浏览器
}
$(window).resize(function(){
	
	var $wind=$(window);
	var $do1=$('#UCPScannerM');
	var $do2=$('#UCPExplorerM');

	var wid=$wind.outerWidth();
	var hei=$wind.outerHeight();

	var newW=$wind.outerWidth();
	var newH=$wind.outerHeight();
	$do1.outerWidth(newW);
	$do2.outerWidth(newW);
	$do1.outerHeight(newH);
	$do1.outerHeight(newH);
});






