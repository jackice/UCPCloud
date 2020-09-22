/**
 * 
 */
package starter;

import java.util.ArrayList;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Repository;

/**
 * @author JackIce
 * @date  2016年10月13日
 * @description 
 */
@Repository
@ConfigurationProperties(prefix = "ui")
public class DefaultUserBindingConfig {
	private String defaultUser;
	private ArrayList<DefaultUserMappingConfig> defaultUserMappingConfig;
	public String getDefaultUser() {
		return defaultUser;
	}
	public void setDefaultUser(String defaultUser) {
		this.defaultUser = defaultUser;
	}
	public ArrayList<DefaultUserMappingConfig> getDefaultUserMappingConfig() {
		return defaultUserMappingConfig;
	}
	public void setDefaultUserMappingConfig(
			ArrayList<DefaultUserMappingConfig> defaultUserMappingConfig) {
		this.defaultUserMappingConfig = defaultUserMappingConfig;
	}
	
}
