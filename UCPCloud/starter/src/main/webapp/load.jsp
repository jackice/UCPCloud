<%@page import="java.io.IOException"%>
<%@page import="org.apache.catalina.connector.ClientAbortException"%>
<%@page import="org.apache.commons.net.io.CopyStreamException"%>
<%@page import="org.apache.commons.net.ftp.FTPFile"%>
<%@page import="java.io.OutputStream"%>
<%@page import="org.apache.commons.net.ftp.FTPReply"%>
<%@page import="org.apache.commons.net.ftp.FTPClient"%>
<%@page import="starter.cfsrest.util.FtpUtils"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<title>Insert title here</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
</head>
<body>
<%
String url="200.198.22.83";
String port="21";
String username="ftptest";
String password="ftptest";
String remotePath="/";
String fileName="setup.exe";
//FtpUtils.downFile(url, port, username, password, remotePath, fileName, response);

 int port2=Integer.parseInt(port);
//String localPath="D:/";
   FTPClient ftp = new FTPClient();    
   try {    
       int reply;    
       ftp.connect(url, port2);    
       //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器     
       ftp.login(username, password);//登录     
       reply = ftp.getReplyCode();    
       if (!FTPReply.isPositiveCompletion(reply)) {    
           ftp.disconnect(); 
       }    
       ftp.changeWorkingDirectory(remotePath);//转移到FTP服务器目录     
       FTPFile[] fs = ftp.listFiles();  

out.clear();
out=pageContext.pushBody();
       for(FTPFile ff:fs){    
           if(ff.getName().equals(fileName)){    
           	response.setContentType("application/octet-stream");
           	response.setHeader("Content-Disposition","attachment;filename=".concat(fileName));
           	OutputStream output=response.getOutputStream();
           	//File localFile = new File(localPath+"/"+ff.getName());
               
               //OutputStream is = new FileOutputStream(localFile);     
           	
           	ftp.retrieveFile(ff.getName(), output);
        
           	  output.flush();
           	  
           	  output.close();
             
             }//if    
       }//for 
     
           
       ftp.logout();
   } catch (CopyStreamException cse) {
   	System.out.println("取消了下载");
      // cse.printStackTrace();    
   }catch(ClientAbortException cae){
   	System.out.println("中断了下载");
   	
   //cae.printStackTrace();
   }catch(IOException ioe){
   	//ioe.printStackTrace();
   	System.out.println("连接中断，取消了下载");
   }finally {    
       if (ftp.isConnected()) {    
           try {    
               ftp.disconnect();    
           } catch (IOException ioe) {    
           	System.out.println("连接中断，取消了下载");
           }    
       }    
   } 

%>

</body>
</html>