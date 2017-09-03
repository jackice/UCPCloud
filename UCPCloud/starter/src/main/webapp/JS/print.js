/*$(document).ready(function(){
	var a=window.location.href;
//alert(a);
	var data={"value":a};
	
    // $("#url-value").attr("value",a);
     //自动触发点击事件，
     //$("#tijiao").trigger("click");
     //alert(33333333333);
     
     var url="cfs/test/demo/";
 	$.ajax({
 		async: false,
 		cache: false,
 		contentType: 'application/json',
 		type: 'GET',
 		dataType: 'json',
 		url: url,
 		data:data,
 		error:function(e){//请求失败处理函数
 			alert('请求失败');
			},
 		success:function loadinfo(data){
 			var json=eval(data);
 			var BarCode2=json.BarCode;
 			var PrintType2=json.PrintType;
 			//alert(BarCode.length);
 			for(var i=0;i<BarCode2.length;i++){
 			var insert="<ul class='extra-content' ><li class='col-input'><input type='checkbox' name='subBox' class='checkbox' value='"+BarCode2[i]+"' style='width: 18px;height: 18ppx'></li><li class='col-p'><p>"+BarCode2[i]+"</p></li></ul>";
 			$("#BarCode").append(insert);
 			}
 			$("#PrintType").val(PrintType2);
 		}	
 		
 	});
});*/


$(function(){
	$("#checkAll").click(function(){
		//$('input[name="subBox"]').attr("checked",this.checked);
		$('input[name="subBox"]').prop("checked",$(this).prop("checked"));
	});
	var $subBox=$("input[name='subBox']");
	$subBox.click(function(){
		$("#checkAll").attr("checked",$subBox.length == $("input[name='subBox']:checked").length ? true :false);
	});
	
});




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
				//BarCode=BarCode+code+box[i].value+",";
				
			}else{
				BarCode=BarCode+code+":"+box[i].value;
				//BarCode=BarCode+code+box[i].value;
				
			}
		}
		
	}
	
	//self.location="index.html?BarCode="+BarCode;
	document.getElementById("UCPPrintBarcode").Print(BarCode);	
	//$("#UCPPrintBarcode").Print(BarCode);
}
































