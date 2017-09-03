$(function(){
	$("#checkAll").click(function(){
		//$('input[name="subBox"]').attr("checked",this.checked);
		$('input[name="box"]').prop("checked",$(this).prop("checked"));
	});
	var $subBox=$("input[name='box']");
	$subBox.click(function(){
		$("#checkAll").attr("checked",$subBox.length == $("input[name='box']:checked").length ? true :false);
	});
	
});