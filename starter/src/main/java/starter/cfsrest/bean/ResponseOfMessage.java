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
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="response")
public class ResponseOfMessage {
	@XmlElementWrapper(name="result")
	@XmlElement(name="item")
	private ArrayList<Item> results;

	/**
	 * @return the results
	 */
	public ArrayList<Item> getResults() {
		return results;
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(ArrayList<Item> results) {
		this.results = results;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResponseOfMessage [results=" + results + "]";
	}
	
}
