/**
 * @date 2015年6月8日
 * @author jackice
 * @company nko
 * @description
 */
package starter.cfsrest.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class SimulatorDataReader {
	private static final String SIMULATOR_XML_DATA_PATH = "D:\\simulator\\xml\\";
	private static final String SIMULATOR_TV_DATA_PATH = "D:\\simulator\\tv\\";
	/**
	 * @description 读取xml模拟数据
	 * @param dataFileName	xml文件名
	 * @return
	 * @version 1.0
	 */
	public static String readerSimulatorXmlData(String dataFileName){
		return readerSimulatorData(dataFileName,SIMULATOR_XML_DATA_PATH,"xml");
	}
	/**
	 * @description 通用读取模拟数据函数
	 * @param workPath 文件所在目录
	 * @param dataFileName 文件名，不包含扩展名
	 * @param extension	扩展名
	 * @return
	 * @version 1.0
	 */
	public static String readerSimulatorData(String dataFileName,String workPath,String extension){
//		DataFileHelper dfh = new DataFileHelper();
//		File xmlFile = new File(dfh.getClassPathFilePath(xmlName));
		File file = new File(workPath+dataFileName+"."+extension);
		StringBuffer buffer = new StringBuffer("");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = null;
			while((line=reader.readLine()) != null){
				line = line.replaceAll("\n|\r|\n\r|\r\n", "");
				buffer.append(line);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buffer.toString();
	}
	
//	static class DataFileHelper{
//		public String getClassPathFilePath(String name){
//			return getClass().getClassLoader().getResource("simulator/data/"+name+".xml").getPath();
//		}
//	}

	public static String readSimulatorTVData(String dataFileName) {
		return readerSimulatorData(dataFileName,SIMULATOR_TV_DATA_PATH,"tv");
	}
}


