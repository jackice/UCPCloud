package starter.cfsrest.util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

public class ZipUtil {
	/** 
	 * zip compress
	 * @param dir 
	 * @param zippath 
	 */  
	public static void zip(String dir ,String zippath){  
		List<String> paths = getFiles(dir);   
		compressFilesZip(paths.toArray(new String[paths.size()]),zippath,dir);  
	}  
	/** 
	 * recursive get all the files of the current directory
	 * @param dir 
	 * @return 
	 */  
	public static List<String> getFiles(String dir){  
		List<String> lstFiles = null;       
		if(lstFiles == null){  
			lstFiles = new ArrayList<String>();  
		}  
		File file = new File(dir);  
		File [] files = file.listFiles();   
		for(File f : files){  
			if(f.isDirectory()){  
				lstFiles.add(f.getAbsolutePath());  
				lstFiles.addAll(getFiles(f.getAbsolutePath()));  
			}else{   
				String str =f.getAbsolutePath();  
				lstFiles.add(str);  
			}  
		}  
		return lstFiles;  
	}  

	/** 
	 * handle file name
	 * @param dir 
	 * @param path 
	 * @return 
	 */  
	public static String getFilePathName(String dir,String path){  
		String p = path.replace(dir+File.separator, "");  
		p = p.replace("\\", "/");  
		return p;  
	}  
	/** 
	 * compress file to zip
	 * @param files target files 
	 * @param zipFilePath compressed file path 
	 */  
	public static void compressFilesZip(String[] files,String zipFilePath,String dir) {  
		if(files == null || files.length <= 0) {  
			return ;  
		}  
		ZipArchiveOutputStream zaos = null;  
		try {  
			File zipFile = new File(zipFilePath);  
			zaos = new ZipArchiveOutputStream(zipFile);  
			zaos.setEncoding(System.getProperty("sun.jnu.encoding"));
			zaos.setUseZip64(Zip64Mode.AsNeeded);
			for(String strfile : files) {  
				File file = new File(strfile);  
				if(file != null) {  
					String name = getFilePathName(dir,strfile);  
					ZipArchiveEntry zipArchiveEntry  = new ZipArchiveEntry(file,name);  
					zaos.putArchiveEntry(zipArchiveEntry);  
					if(file.isDirectory()){  
						continue;  
					}  
					InputStream is = null;  
					try {  
						is = new BufferedInputStream(new FileInputStream(file));  
						byte[] buffer = new byte[1024 ];   
						int len = -1;  
						while((len = is.read(buffer)) != -1) {  
							zaos.write(buffer, 0, len);  
						}  
						zaos.closeArchiveEntry();   
					}catch(Exception e) {  
						throw new RuntimeException(e);  
					}finally {  

						if(is != null)  
							is.close();  
					}  

				}  
			}  
			zaos.finish();  
		}catch(Exception e){  
			throw new RuntimeException(e);  
		}finally {  
			try {  
				if(zaos != null) {  
					zaos.close();  
				}  
			} catch (IOException e) {  
				throw new RuntimeException(e);  
			}  
		}  

	}  


	/** 
	 * decompress zip file
	 * @param zipFilePath  zip file path
	 * @param saveFileDir decompressed file path 
	 */  
	public static void unzip(String zipFilePath, String saveFileDir) {  
		if(!saveFileDir.endsWith("\\") && !saveFileDir.endsWith("/") ){  
			saveFileDir += File.separator;  
		}  
		File dir = new File(saveFileDir);  
		if(!dir.exists()){  
			dir.mkdirs();  
		}  
		File file = new File(zipFilePath);  
		if (file.exists()) {  
			InputStream is = null;   
			ZipArchiveInputStream zais = null;  
			try {  
				is = new FileInputStream(file);  
				zais = new ZipArchiveInputStream(is);  
				ArchiveEntry archiveEntry = null;  
				while ((archiveEntry = zais.getNextEntry()) != null) {   
					String entryFileName = archiveEntry.getName();  
					String entryFilePath = saveFileDir + entryFileName;  
					OutputStream os = null;  
					try {  
						File entryFile = new File(entryFilePath);  
						if(entryFileName.endsWith("/")){  
							entryFile.mkdirs();  
						}else{  
							os = new BufferedOutputStream(new FileOutputStream(  
									entryFile));                              
							byte[] buffer = new byte[1024 ];   
							int len = -1;   
							while((len = zais.read(buffer)) != -1) {  
								os.write(buffer, 0, len);   
							}  
						}  
					} catch (IOException e) {  
						throw new IOException(e);  
					} finally {  
						if (os != null) {  
							os.flush();  
							os.close();  
						}  
					}  

				}   
			} catch (Exception e) {  
				throw new RuntimeException(e);  
			} finally {  
				try {  
					if (zais != null) {  
						zais.close();  
					}  
					if (is != null) {  
						is.close();  
					}  
				} catch (IOException e) {  
					throw new RuntimeException(e);  
				}  
			}  
		}  
	}  

}
