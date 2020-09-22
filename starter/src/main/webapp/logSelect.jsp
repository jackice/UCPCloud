<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
	<script type="text/javascript" src="LogJS/jquery.js"></script>
	<script type="text/javascript" src="LogJS/CFSLog.js"></script>
<link rel="stylesheet" type="text/css" media="all" href="JS/calendar/calendar-win2k-cold-1.css" title="win2k-cold-1" />

  <script type="text/javascript" src="JS/calendar/calendar.js"></script>

 
  <script type="text/javascript" src="JS/calendar/lang/calendar-en.js"></script>
  <script type="text/javascript" src="JS/calendar/lang/cn_utf8.js"></script>

  <script type="text/javascript" src="JS/calendar/calendar-setup.js"></script>	 
	<style type="text/css">
	
	.back{
	background:url(images/ddd.png) repeat; 
	color:black; 
	}
	html,body{
	width: 100%;
	margin: 0;
	padding: 0;
	}
	
	/* .code_div{
	width:90px;
	margin: 0;
	padding: 0;
	overflow: hidden;
	text-overflow:ellipsis;
	white-space: nowrap;
	}
	.content_div{
	width:170px;
	margin: 0;
	padding: 0;
	overflow: hidden;
	text-overflow:ellipsis;
	white-space: nowrap;
	}
	
	.content2_div{
	width:250px;
	margin: 0;
	padding: 0;
	overflow: hidden;
	text-overflow:ellipsis;
	white-space: nowrap;
	
	}
	.content3_div{
	width:200px;
	margin: 0;
	padding: 0;
	overflow: hidden;
	text-overflow:ellipsis;
	white-space: nowrap;
	
	} */
	
	</style>
  </head>
  
  <body>
  <fieldset>
  <p class="back">您当前的位置：【影像日志管理】-【影像日志查询】</p>
<form action="/cfs/logs/cfs_logs">
<input type="hidden" id="page" value="0"/>
<fieldset id="select">
<legend align="left">查询条件</legend>
<table border="1" cellspacing="0" >
<tr align="right" >
<td width=375>系统代码:</td>
<td width="375" align="left">&nbsp;&nbsp;<input id="BIS_SYS_CODE" type="text"/></td>
<td width="375">机构代码:</td>
<td width="375" align="left">&nbsp;&nbsp;<input id="ORG_CODE" type="text"/></td>
</tr>
<tr align="right">
<td width="375">用户代码:</td>
<td width="375" align="left">&nbsp;&nbsp;<input id="OPERATE_PERSON" type="text"/></td>
<td width="375">批次代码:</td>
<td width="375"  align="left">&nbsp;&nbsp;<input id="BATCH_CODE" type="text"/></td>
</tr>
<tr align="right">
<td width="375">客户编号:</td>
<td width="375" align="left">&nbsp;&nbsp;<input id="CUSTOM_CODE" type="text"/></td>
<td width="375">业务编号:</td>
<td width="375" align="left">&nbsp;&nbsp;<input id="FLW_CODE" type="text"/></td>
</tr>
<tr align="right">
<td width="375">影像名称:</td>
<td width="375" align="left">&nbsp;&nbsp;<input id="IMG_NAME" type="text"/></td>
<td width="375">操作时间:</td>
<td width="375" align="left">&nbsp;&nbsp;<input  type="text" id="OPERATE_DATE" readonly="readonly" /><button type="reset" id="f_trigger_b" onclick="show_calendar()">...</button></td>
</tr>
<tr class="back">
<td colspan="4" align="center" ><input id="sub" type="button" value="提交查询"  onclick="selectCFSLog()" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="reset" value="重置页面"/></td>
</tr>
</table>
</fieldset>
<fieldset>
<legend align="left">查询结果</legend>
<table border="1" cellspacing="0" id="insertLog">
<!-- <tr style="background:#99FFFF">
<td width="95">序列代码</td>
<td width="95">客户号</td>
<td width="120">系统号</td>
<td width="120">业务号</td>
<td width="120">影像名称</td>
<td width="170">批次号</td>
<td width="120">机构名称</td>
<td width="120">用户名称</td>
<td width="120">操作时间</td>
<td width="120">备注</td>
</tr> -->
</table>
<!-- <table border="1" cellspacing="0" id="insertLog"></table> -->
</fieldset>
</form>
<table>
<tr class="back">
<td width="750" align="left"> 共<label id="totalCount"></label>条记录，当前第<span id="pageIndex"></span>/<span id="totalPageCount"></span>页</td>
<td width="750" align="right"> 
	<%--  <c:if test="${pageIndex>1}">
		 <a href="javascript:page_nav(document.forms[0],1)">首页</a>
		<a href="javascript:page_nav(document.forms[0],${pageIndex-1})">上一页</a>
		</c:if>
	  	<c:if test="${pageIndex<totalPageCount}">
		<a href="javascript:page_nav(document.forms[0],${pageIndex+1})">下一页</a>
		<a href="javascript:page_nav(document.forms[0],${totalPageCount})">最后一页</a>
		</c:if> --%>
		
	<input type="button" value="首页" id="first" onclick="jump_to('1','0')"/>	
	<input type="button" value="上一页" id="before" onclick="jump_to(document.getElementById('pageIndex').innerHTML,'1')"/>	
	<input type="button" value="下一页" id="after" onclick="jump_to(document.getElementById('pageIndex').innerHTML,'2')"/>	
	<input type="button" value="末页"  id="last" onclick="jump_to(document.getElementById('totalPageCount').innerHTML,'0')"/>	
	  <span><label>转至</label>
	  <input type="text" name="inputPage" id="inputPage"  style="width:40px"/>页
	  <button type="button" style="font-weight:bold" onclick="jump_to(document.getElementById('inputPage').value,'0')">&rarr;转</button>
	  </span>
`

</td>
</tr>
</table>
  
  
  </fieldset>
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

 
 
  </body>
</html>
