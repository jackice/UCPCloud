package starter.cfsrest.util;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;




@Controller
@RequestMapping(value = "/load", produces = MediaType.APPLICATION_JSON_VALUE)
public class FtpUtils {
	/*
	 * 从ftp提取文件，并供页面下载
	 */
	@RequestMapping(value = "/loadUCPmsi", method = {RequestMethod.GET})
	 public static void downFile(  
			 @RequestParam(defaultValue = "200.198.22.83") String url, //FTP服务器hostname  
	            @RequestParam(defaultValue = "21") String port,//FTP服务器端口  
	            @RequestParam(defaultValue = "ftptest") String username, //FTP登录账号  
	            @RequestParam(defaultValue = "ftptest") String password, //FTP登录密码  
	            @RequestParam(defaultValue = "/") String remotePath,//FTP服务器上的相对路径   
	            @RequestParam(defaultValue = "setup.exe") String fileName,//要下载的文件名
	            HttpServletResponse response
	            ) {
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
	        
	    }
	 
	 
}
