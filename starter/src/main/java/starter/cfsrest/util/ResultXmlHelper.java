/**
 * 
 */
package starter.cfsrest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import starter.cfsrest.bean.ApiOfResponse;
import starter.cfsrest.bean.ApiOfResponseList;
import starter.cfsrest.bean.ApiOfResponseLists;
import starter.cfsrest.bean.ApiOfResponseOfMessage;
import starter.cfsrest.bean.ApiOfRow;
import starter.cfsrest.bean.Constant;
import starter.cfsrest.bean.DeleteApiOfResponse;
import starter.cfsrest.bean.DeleteApiOfResponseOfMessage;
import starter.cfsrest.bean.Item;
import starter.cfsrest.bean.Property;
import starter.cfsrest.bean.RequestOfMessage;
import starter.cfsrest.bean.ResponseMessageBean;
import starter.cfsrest.bean.ResponseOfMessage;
import starter.cfsrest.exception.CFSImgException;
import starter.cfsrest.factory.ItemFactory;
import starter.cfsrest.factory.PropertyFactory;
import starter.cfsrest.factory.RequestOfMessageFactory;
import starter.cfsrest.factory.ResponseMessageBeanFactory;
import starter.cfsrest.factory.ResponseOfMessageFactory;

/**
 * @author JackIce
 * @date  2016年5月31日
 * @description 
 */
public class ResultXmlHelper {
	/**
	 * 生成xml文件
	 * @param request_map
	 * @param response_map
	 * @param opretion_type
	 * @return
	 */
	public static String convertXml(Map request_map,Map response_map,String opretion_type){
		PropertyFactory<Property> propertyFactory = Property::new;
		Property property = propertyFactory.create();
		Map attr_map = (Map)request_map.get(Constant.XML_ATTRANDVALUE_MAP_NAME);
		property.setCode(Constant.CFS_SYS_CODE_KEY);
		property.setValue((String)attr_map.get(Constant.CFS_SYS_CODE_KEY));
		String fun_code = (String)attr_map.get(Constant.CFS_FUN_CODE_KEY);
		Property property_1 = propertyFactory.create();
		property_1.setCode(Constant.CFS_FUN_CODE_KEY);
		property_1.setValue(fun_code);
		Property property_2 = propertyFactory.create();
		property_2.setCode(Constant.CFS_CUSTOM_CODE_KEY);
		property_2.setValue((String)attr_map.get(Constant.CFS_CUSTOM_CODE_KEY));
		Property property_3 = propertyFactory.create();
		property_3.setCode(Constant.CFS_BUSS_ID_KEY);
		property_3.setValue((String)attr_map.get(Constant.CFS_BUSS_ID_KEY));
		Property property_4 = propertyFactory.create();
		if (StringUtils.isNotEmpty((String)attr_map.get(Constant.CFS_OP_CODE_KEY))) {
			property_4.setCode(Constant.CFS_OP_CODE_KEY);
			property_4.setValue((String)attr_map.get(Constant.CFS_OP_CODE_KEY));
		}
		Property property_5 = propertyFactory.create();
		if (Constant.CFS_OPERATION_UPDATE_STAGE.equals(opretion_type)) {
			property_5.setCode(Constant.CFS_STAGE_FLAG_KEY);
			property_5.setValue((String)attr_map.get(Constant.CFS_STAGE_FLAG_KEY));
		}else{
			property_5.setCode(Constant.CFS_FLAG_KEY);
			property_5.setValue((String)attr_map.get(Constant.CFS_FLAG_KEY));
		}
		Property property_6 = propertyFactory.create();
		if (Constant.CFS_OPERATION_IMG_COUNT.equals(opretion_type)) {
			property_6.setCode(Constant.CFS_RES_IMG_COUNT_IMGLIST);
			property_6.setValue((String)request_map.get(Constant.CFS_RES_IMG_COUNT_IMGLIST));
		}
		ArrayList<Property> list = new ArrayList<Property>();
		list.add(property);
		list.add(property_1);
		if (Constant.CFS_OPERATION_IMG_COUNT.equals(opretion_type)) {
			if (Constant.CFS_QLCXD_BUSINESS.equals(fun_code)) {
				list.add(property_3);
			}else {
				list.add(property_2);
			}
			list.add(property_6);
		}else{
			list.add(property_2);
			list.add(property_3);
			list.add(property_5);
		}
		if (StringUtils.isNotEmpty((String)attr_map.get(Constant.CFS_OP_CODE_KEY))) {
			list.add(property_4);
		}
		ResponseMessageBeanFactory<ResponseMessageBean> messageFactory = ResponseMessageBean::new;
		ResponseMessageBean message = messageFactory.create();
		RequestOfMessageFactory<RequestOfMessage> requestFactory = RequestOfMessage::new;
		RequestOfMessage request = requestFactory.create();
		request.setProperties(list);
		ResponseOfMessageFactory<ResponseOfMessage> responseFactory = ResponseOfMessage::new;
		ResponseOfMessage response = responseFactory.create();
		ItemFactory<Item> item = Item::new;
		Item item_1 = item.create();
		item_1.setCode(Constant.CFS_IMG_RESULT_KEY);
		item_1.setValue((String)response_map.get(Constant.CFS_IMG_RESULT_KEY));
		Item item_2 = new Item();
		item_2.setCode(Constant.CFS_RESULT_REMARK_KEY);
		item_2.setValue((String)response_map.get(Constant.CFS_RESULT_REMARK_KEY));
		ArrayList<Item> results = new ArrayList<Item>();
		results.add(item_1);
		results.add(item_2);
		response.setResults(results);
		message.setCode((String)request_map.get(Constant.CFS_XMLROOTNODE_NAME));
		message.setRequest(request);
		message.setResponse(response);
		JaxbHelper jaxbHelper = new JaxbHelper(message.getClass());
		String xml = null;
		try {
			xml = jaxbHelper.objConvertXml(message,"UTF-8");
		} catch (Exception e) {
			throw new CFSImgException("xml 转换异常");
		}
		return xml;
	}
/**
 * 根据api生成xml文件
 * @param rows
 * @param batch_code
 * @param response_map
 * @param message_code
 * @return
 */
	public static String convertXmlOfApi(List rows,String batch_code, Map response_map,String message_code){
		Set keySet = response_map.keySet();
		ArrayList<Property> list = new ArrayList<Property>();
		for (Object object : keySet) {
			if (object instanceof String) {
				String key = (String)object;
				if (rows==null) {
					if (Constant.ApiResponseOfError.isConstins(key)) {
						Property property = new Property();
						property.setCode(key);
						property.setValue((String)response_map.get(key));
						list.add(property);
					}
				} else if(Constant.ApiResponseOfPublic.isConstins(key)){
					Property property = new Property();
					property.setCode(key);
					property.setValue((String)response_map.get(key));
					list.add(property);
				}
			}else{
				throw new RuntimeException();
			}
		}
		ApiOfResponseOfMessage message = new ApiOfResponseOfMessage();
		message.setCode(message_code);
		ApiOfResponse apiOfResponse = new ApiOfResponse();
		apiOfResponse.setProperties(list);
		apiOfResponse.setLists(null);
		message.setResponse(apiOfResponse);
		if (rows!=null&&rows.size()!=0&&Constant.CFS_OPERATION_TRANS_RES.equals(message_code)) {
			ApiOfResponseList apiOfResponseList = new ApiOfResponseList();
			ApiOfResponseLists lists = new ApiOfResponseLists();
			lists.setList(apiOfResponseList);
			apiOfResponseList.setCode("img_list");
			apiOfResponseList.setCount(rows.size()+"");
			apiOfResponseList.setBatch_code(batch_code);
			apiOfResponse.setLists(lists);
			ArrayList<ApiOfRow> row_list = new ArrayList<ApiOfRow>();
			for (Object row : rows) {
				ApiOfRow apiOfRow = new ApiOfRow();
				ArrayList<Item> results = new ArrayList<Item>();
				if (row instanceof Map) {
					Map item_map = (Map)row;
					Set itemSet = item_map.keySet();
					for (Object key : itemSet) {
						if (key instanceof String) {
							String itemKey = (String) key;
							if (Constant.ApiResponse.isConstins(itemKey)) {
								Item item = new Item();
								item.setCode(itemKey);
								item.setValue((String)item_map.get(itemKey));
								results.add(item);
							}
						}else{
							throw new RuntimeException();
						}
						apiOfRow.setResults(results);
					}
					row_list.add(apiOfRow);
				}else{
					throw new RuntimeException();
				}
			}
			apiOfResponseList.setRows(row_list);;
		}
		String xml = null;
		try {
			JaxbHelper jaxbHelper = new JaxbHelper(message.getClass());
			xml = jaxbHelper.objConvertXml(message,"utf-8");
			System.out.println(xml);
		} catch (Exception e) {
			throw new CFSImgException("xml 转换异常");
		}
		return xml;
	}
	/**
	 * 根据删除api生成xml文件，用于删除
	 * @param response_map
	 * @param result_map
	 * @param message_code
	 * @return
	 */
	public static String convertXmlOfDeleteApi(Map response_map,Map result_map,String message_code){
		Set keySet = response_map.keySet();
		DeleteApiOfResponseOfMessage message = new DeleteApiOfResponseOfMessage();
		ArrayList<Property> properties = new ArrayList<Property>();
		for (Object object : keySet) {
			if (object instanceof String) {
				String key = (String)object;
				if (Constant.DeleteApiResponse.isConstins(key)) {
					Property property = new Property();
					property.setCode(key);
					property.setValue((String)response_map.get(key));
					properties.add(property);
				} 
			}else{
				throw new RuntimeException();
			}
		}
		message.setCode(message_code);
		DeleteApiOfResponse deleteApiOfResponse = new DeleteApiOfResponse();
		deleteApiOfResponse.setProperties(properties);
		ArrayList<Item> results = new ArrayList<Item>();
		for(Object key:result_map.keySet()){
			if (key instanceof String) {
				String str_key = (String)key;
				Item item = new Item();
				item.setCode(str_key);
				item.setValue((String)result_map.get(str_key));
				results.add(item);
			} else {
				throw new RuntimeException();
			}
		}
		deleteApiOfResponse.setResults(results);
		message.setResponse(deleteApiOfResponse);
		String xml = null;
		try {
			JaxbHelper jaxbHelper = new JaxbHelper(message.getClass());
			xml = jaxbHelper.objConvertXml(message,"utf-8");
		} catch (Exception e) {
			throw new CFSImgException("xml 转换异常");
		}
		return xml;
	}
}