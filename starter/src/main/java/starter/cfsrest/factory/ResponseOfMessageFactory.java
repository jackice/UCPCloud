/**
 * 
 */
package starter.cfsrest.factory;

import starter.cfsrest.bean.ResponseOfMessage;

/**
 * @author JackIce
 * @date  2016年5月31日
 * @description 
 */
public interface ResponseOfMessageFactory<P extends ResponseOfMessage> {
	P create();
}
