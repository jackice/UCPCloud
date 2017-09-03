/**
 * 
 */
package starter.cfsrest.bean;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author jackice
 * @company nko
 * @description
 * @date 2016年5月29日
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name="response")
public class ApiOfResponse {
	@XmlElementWrapper(name="properties")
	@XmlElement(name="property")
	private ArrayList<Property> properties;
	@XmlElement(name="lists")
	private ApiOfResponseLists lists;
	public ArrayList<Property> getProperties() {
		return properties;
	}
	public void setProperties(ArrayList<Property> properties) {
		this.properties = properties;
	}
	public ApiOfResponseLists getLists() {
		return lists;
	}
	public void setLists(ApiOfResponseLists lists) {
		this.lists = lists;
	}
	@Override
	public String toString() {
		return "ApiOfResponse [properties=" + properties + ", lists=" + lists
				+ "]";
	}
	
}
