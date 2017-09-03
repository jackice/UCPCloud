package starter.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import starter.uContentException;
import starter.cfsrest.bean.CFSConfig;
import starter.cfsrest.util.DownLoadUtils;
import starter.cfsrest.util.ZipUtil;
import starter.rest.Json;

@Service
public class DownloadSerive {
	@Autowired
	private StreamService streamService;
	@Autowired
	private CFSConfig cfsConfig;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private DownLoadUtils downLoadUtils;
	private Logger logger = Logger.getLogger(DownloadSerive.class);
	/**
	 * 下载功能
	 * @param type 类型
	 * @param query 查询条件
	 * @param DisctoryStructure 节点编码和名字
	 * @param fulltext 是否全文查询
	 * @param start查询开始条数
	 * @param limit查询条数
	 * @param sort排序规则
	 * @param allowableActions是否返回权限设置
	 * @param containsType是否返回类型
	 * @param request
	 * @param response
	 */
	public void downLoad(String type, String query, String DisctoryStructure,
			boolean fulltext, int start, int limit, SortBuilder[] sort,
			boolean allowableActions, boolean containsType,
			HttpServletRequest request, HttpServletResponse response) {

		// tempPath 下载的文件所在的临时目录，最后删除
		String tempPath = cfsConfig.getDownloadzip() + File.separator
				+ "ziptemp";
		File file1 = new File(tempPath);
		file1.setExecutable(true);
		file1.setReadable(true);
		file1.setWritable(true);
		String[] types = { type };
		XContentBuilder xContentBuilder;
		try {
			// 检查目录树的合法性
			if (!checkLegality(DisctoryStructure)) {
				logger.error("the DisctoryStructure is illegal!!");
				throw new uContentException("the DisctoryStructure is illegal!!", HttpStatus.INTERNAL_SERVER_ERROR);
				//throw new Exception("the DisctoryStructure is illegal!!");
			}
			// 根据条件进行查询
			xContentBuilder = this.documentService.query(types, query, start,
					limit, sort, allowableActions, fulltext, containsType);
			// 将XContentBuilder类型数据转化为Map<String,Object>类型进行解析
			Map<String, Object> mapJson = Json.parseToMap(xContentBuilder
					.string());
			// 获取map里面key为documents的value值，并转化为ArrayList<Map<String,Object>> 格式
			// 另外，map的key里面还有一个total
			ArrayList<Map<String, Object>> listMap = (ArrayList<Map<String, Object>>) mapJson
					.get("documents");

			HashMap<String, ArrayList<Object>> map = new HashMap<String, ArrayList<Object>>();
			// 循环遍历获取需要的值，包括_id，IMAGE_NAME，IMAGE_ID，TYPE_CODE
			for (int i = 0; i < listMap.size(); i++) {

				Map<String, Object> mapList = listMap.get(i);
				String id = (String) mapList.get("_id");
				String IMAGE_NAME = (String) mapList.get("IMAGE_NAME");
				String IMAGE_ID = (String) mapList.get("IMAGE_ID");
				String TYPE_CODE = (String) mapList.get("TYPE_CODE");
				// 根据type和id查询streamid
				XContentBuilder xDocumentB = streamService.get(type, id);
				String string = xDocumentB.string();
				if (string.equals("[]")) {
					continue;
				}
				StringBuilder ss = new StringBuilder(string);
				ss.deleteCharAt(string.length() - 1);
				ss.deleteCharAt(0);

				Map map1 = new HashMap();
				map1 = Json.parseToMap(ss.toString());
				String steamId = (String) map1.get("streamId");
				// 根据type,id, steamId获取流文件
				Map<String, Object> map2 = streamService.getStream(type, id,
						steamId);
				byte[] _stream = (byte[]) map2.get("bytes");
				if(_stream==null){
					logger.error("The file IMAGE_ID="+IMAGE_ID+" doesn't has _stream");
				}
				// 将每一张图片的信息存储到list集合
				ArrayList<Object> list = new ArrayList<Object>();
				list.add(IMAGE_NAME);
				list.add(TYPE_CODE);
				list.add(IMAGE_ID);
				list.add(_stream);
				//判断是否有FILE_TYPE字段，如果有，则添加
				if(mapList.containsKey("FILE_TYPE")){
					String FILE_TYPE = (String) mapList.get("FILE_TYPE");
					//如果FILE_TYPE为空，则设置为.jpg
					if(FILE_TYPE==null||FILE_TYPE.length()==0){
						list.add(".jpg");
					}else{
						list.add(FILE_TYPE);
					}
				}
				map.put(id, list);
			}
			// 将目录树转化为ArrayList<Map<String, Object>>类型
			ArrayList<Map<String, Object>> list = this
					.stringToMap(DisctoryStructure);

			Set<String> set = map.keySet();
			ArrayList<String> list2 = new ArrayList<String>(set);
			for (int i = 0; i < list2.size(); i++) {
				String IMAGE_NAME = (String) map.get(list2.get(i)).get(0);
				String TYPE_CODE = (String) map.get(list2.get(i)).get(1);
				String IMAGE_ID = (String) map.get(list2.get(i)).get(2);
				ArrayList<String> listFinally = new ArrayList<String>();
				// 根据type_code和目录树 查询 图片所在的目录树
				if (TYPE_CODE == null || TYPE_CODE.equals("0")) {
					listFinally.add("未分类");
				} else {
					listFinally = this.checkChildrensTypeCode(TYPE_CODE, list,
							listFinally);
				}

				if (listFinally.size() == 0) {
					listFinally.add("未分类");
				}

				byte[] _stream = (byte[]) map.get(list2.get(i)).get(3);
				StringBuilder path = new StringBuilder(tempPath);
				// 拼接文件路径
				for (int j = 0; j < listFinally.size(); j++) {
					path.append(File.separator);
					path.append(listFinally.get(j));

				}
				File file = new File(path.toString());
				if (!file.exists()) {
					file.mkdirs();
				}

				File file2 = null;
				File file3 = new File(path.toString());
				// 如果没有IMAGE_NAME，则设置图片名字为IMAGE_ID
				if (IMAGE_NAME == null || IMAGE_NAME.length() == 0) {
					//如果有FILE_TYPE字段，则加上
					if(map.get(list2.get(i)).size()==5){
						String type_file=(String) map.get(list2.get(i)).get(4);
						String imageName=IMAGE_ID+type_file;
						imageName=this.checkSameName(imageName, file3);
						file2 = new File(path.toString() + File.separator
								+ imageName);	
					}else{
						IMAGE_ID = this.checkSameName(IMAGE_ID, file3);
						file2 = new File(path.toString() + File.separator
								+ IMAGE_ID);	
					}
				} else {
					if(map.get(list2.get(i)).size()==5){
						String type_file=(String) map.get(list2.get(i)).get(4);
						if(!IMAGE_NAME.matches("^[\\s\\S]*\\.[^\\.]{3,4}$")){
							IMAGE_NAME=IMAGE_NAME+type_file;
						}
						
						IMAGE_NAME=this.checkSameName(IMAGE_NAME, file3);
						file2 = new File(path.toString() + File.separator
								+ IMAGE_NAME);	
					}else{
						IMAGE_NAME = this.checkSameName(IMAGE_NAME, file3);
						file2 = new File(path.toString() + File.separator
								+ IMAGE_NAME);
					}
				}

				FileOutputStream fos = new FileOutputStream(file2);
				fos.write(_stream);
				fos.flush();
				fos.close();
			}

			String filezip = cfsConfig.getDownloadzip() + File.separator
					+ "temp";
			File file2 = new File(filezip);
			if (!file2.exists()) {
				file2.mkdirs();
			}

			if (file1.exists()) {
				File[] files = file1.listFiles();
				if (files.length > 0) {
					Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyyMMddHHMMss");
					String zipDate = sdf.format(date);
					// 压缩文件
					ZipUtil.zip(tempPath, filezip + File.separator + zipDate
							+ ".zip");
					// 页面下载文件
					this.downLoadUtils.service(request, response, filezip,
							zipDate + ".zip");
				} else {
					logger.error("not find file streams");
					throw new uContentException("download failed",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

			} else {
				logger.error("temp file not find:");
				throw new uContentException("download failed",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (FileNotFoundException e) {
			logger.error("file streams not find:" + e);
			throw new uContentException("download failed",
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			logger.error("download fail:" + e);
			throw new uContentException("download failed",
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("download fail:" + e.getMessage());
			throw new uContentException("download failed",
					HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			// 删除临时目录
			if (file1.exists()) {
				this.deleteFiles(file1);
			}
			File file2 = new File(cfsConfig.getDownloadzip() + File.separator
					+ "temp");
			if (file2.exists()) {
				this.deleteFiles(file2);
			}
		}

	}

	/**
	 * 检查type DisctoryStructure的合法性
	 * 
	 * @param DisctoryStructure
	 * @return
	 */
	private boolean checkLegality(String DisctoryStructure) {

		try {
			stringToMap(DisctoryStructure);
			return true;
		} catch (Exception e) {
			logger.error("The string of disctoryStructure is illegal！Please check it!");
			return false;
		}

	}

	/**
	 * 将字符串转换成json格式，并组成list集合
	 * 
	 * @param str
	 * @return
	 */
	public ArrayList<Map<String, Object>> stringToMap(String str) {
		ArrayList<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
		char[] stringChars = str.toCharArray();
		// 用于存储字符串中'{'所在位置的集合
		ArrayList<Integer> listLeft = new ArrayList<Integer>();
		// 用于存储字符串中'}'所在位置的集合
		ArrayList<Integer> listRight = new ArrayList<Integer>();

		// 获取'{'和'}'的位置
		for (int i = 0; i < stringChars.length; i++) {
			if (stringChars[i] == '{') {
				listLeft.add(i);
			}
			if (stringChars[i] == '}') {
				listRight.add(i);
			}
		}
		//当大括号的数量为0时，说明格式错误，或者没有内容
		if(listLeft.size()==0||listRight.size()==0){
			logger.error("The string of disctoryStructure is  illegal!");
			throw new RuntimeException("The string of disctoryStructure is illegal!");
		}
		// 升序排列
		Collections.sort(listLeft);
		Collections.sort(listRight);
		// 当'{'和'}'数量不等时，说明格式错误
		if (listLeft.size() != listRight.size()) {
			logger.error("The string of disctoryStructure is illegal because the amount of '{'and the amount of '}' are different!");
			throw new RuntimeException(
					"The string of disctoryStructure is illegal because the amount of '{'and the amount of '}' are different!");
		}
		//当第一个'}'的位置在'{'之前，说明格式错误
		if(listRight.get(0)<listLeft.get(0)){
			logger.error("The string of disctoryStructure is illegal!");
			throw new RuntimeException("The string of disctoryStructure is illegal!");
		}
		
		/*
		 * 判断左右大括号的位置，避免出现类似'{}}}{{' 或者'{}}{{}'的情况
		 */
		for(int i=0;i<listLeft.size();i++){
			int left1=listLeft.get(i);
			int sum=0;
			for(int j=0;j<listRight.size();j++){
				int right1=listRight.get(j);
				if(right1<left1){
					sum++;
				}		
			}
			if(sum>i){
				logger.error("The string of disctoryStructure is illegal!");
				throw new RuntimeException("The string of disctoryStructure is illegal!");
			}
		}
		
		/*
		 * map存储对应的'{'和'}'的位置
		 */
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		/*
		 * 循环遍历'}'的位置,找到'{'的位置，因为'{'的位置一定在对象的'}'前面，这样容易判断
		 * 找到和'}'位置最近的'{',即判断Right - Left的大小，
		 * 一定要小于Right本身的大小，防止'{'的位置为0，也一定大于0,因为与之相对应的'{'一定在前面
		 * 找到相应的{}，放入map集合中，然后把找到的'{'的位置删除，以免影响后面循环的判断
		 */
		for (int i = 0; i < listRight.size(); i++) {

			int length = listRight.get(i);
			int left = 0;
			for (int j = 0; j < listLeft.size(); j++) {
				if (listRight.get(i) - listLeft.get(j) <= length
						&& listRight.get(i) - listLeft.get(j) > 0) {
					length = listRight.get(i) - listLeft.get(j);
					left = j;
				}
			}
			map.put(listLeft.get(left), listRight.get(i));
			listLeft.remove(left);
		}
		/*
		 * map2用于存储最外层的{}的位置
		 */
		HashMap<Integer, Integer> map2 = new HashMap<Integer, Integer>();

		Set<Integer> set = map.keySet();
		ArrayList<Integer> list = new ArrayList<Integer>(set);
		Collections.sort(list);
		/*
		 * 第一个'{'一定是每个集合最外层的 每次循环进行判断，判断left与right的大小，
		 * 当left>right时，设置right=left,然后继续循环 这样就可以获得每个集合最外层{}所在的位置
		 */
		int right = map.get(list.get(0));

		for (int i = 0; i < list.size();) {
			if (map.get(list.get(i)) < right) {
				i++;
			} else {
				right = map.get(list.get(i));
				map2.put(list.get(i), right);
				i++;
			}

		}
		Set<Integer> set2 = map2.keySet();
		Iterator<Integer> it = set2.iterator();
		/*
		 * 根据{}的位置，截取字符串 然后转换成Map<String, Object>类型
		 */
		try {
			while (it.hasNext()) {
				int left = it.next();
				int right2 = map2.get(left);
				String json = str.substring(left, right2 + 1);
				Map<String, Object> jsonMap = Json.parseToMap(json);
				listMap.add(jsonMap);
			}
		} catch (Exception e) {
			logger.error(
					"The string of disctoryStructure is illegal！Please check it!",
					e);
			throw new RuntimeException(
					"The string of disctoryStructure is illegal！Please check it!");
		}
		return listMap;
	}

	/**
	 * 获取目录树结构集合 
	 * 根据查找到的type_code时候是否图片所在的type_code的前缀，判断是否是在当前目录下
	 * 
	 * @param type_code
	 * @param listDisctoryStructure
	 * @param listFinally
	 * @return
	 * @throws IOException
	 */
	public ArrayList<String> checkChildrensTypeCode(String type_code,
			ArrayList<Map<String, Object>> listDisctoryStructure,
			ArrayList<String> listFinally) throws IOException {
		int sum = 0;
		for (int i = 0; i < listDisctoryStructure.size(); i++) {
			Map<String, Object> map = listDisctoryStructure.get(i);
			String type_code2 = (String) map.get("type_code");
			if (type_code.startsWith(type_code2)) {
				if (type_code2.equals(type_code)) {
					listFinally.add((String) map.get("type_name"));
					break;
				} else {
					String s = (String) map.get("type_name");
					listFinally.add(s);
					if (map.containsKey("childrens")) {
						ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) map
								.get("childrens");
						this.checkChildrensTypeCode(type_code, list,
								listFinally);
						break;
					}
					;
				}
			}
			sum++;
		}
		// 如果没找到添加到未分类中
		if (listDisctoryStructure.size() == sum) {
			listFinally.add("未分类");
		}
		return listFinally;
	}

	/**
	 * 删除目录
	 * 
	 * @param file
	 */
	private void deleteFiles(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {

				if (!files[i].isDirectory()) {

					files[i].delete();
				} else {
					deleteFiles(files[i]);
				}

			}
			file.delete();

		} else {
			file.delete();
		}

	}

	/**
	 * 检查目录下有没有重名的文件，有的话在后面加上"(数字)"
	 * 
	 * @param image_name
	 * @param file
	 * @return
	 */
	private String checkSameName(String image_name, File file) {
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			/*
			 * 当文件中有重名的进入条件
			 */
			if (files[i].getName().equals(image_name)) {
				String name=image_name;
				int m = 1;//()中的数字从1开始
				while (true) {
					// image_name=image_name+"("+m+")";
					char[] images = image_name.toCharArray();
					int index = -1;
					/*
					 * 找到第一个'.'的位置,添加（m）应该在这个点之前
					 * 当文件中不存在点的时候以及.在最开始的时候，即index==-1||index==0这两种情况是不会出现的
					 * 不会出现的原因：1针对index==0,说明.在最开始的位置，下载功能中，当文件名为空的时候会用IMAGE_ID代替文件名
					 * 			   2针对index==-1,说明.的位置不存在，下载功能中，当后缀名不存在的时候，默认会在后面加上.jpg
					 */
					for (int k = 0; k < images.length; k++) {
						if (images[k] == '.') {
							index = k;
							break;
						}
					}
					
					if (index != 0&&index!=-1) {
						String name1 = image_name.substring(0, index);
						image_name = name1
								+ "("
								+ m
								+ ")"
								+ image_name.subSequence(index,
										image_name.length());
					}else if(index==0){
						logger.error("the file's image_name is null!");
						throw new uContentException("the file's image_name is null!",
								HttpStatus.INTERNAL_SERVER_ERROR);
					}else if(index==-1){
						logger.error("the file's suffix is null!");
						throw new uContentException("the file's suffix is null!",
								HttpStatus.INTERNAL_SERVER_ERROR);
						
					}
					/*
					 *用于判断是否有重名，当sum==files.length时，说明没有重名 
					 */
					int sum=0;
					/*
					 *再次判断新生成的image_name有没有重名
					 *如果有，那么m++;
					 *并且把image_name恢复到最初状态
					 *继续while循环
					 * 直到sum==files.length
					 */
					for (int j = 0; j < files.length; j++) {
						if (files[j].getName().equals(image_name)) {
							//int lastLeft = image_name.lastIndexOf("(");
							//name = image_name.substring(0, lastLeft);
							image_name=name;
							m++;
							break;
						}
						sum++;
					}
					
					if(sum==files.length){
						break;
					}
					
					/*if (name.length() != 0) {
						image_name = name;
					} else {
						break;
					}*/
				}
				
				break;
			}
		}
		return image_name;
	}
}
