package starter;

import java.util.Arrays;
import java.util.List;

public class FileSizeLimitConfig {
	private String type;
	private long size;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	
	public List<String> getTypes(){
		return Arrays.asList(type.split(","));
	}
	
}
