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
public class DeleteApiOfResponse {
	@XmlElementWrapper(name="properties")
	@XmlElement(name="property")
	private ArrayList<Property> properties;
	@XmlElementWrapper(name="result")
	@XmlElement(name="item")
	private ArrayList<Item> results;
	public ArrayList<Property> getProperties() {
		return properties;
	}
	public void setProperties(ArrayList<Property> properties) {
		this.properties = properties;
	}
	public ArrayList<Item> getResults() {
		return results;
	}
	public void setResults(ArrayList<Item> results) {
		this.results = results;
	}
	@Override
	public String toString() {
		return "DeleteApiOfResponse [properties=" + properties + ", results="
				+ results + "]";
	}
	
}
