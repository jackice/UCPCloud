package starter;

import java.util.ArrayList;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Repository;

@Repository
@ConfigurationProperties(prefix = "fsl")
public class FileSizeLimit {
	private long defaultSize;
	private ArrayList<FileSizeLimitConfig> fslConfigs;
	
	public ArrayList<FileSizeLimitConfig> getFslConfigs() {
		return fslConfigs;
	}
	public void setFslConfigs(ArrayList<FileSizeLimitConfig> fileSizeLimitConfigs) {
		this.fslConfigs = fileSizeLimitConfigs;
	}
	public long getDefaultSize() {
		return defaultSize;
	}
	public void setDefaultSize(long defaultSize) {
		this.defaultSize = defaultSize;
	}

}
