/**
 * 
 */
package starter.cfsrest.bean;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author JackIce
 * @date  2016年6月7日
 * @description 
 */
@Component
@ConfigurationProperties(prefix = "cfs")
@Service
public class CFSConfig {
	private String folderTempPath;
	private String zipTempPath;
	private String downloadzip;

	public String getDownloadzip() {
		return downloadzip;
	}
	public void setDownloadzip(String downloadzip) {
		this.downloadzip = downloadzip;
	}
	public void setTempPath(String folderTempPath) {
		this.folderTempPath = folderTempPath;
	}
	public void setUploadTempPath(String zipTempPath) {
		this.zipTempPath = zipTempPath;
		File dir = new File(zipTempPath);
		if (dir.exists() && dir.isFile()) {
			throw new RuntimeException(String.format("the path: %s is not a directory", dir));
		}
		if (!dir.exists()) {
			dir.mkdirs();
			dir.setReadable(true);
			dir.setWritable(true);
		}
	}
	public String getFolderTempPath() {
		return folderTempPath;
	}
	public void setFolderTempPath(String folderTempPath) {
		this.folderTempPath = folderTempPath;
	}
	public String getZipTempPath() {
		return zipTempPath;
	}
	public void setZipTempPath(String zipTempPath) {
		this.zipTempPath = zipTempPath;
	}
}
