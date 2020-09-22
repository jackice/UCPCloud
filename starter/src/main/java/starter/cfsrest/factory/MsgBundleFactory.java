/**
 * 
 */
package starter.cfsrest.factory;

import starter.cfsrest.bean.MsgBundle;

/**
 * @author JackIce
 * @date  2016年5月31日
 * @description 
 */
@FunctionalInterface
public interface MsgBundleFactory<P extends MsgBundle<Object>> {
	P create();
}
