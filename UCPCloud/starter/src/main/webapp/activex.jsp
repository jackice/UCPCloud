<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>activity</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
 <script type="text/javascript" src="JS/jquery.js"></script>
<!--<script type="text/javascript" src="JS/print.js"></script>
<script type="text/javascript" src="JS/treeShow.js"></script>
<script type="text/javascript" src="JS/printCode3.js"></script> -->
<script type="text/javascript" src="JS/jquery_treeview/jquery.treeview.js"></script>
<script type="text/javascript" src="JS/activex.js"></script>
<link type="text/css" rel="stylesheet" href="JS/jquery_treeview/jquery.treeview.css"/> 
<link type="text/css" rel="stylesheet" href="CSS/activex.css"/>
<style type="text/css">

  .button{
  	 	background:url(images/aaa.png) no-repeat; 

color:black; 
/* border-radius:100px; */

font-family:Verdana,Arial,Sans-Serif;
font-size:2 em;
font-weight:bold;
text-align:center;
/* box-shadow:5px 5px 5px #888; */
display:inline-block;
margin-right:5px; 
  }
  #loadkongjian{
  height:30px;
width:160px;
background:url(images/ddd.png) no-repeat; 

color:black; 

font-family:Verdana,Arial,Sans-Serif;
font-size:2 em;
font-weight:bold;
text-align:center;
display:inline-block;
margin-right:5px; 
  
  
  }
</style>
</head>
<body>
<div class="main-active">

 <div class="buttons">
	<div class="dButton" id="sscan"><input class="button" type="button" value="扫&nbsp;描"  id="scanButton"  onclick="scan_code()"/></div>
	<div class="dButton" id="pprint"><input class="button" type="button" value="条&nbsp;码" id="print_codeButton" onclick="print_code()"/></div>
	<div class="dButton"><input class="button" type="button" value="查&nbsp;看" id="lookButton"  onclick="look_code()"/></div>
	<div class="dButton5"><input readonly="readonly" id="tishi"/></div>
	<div class="dButton3"><input  type="button" id="loadkongjian" onclick="download_batchshell()" value="下载补丁"/></div>
	<div class="dButton3"><input  type="button" id="loadkongjian" onclick="load_kongjian()" value="下载控件安装包"  /></div>
</div>

<!-- 控件区 -->
 <div class="kongjian" id="UCPScannerM"  style="display:none"></div>
<div class="kongjian" id="UCPExplorerM"  style="display:none"></div>

<!-- 条码区 -->
<div class="print_codes" style="display:none;">
<div class="buttons">
<input class="button4" type="button" value="打印条码"  id=""  onclick="toPrint()"/>
</div>
<div class="main-content" id="BarCode">
		 	<table border="1" cellspacing="0" id="table_show">
		 	<tr align="center" style="background:#CCEEFF;height:30px">
		 	<td width="500px"><input type="checkbox" id="checkAll" class="checkbox" value="false"></td>
		 	<td width="2000px"><p id="typeCode" >业务流水号</p></td>
		 	</tr>
		 	</table>
</div>
</div>

<!-- 树状图 -->

<div class="print_tree_code"  style="display:none">
	<div class="buttons2">
	<input class="button4" type="button" value="打印条码"  id=""  onclick="printC()"/>
	</div>
		<div id="tree_show">
			<div id="message"><p>文档类型区(单击节点后面的按钮)</p></div>
			<div id="data_select"><div id="selectMess"><p>文档类型编码:</p></div><div id="inputSelectCode"><input id="inputCode" type="text"/></div><div id="selectCodeButton"><input type="button" value="查询" onclick="selectCode()"/> </div></div>
			<div  id="trees">
				<ul id="tree"  class="filetree"></ul>
			</div>
		</div>
		<div id="tree_message">
			<div id="message_data"><div id="messageDelete"><p>类型选择区</p></div><div class="button4"><input type="button" value="删除" onclick="deleteCode()"/></div></div>
			<div id="tree-part-data">
						<table border="1" cellspacing="0" id="showCode">
						<tr align="center" style="background:#99FFFF">
						<td width="30"><input type="checkbox" id="checkAllCode"/></td>
						<td width="300">打印码</td>
						<td  width="800">名称</td>
						</tr>
						</table>
			</div>
		</div>
	
</div> 
<!-- 打印控件 -->
<div><object  id="UCPPrintBarcode" classid="clsid:5e8b9d96-abdd-47b9-93ab-8c0add82564c" style="display:none"></object></div>

<!-- 隐藏域 -->
<div style="display: none;">
<input type="hidden" id="url"/>
<input type="hidden" id="ucpType" />
<input type="hidden" id="username"/>
<input type="hidden" id="password"/>
<input type="hidden" id="SystemCode" value="<%=request.getParameter("SystemCode")%>"/>
<input type="hidden" id="FunctionCode" value="<%=request.getParameter("FunctionCode") %>"/>
<input type="hidden" id="CustomerCode" value="<%=request.getParameter("CustomerCode") %>"/>
<input type="hidden" id="BusinessCode" value="<%=request.getParameter("BusinessCode")%>"/>
<input type="hidden" id="BatchCode" value="<%=request.getParameter("BatchCode") %>"/>
<input type="hidden" id="OrgCode" value="<%=request.getParameter("OrgCode") %>"/>
<input type="hidden" id="UserCode" value="<%=request.getParameter("UserCode") %>"/>
<input type="hidden" id="ImgCode" value="<%=request.getParameter("ImgCode")%>"/>
<input type="hidden" id="AuthorizCode" value="<%=request.getParameter("AuthorizCode") %>"/>
<input type="hidden" id="ScanFlag" value="<%=request.getParameter("ScanFlag") %>"/>
<input type="hidden" id="IsBatch" value="<%=request.getParameter("IsBatch") %>"/>
<input type="hidden" id="CheckFlag" value="<%=request.getParameter("CheckFlag") %>"/>
<input type="hidden" id="FileFlag" value="<%=request.getParameter("FileFlag") %>"/>
<input type="hidden" id="FlowFlag" value="<%=request.getParameter("FlowFlag") %>"/>
<input type="hidden" id="BarCode1" value="<%=request.getParameter("BarCode") %>"/>
<input type="hidden" id="PrintType" value="<%=request.getParameter("PrintType") %>"/>
<input type="hidden" id="ActiveType" value="<%=request.getParameter("ActiveType") %>"/>
</div>
</div>

</body>
</html>
