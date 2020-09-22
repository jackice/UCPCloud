/**
 * 
 */
package starter.cfsrest.bean;

/**
 * @author JackIce
 * @date  2016年5月31日
 * @description 
 */
public class MsgBundle<T> {
	private boolean success;
	private String error_code;
	private String error_message;
	private T t;
	public String getError_code() {
		return error_code;
	}
	public void setError_code(String error_code) {
		this.error_code = error_code;
	}
	public String getError_message() {
		return error_message;
	}
	public void setError_message(String error_message) {
		this.error_message = error_message;
	}
	public T getT() {
		return t;
	}
	public void setT(T t) {
		this.t = t;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	@Override
	public String toString() {
		return "MsgBundle [success=" + success + ", error_code=" + error_code
				+ ", error_message=" + error_message + ", t=" + t + "]";
	}

}
