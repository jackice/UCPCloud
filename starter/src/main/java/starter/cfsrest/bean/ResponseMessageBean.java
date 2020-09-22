/**
 * 
 */
package starter.cfsrest.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
@XmlType(propOrder={"code","request","response"})
public class ResponseMessageBean {
	@XmlAttribute
	private String code;
	@XmlElement(name="request")
	private RequestOfMessage request;
	@XmlElement(name="response")
	private ResponseOfMessage response;
	
	/**
	 * @return the response
	 */
	public ResponseOfMessage getResponse() {
		return response;
	}
	/**
	 * @param response the response to set
	 */
	public void setResponse(ResponseOfMessage response) {
		this.response = response;
	}
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return the request
	 */
	public RequestOfMessage getRequest() {
		return request;
	}
	/**
	 * @param request the request to set
	 */
	public void setRequest(RequestOfMessage request) {
		this.request = request;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResponseMessageBean [code=" + code + ", request=" + request
				+ ", response=" + response + "]";
	}


}

