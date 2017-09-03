/**
 * 
 */
package starter.cfsrest.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;

/**
 * @author jackice
 * @company nko
 * @description
 * @date 2016年5月13日
 */
public class JaxbHelper {
	private JAXBContext jaxbContext;
	/**
	 * @param jaxbContext
	 */
	public JaxbHelper(Class...classes ) {
		try {
			jaxbContext = JAXBContext.newInstance(classes);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 创建Marshaller, 设定encoding(可为Null).
	 */
	public Marshaller createMarshaller(String encoding) {
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();

			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			if (StringUtils.isNotBlank(encoding)) {
				marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
			}
			return marshaller;
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
/**
 * 把xml对象转化成java对象
 * @return
 */
	public Unmarshaller createUnmarshaller() {
		try {
			return jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
/**
 * 生成xml格式文件
 * @param root
 * @param rootName
 * @param encoding
 * @return
 */
	@SuppressWarnings("unchecked")
	public String toXml(Collection root, String rootName, String encoding) {
		try {
			CollectionWrapper wrapper = new CollectionWrapper();
			wrapper.collection = root;

			JAXBElement<CollectionWrapper> wrapperElement = new JAXBElement<CollectionWrapper>(
					new QName(rootName), CollectionWrapper.class, wrapper);

			StringWriter writer = new StringWriter();
			createMarshaller(encoding).marshal(wrapperElement, writer);

			return writer.toString();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 把XML对象转化成java对象
	 * @param xml
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T fromXml(String xml) {
		try {
			StringReader reader = new StringReader(xml);
			return (T) createUnmarshaller().unmarshal(reader);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
/**
 * 把XML对象转化成java对象
 * @param xml
 * @param caseSensitive
 * @return
 */
	@SuppressWarnings("unchecked")
	public <T> T fromXml(String xml, boolean caseSensitive) {
		try {
			String fromXml = xml;
			if (!caseSensitive)
				fromXml = xml.toLowerCase();
			StringReader reader = new StringReader(fromXml);
			return (T) createUnmarshaller().unmarshal(reader);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 把XML对象转化成java对象
	 * @param xml
	 * @param c1
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")

	public  <T> T xmlConvertObject(String xml,Class<T> c1) throws Exception{
		T t=null;
		try {
			jaxbContext = JAXBContext.newInstance(c1);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			t = (T)unmarshaller.unmarshal(new StringReader(xml));
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new Exception(String.format("convert fail:%s",e.getMessage()));
		}
		return t;

	}
	/**
	 * 将java对象转化成xml对象
	 * @param obj
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public String objConvertXml(Object obj,String encoding) throws Exception{
		String result = null;
		try {
			Marshaller marshaller = createMarshaller(encoding);
			StringWriter writer = new StringWriter();
			marshaller.marshal(obj,writer);
			result = writer.toString();
		} catch (JAXBException e) {
			throw new Exception(String.format("convert fail:%s",e.getMessage()));
		}
		return result;
	}
	public static class CollectionWrapper {
		@SuppressWarnings("unchecked")
		@XmlAnyElement
		protected Collection collection;
	}
}
