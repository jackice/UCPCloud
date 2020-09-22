package starter.cfsrest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;
@Service
public class CompressZip {
	/**
	 * 打包下载文件
	 * @param file1 要下载的文件
	 * @param FilePath 生成的压缩包文件所在路径
	 * @param tmpFileName 生成的压缩包文件的名字
	 * @param zipPath 
	 * @throws IOException
	 */
	public static void moredownload(File[] file1,String FilePath,String tmpFileName,String zipPath) throws IOException{
		File file=new File(FilePath);
		if(!file.exists()){
			file.mkdirs();
		}
		byte[] buffer = new byte[1024];
		String strZipPath = FilePath + tmpFileName;   	
//		System.out.println(strZipPath);
		          
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(strZipPath));   
		for (int i = 0; i < file1.length; i++) {   
			FileInputStream fis = new FileInputStream(file1[i]); 
		    out.putNextEntry(new ZipEntry(file1[i].getAbsolutePath().replace(zipPath+ File.separator + "temp" + File.separator, ""))); 
		    
		    //设置压缩文件内的字符编码，不然会变成乱码   
		    //out.setEncoding("GBK");   
//		    out.setEncoding(System.getProperty("sun.jnu.encoding"));
		    int len;   
		    // 读入需要下载的文件的内容，打包到zip文件   
		    while ((len = fis.read(buffer)) > 0) {   
		    	out.write(buffer, 0, len);   
		    }   
		    	out.closeEntry();   
		        fis.close();   
		    }   
		        out.close();	         				
	}
}
