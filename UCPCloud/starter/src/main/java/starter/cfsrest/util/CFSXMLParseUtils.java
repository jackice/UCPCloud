/**
 * 
 */
package starter.cfsrest.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.JSONObject;


/**
 * @author jackice
 * @company nko
 * @description
 * @date 2016年5月23日
 */
public class CFSXMLParseUtils {
	private static final String THE_SECONDARY_NODE_NAME="request";
	private static final String THE_THREE_NODE_NAME="properties";
	/** 
	 * 转换一个xml格式的字符串到json格式 
	 *  
	 * @param xml 
	 *            xml格式的字符串 
	 * @return 成功返回json 格式的字符串;失败反回null 
	 */  
	@SuppressWarnings("unchecked")  
	public static  String xml2JSON(String xml) {  
		JSONObject obj = new JSONObject();  
		try {  
			InputStream is = new ByteArrayInputStream(xml.getBytes("utf-8"));  
			SAXBuilder sb = new SAXBuilder();  
			Document doc = sb.build(is);  
			Element root = doc.getRootElement();  
			obj.put(root.getName(), iterateElement(root));  
			return obj.toString();  
		} catch (Exception e) {  
			e.printStackTrace();  
			return null;  
		}  
	}  

	/** 
	 * 转换一个xml格式的字符串到json格式 
	 *  
	 * @param file 
	 *            java.io.File实例是一个有效的xml文件 
	 * @return 成功反回json 格式的字符串;失败反回null 
	 */  
	@SuppressWarnings("unchecked")  
	public static String xml2JSON(File file) {  
		JSONObject obj = new JSONObject();  
		try {  
			SAXBuilder sb = new SAXBuilder();  
			Document doc = sb.build(file);  
			Element root = doc.getRootElement();  
			obj.put(root.getName(), iterateElement(root));  
			return obj.toString();  
		} catch (Exception e) {  
			e.printStackTrace();  
			return null;  
		}  
	}  

	/** 
	 * 一个迭代方法 
	 *  
	 * @param element 
	 *            : org.jdom.Element 
	 * @return java.util.Map 实例 
	 */  
	@SuppressWarnings("unchecked")  
	private static Map  iterateElement(Element element) {  
		List jiedian = element.getChildren();  
		Element et = null;  
		Map obj = new HashMap();  
		List list = null;  
		for (int i = 0; i < jiedian.size(); i++) {  
			list = new LinkedList();  
			et = (Element) jiedian.get(i); 
			List attributes = et.getAttributes();
			for (int j = 0; j < attributes.size(); j++) {
				System.out.println(attributes.get(j));
			}
			if (et.getTextTrim().equals("")) {  
				if (et.getChildren().size() == 0)  
					continue;  
				if (obj.containsKey(et.getName())) {  
					list = (List) obj.get(et.getName());  
				}  
				list.add(iterateElement(et));  
				obj.put(et.getName(), list);  
			} else {  
				if (obj.containsKey(et.getName())) {  
					list = (List) obj.get(et.getName());  
				}  
				list.add(et.getTextTrim());  
				obj.put(et.getName(), list);  
			}  
		}  
		return obj;  
	}  

//	public static Map<String,String> analysisXml(String xml){
//		Map<String,String> map = new HashMap<String,String>();
//		try {
//			//解析xml
//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();//获取实例
//			DocumentBuilder builder = factory.newDocumentBuilder();//使用当前配置创建新实例
//			InputSource source = new InputSource(new StringReader(xml));//使用字节流创建新的输入源
//			org.w3c.dom.Document doc = builder.parse(source);//解析为xml
//			//xml类型
//			String code = doc.getDocumentElement().getAttribute("code");
//			//系统编号
//			String sysCode = XPathAPI.selectSingleNode(doc, "//property[@code='sys_code']").getTextContent();
//			//功能编号
//			String funCode = XPathAPI.selectSingleNode(doc, "//property[@code='fun_code']").getTextContent();
//			//客户编号
//			String customCode = XPathAPI.selectSingleNode(doc, "//property[@code='custom_code']")==null?"":XPathAPI.selectSingleNode(doc, "//property[@code='custom_code']").getTextContent();
//			//业务号
//			String bussId = XPathAPI.selectSingleNode(doc, "//property[@code='buss_id']")==null?"":XPathAPI.selectSingleNode(doc, "//property[@code='buss_id']").getTextContent();
//			//操作员
//			String opCode = XPathAPI.selectSingleNode(doc, "//property[@code='op_code']")==null?"":XPathAPI.selectSingleNode(doc, "//property[@code='op_code']").getTextContent();
//			//操作标识
//			String flag = XPathAPI.selectSingleNode(doc, "//property[@code='flag']").getTextContent();
//			
//			map.put(Constant.NODE_ATTR_NAME, code);
//			map.put(Constant.CFS_SYS_CODE_KEY, sysCode);
//			map.put(Constant.CFS_FUN_CODE_KEY, funCode);
//			map.put(Constant.CFS_CUSTOM_CODE_KEY, customCode);
//			map.put(Constant.CFS_BUSS_ID_KEY, bussId);
//			map.put(Constant.CFS_OP_CODE_KEY, opCode);
//			map.put(Constant.CFS_FLAG_KEY, flag);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return map;
//	}
	/**
	 * 获取xml的节点属性和value值
	 * @param xml
	 * @param rootNodeAttrName
	 * @param defineListMapKey
	 * @return map
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map getXmlAttrAndValue(String xml,String rootNodeAttrName,String defineListMapKey) throws Exception{
		Element element = getRootElement(xml);
		String rootElementName = element.getName();
		String rootAttrValue = null;
		try {
			Attribute attribute = element.getAttribute(rootNodeAttrName);
			rootAttrValue = attribute.getValue();
		} catch (NullPointerException e) {
			throw new Exception(String.format("The %s does not exist",rootNodeAttrName));
		}
		Map  resultBundle = new HashMap();
		Map attrValues_Text_Map = getNodeAttrs(element);
		resultBundle.put(rootElementName, rootAttrValue);
		resultBundle.put(defineListMapKey, attrValues_Text_Map);
		return resultBundle;
	}
/**
 * 获取xml的节点属性和value值
 * @param xml
 * @param rootNodeAttrName
 * @param defineListMapKey
 * @return map
 * @throws Exception
 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map getSpecialXmlAttrAndValue(String xml,String rootNodeAttrName,String... defineListMapKey) throws Exception{
		Element element = getRootElement(xml);
		String rootElementName = element.getName();
		String rootAttrValue = null;
		try {
			Attribute attribute = element.getAttribute(rootNodeAttrName);
			rootAttrValue = attribute.getValue();
		} catch (NullPointerException e) {
			throw new Exception(String.format("The %s does not exist",rootNodeAttrName));
		}
		Map  resultBundle = new HashMap();
		Map attrValues_Text_Map = getNodeAttrs(element);
		List listNodeAttrs = getListNodeAttrs(element);
		resultBundle.put(rootElementName, rootAttrValue);
		resultBundle.put(defineListMapKey[0], attrValues_Text_Map);
		resultBundle.put(defineListMapKey[1], listNodeAttrs);
		return resultBundle;
	}
	/**
	 * 获取根元素的所有属性节点
	 * @param rootElement
	 * @return list
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List getListNodeAttrs(Element rootElement) throws Exception{
		Element secondaryNode = rootElement.getChild(THE_SECONDARY_NODE_NAME);
		ArrayList<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		if (secondaryNode!=null) {
			Element threeNode = secondaryNode.getChild("lists");
			if (threeNode!=null) {
				Element listElement = threeNode.getChild("list");
				Attribute count = listElement.getAttribute("count");
				int intValue = 0;
				try {
					intValue = count.getIntValue();
				} catch (DataConversionException e) {
					throw new Exception(String.format("The attributes: %s requied","count"));
				}
				List<Element> list = listElement.getChildren();
				int i = 0;
				for (Element rowElement:list) {
					if (i>intValue) {
						break;
					}
					Map attrValue = new HashMap();
					//			Element rowElement = listElement.getChild("row");
					List nodes = rowElement.getChildren();
					Element nodeElement = null; 
					List<Attribute> attributes = null;
					for (int j = 0; j < nodes.size(); j++) {  
						nodeElement = (Element) nodes.get(j); 
						attributes = nodeElement.getAttributes();
						for (Attribute attr : attributes) {
							attrValue.put(attr.getValue(),nodeElement.getTextTrim());
						}
					}
					resultList.add(attrValue);
					i++;
				}
			}
		}
		return resultList;
	}
	/**
	 * 获取根元素的所有属性节点
	 * @param rootElement
	 * @return map
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map getNodeAttrs(Element rootElement){
		Map attrValue = new HashMap();
		Element secondaryNode = rootElement.getChild(THE_SECONDARY_NODE_NAME);
		if (secondaryNode!=null) {
			Element threeNode = secondaryNode.getChild(THE_THREE_NODE_NAME);
			if (threeNode!=null) {
				List nodes = threeNode.getChildren();
				Element nodeElement = null; 
				List<Attribute> attributes = null;
				for (int i = 0; i < nodes.size(); i++) {  
					nodeElement = (Element) nodes.get(i); 
					attributes = nodeElement.getAttributes();
					for (Attribute attr : attributes) {
						attrValue.put(attr.getValue(),nodeElement.getTextTrim());
					}
				}
			}
		}
		return attrValue;
	}
/**
 * 获取根元素
 * @param xml
 * @return element
 */
	private static Element getRootElement(String xml){
		try {  
			InputStream is = new ByteArrayInputStream(xml.getBytes("utf-8"));  
			SAXBuilder sb = new SAXBuilder();  
			Document doc = sb.build(is);  
			Element root = doc.getRootElement();  
			return root;  
		} catch (Exception e) {  
			e.printStackTrace();  
			return null;  
		} 
	}
}  
