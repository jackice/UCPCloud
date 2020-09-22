/**
 * 
 */
package starter.cfsrest.bean;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author JackIce
 * @date  2016年6月21日
 * @description 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="row")
public class ApiOfRow {
	@XmlElement(name="item")
	private ArrayList<Item> results;
	public ArrayList<Item> getResults() {
		return results;
	}

	public void setResults(ArrayList<Item> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "ApiOfRow [results=" + results + "]";
	}
	
}
