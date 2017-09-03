/**
 * 
 */
package starter.cfsrest.factory;

import starter.cfsrest.bean.RequestOfMessage;

/**
 * @author JackIce
 * @date  2016年5月31日
 * @description 
 */
@FunctionalInterface
public interface RequestOfMessageFactory<P extends RequestOfMessage> {
	P create();
}
