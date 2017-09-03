package starter.cfsrest.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import starter.cfsrest.rest.DowloadZipRest;
@Service
public class ZipOutStream extends HttpServlet {
	public ZipOutStream () {
		super();
	}
	public void destroy() {
		super.destroy();
	}
	private Logger logger = Logger.getLogger(ZipOutStream.class);
	/*
	 * 将压缩包的输出流返回页面，供页面下载
	 */
	public void service(HttpServletRequest request, HttpServletResponse response,String filename,String zipTmpPath)throws ServletException, IOException {
		this.service(request, response);
			try {
				FileInputStream fileInput = new FileInputStream(FilenameUtils.concat(zipTmpPath,filename));
				int i = fileInput.available();
				byte[] content = new byte[i];
				fileInput.read(content);
				response.setCharacterEncoding(System.getProperty("sun.jnu.encoding"));
				response.setContentType("application/octet-stream");
				response.setHeader("Content-Disposition", "attachment;filename=".concat(filename));
				OutputStream output = response.getOutputStream();
				output.write(content);
				output.flush();
				fileInput.close();
				//output.close();
			} catch (Exception e) {
				logger.error("outputstream fail:"+e.getMessage());
			}
		}
}
