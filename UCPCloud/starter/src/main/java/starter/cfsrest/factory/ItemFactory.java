/**
 * 
 */
package starter.cfsrest.factory;

import starter.cfsrest.bean.Item;

/**
 * @author JackIce
 * @date  2016年5月31日
 * @description 
 */
public interface ItemFactory<P extends Item> {
	P create();
}
