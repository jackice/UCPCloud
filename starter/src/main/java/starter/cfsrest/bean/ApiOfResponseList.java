/**
 * 
 */
package starter.cfsrest.bean;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author JackIce
 * @date  2016年6月20日
 * @description 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="list")
public class ApiOfResponseList {
	@XmlAttribute
	private String code;
	@XmlAttribute
	private String count;
	@XmlAttribute(name="batch_code")
	private String batch_code;
	@XmlElement(name="row")
	private ArrayList<ApiOfRow> rows;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getBatch_code() {
		return batch_code;
	}
	public void setBatch_code(String batch_code) {
		this.batch_code = batch_code;
	}
	public ArrayList<ApiOfRow> getRows() {
		return rows;
	}
	public void setRows(ArrayList<ApiOfRow> rows) {
		this.rows = rows;
	}
	@Override
	public String toString() {
		return "ApiOfResponseList [code=" + code + ", count=" + count
				+ ", batch_code=" + batch_code + ", rows=" + rows + "]";
	}
	
	
}
