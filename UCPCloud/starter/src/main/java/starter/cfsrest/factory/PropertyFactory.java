/**
 * 
 */
package starter.cfsrest.factory;

import starter.cfsrest.bean.Property;

/**
 * @author JackIce
 * @date  2016年5月31日
 * @description 
 */
public interface PropertyFactory<P extends Property> {
	P create();
}
