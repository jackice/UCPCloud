/**
 * 
 */
package starter.cfsrest.factory;

import starter.cfsrest.bean.ResponseMessageBean;

/**
 * @author JackIce
 * @date  2016年5月31日
 * @description 
 */
public interface ResponseMessageBeanFactory<P extends ResponseMessageBean> {
	P create();
}
