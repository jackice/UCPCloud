/**
 * 
 */
package starter;

import java.util.Arrays;
import java.util.List;

/**
 * @author JackIce
 * @date  2016年10月13日
 * @description 
 */
public class DefaultUserMappingConfig {
	private String interfaceName;
	private String userName;
	private String password;
	
	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getInterfaces(){
		return Arrays.asList(interfaceName.split(","));
	}
}
