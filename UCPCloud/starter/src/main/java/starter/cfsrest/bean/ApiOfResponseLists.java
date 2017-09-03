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
//@XmlType(name="lists")
public class ApiOfResponseLists {
	@XmlElement(name="list")
	private ApiOfResponseList list;

	public ApiOfResponseList getList() {
		return list;
	}

	public void setList(ApiOfResponseList list) {
		this.list = list;
	}

	@Override
	public String toString() {
		return "ApiOfResponseLists [list=" + list + "]";
	}
	
}
