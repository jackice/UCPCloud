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
@XmlRootElement
@XmlType(name="request")
public class RequestOfMessage {
	@XmlElementWrapper(name="properties")
	@XmlElement(name="property")
	private ArrayList<Property> properties;

	/**
	 * @return the properties
	 */
	public ArrayList<Property> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(ArrayList<Property> properties) {
		this.properties = properties;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ImageFileRequest [properties=" + properties + "]";
	}
	
}
