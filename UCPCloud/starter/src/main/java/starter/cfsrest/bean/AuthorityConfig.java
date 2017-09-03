/**
 * 
 */
package starter.cfsrest.bean;

import java.util.ArrayList;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Repository;

/**
 * @author JackIce
 * @date  2016年6月7日
 * @description 
 */
@Repository
@ConfigurationProperties(prefix = "auth")
public class AuthorityConfig {
	private String permissions;
	private String matchs;

	public String getMatchs() {
		return matchs;
	}

	public void setMatchs(String matchs) {
		this.matchs = matchs;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

}
