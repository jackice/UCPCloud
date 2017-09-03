package starter.cfsrest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class DownLoadUtils extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public DownLoadUtils () {
		super();
	}
	public void destroy() {
		super.destroy();
	}
	private Logger logger = Logger.getLogger(DownLoadUtils.class);
	/**
	 *  将压缩包的输出流返回页面，供页面下载
	 * @param request
	 * @param response
	 * @param filename	压缩文件存放路径
	 * @param zipTmpPath 压缩文件的名称
	 * @throws ServletException
	 * @throws IOException
	 */
	 
	 
	public void service(HttpServletRequest request, HttpServletResponse response,String filename,String zipTmpPath)throws ServletException, IOException {
		this.service(request, response);
			try {
				File file =new File(filename+File.separator+zipTmpPath+File.separator);
				FileInputStream fileInput = new FileInputStream(file);
				int i = fileInput.available();
				byte[] content = new byte[i];
				fileInput.read(content);
				response.setCharacterEncoding(System.getProperty("sun.jnu.encoding"));
				response.setContentType("application/octet-stream");
				response.setHeader("Content-Disposition", "attachment;filename=".concat(zipTmpPath));
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
