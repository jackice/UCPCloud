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
 * @author jackice
 * @company nko
 * @description
 * @date 2016年5月29日
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="message")
@XmlType(name="response")
public class DeleteApiOfResponseOfMessage {
	@XmlAttribute
	private String code;
	@XmlElement(name="response")
	private DeleteApiOfResponse response;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public DeleteApiOfResponse getResponse() {
		return response;
	}
	public void setResponse(DeleteApiOfResponse response) {
		this.response = response;
	}
	@Override
	public String toString() {
		return "DeleteApiOfResponseOfMessage [code=" + code + ", response="
				+ response + "]";
	}
	
	
}
