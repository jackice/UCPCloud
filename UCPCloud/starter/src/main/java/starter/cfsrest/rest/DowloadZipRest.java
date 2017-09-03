package starter.cfsrest.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import starter.uContentException;
import starter.cfsrest.bean.CFSConfig;
import starter.cfsrest.bean.Constant;
import starter.cfsrest.service.CFSCommonService;
import starter.cfsrest.service.CFSStreamService;
import starter.cfsrest.service.CFSTreeService;
import starter.cfsrest.service.ZipOutStream;
import starter.cfsrest.util.ZipUtil;
import starter.rest.Json;




@RestController
@RequestMapping(value = "/store", produces = MediaType.APPLICATION_JSON_VALUE)
public class DowloadZipRest {
	@Autowired
	private CFSTreeService cfsTreeService;
	@Autowired
	private CFSConfig cfsConfig;
	@Autowired
	private CFSCommonService cfsCommonService;
	@Autowired
	private CFSStreamService streamService;//图像流生成类
	@Autowired
	private ZipOutStream zipOutStream;//压缩包下载类
	private Logger logger = Logger.getLogger(DowloadZipRest.class);
	/*
	 * 查看控件下载zip压缩包
	 */
	@RequestMapping(value = "/batchDownload", method = { RequestMethod.GET,RequestMethod.POST}, produces = MediaType.ALL_VALUE)
	public void query(HttpServletRequest request, 
			HttpServletResponse response,
			@RequestParam(defaultValue = "") String CustomerCode,
			@RequestParam(defaultValue = "") String BusinessCode,
			@RequestParam(defaultValue = "") String BisInfCode,
			@RequestParam(defaultValue = "") String FunctionCode,
			@RequestParam(defaultValue = "") String CheckFlag
			) {
		/*
		 * 首先判断FunctionCode是QLCXD_CUSTOMER还是QLCXD_BUSINESS
		 * 当FunctionCode=QLCXD_CUSTOMER时，那么就根据CustomerCode进行下载
		 * 当FunctionCode=QLCXD_BUSINESS时，那么就根据BusinessCode进行下载
		 */
		String code = BusinessCode;
		if(Constant.CFS_QLCXD_CUSTOMERS.equals(FunctionCode)){
			code = CustomerCode;
		}
		//根据业务号、客户编号、索引类型获取到图像ID号和分类编码健值对
//		String[] types = {Constant.CFS_MASTERINDEX_TYPE};
		String[] types =  new String[1];
		/*
		 * 当CheckFlag=2时，下载的是Copy表中的相关内容
		 * 当CheckFlag=1或者3时，下载的时Master表中的相关内容
		 */
		if(CheckFlag.equals("2")){
			types[0] = Constant.CFS_COPYEINDEX_TYPE;
		}else if(CheckFlag.equals("3")){
			types[0]=Constant.CFS_UPDATEINDEX_TYPE;
		}else{
			types[0] = Constant.CFS_MASTERINDEX_TYPE;
		}
		
		Map map = null;
		try {
			map = cfsCommonService.queryZipData(BusinessCode, CustomerCode,FunctionCode, types,BisInfCode);
//			System.out.println(map.toString());
		} catch (IOException e) {
			logger.error("download fail to query metedata:"+e);
			throw new uContentException("下载失败", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		List listes = null;
		if (map!=null&&!map.isEmpty()) {
			Set set = map.keySet();//获取图片ID集合
			listes = new ArrayList();
			listes.addAll(set);//转换为List集合
			String path = cfsConfig.getDownloadzip() + File.separator + "temp" + File.separator + code;
//			File[] files = new File[listes.size()];//准备把得到结点图像文件装进数组里，以便整体打包
			for (int i = 0; i < listes.size(); i++) {//图片ID号遍历 获取流二进制文件
				try {
					//根据图片ID号、索引获取图像流ID号
//					XContentBuilder xDocumentB = streamService.get(Constant.CFS_MASTERINDEX_TYPE,listes.get(i).toString());
					XContentBuilder xDocumentB = streamService.get(types[0],listes.get(i).toString());
					String string = xDocumentB.string();
					if(string.equals("[]") ){
						continue;
					}
					StringBuilder ss=new StringBuilder(string);
					ss.deleteCharAt(string.length()-1);
					ss.deleteCharAt(0);
					
					
					Map map2=new HashMap();
					map2=Json.parseToMap(ss.toString());
					String steamId=(String) map2.get("streamId");
					
//					String[] s = string.split("\"");//解释字符串
					//获取图像二进制map值
					Map <String, Object> map1 = streamService.getStream(types[0], listes.get(i).toString(), steamId);
					byte[] bytes = (byte[]) map1.get("bytes");
					// System.out.println(map.get(listes.get(i).toString()));//分类编码
					//以下是通过分类编码，调取解释类得图像存放的路径
					if (bytes == null || bytes.length == 0){
						continue;
					}
					List docK = new ArrayList();//fyh
					docK = (List) map.get(listes.get(i).toString());//fyh
					String path2 = path
							+ getURI(docK.get(0).toString());
					File file = new File(path2);
					if (!file.exists()) {//创建树结构一个分支，多次循环即可获得目录树
						file.mkdirs();
					}
					String docKide = null;//fyh
					if(StringUtils.isEmpty(docK.get(1).toString())){	//如果后缀为空的话，则设置为.jpg			
						docKide = ".jpg";	//fyh
					}else{
						docKide = docK.get(1).toString();//设置后缀为指定类型
					}
					File file2 = null;
					
/*
 * 如果图片名称不为空，1>图片名称包含后缀 ，则直接使用 2>不包含后缀，要加上后缀
 * 如果图片名称为空，则图片名称就是图片id					
 */
					if(!docK.get(2).equals(null)){
						String imageName=docK.get(2).toString();
						if(!imageName.matches("^[\\s\\S]*\\.[^\\.]{3,4}$")){
							file2 = new File(path2 + File.separator + docK.get(2).toString() + docKide);//fyh
						}else{
							file2 = new File(path2 + File.separator + docK.get(2).toString());//fyh
						}
					}else{
					
						file2 = new File(path2 + File.separator + listes.get(i).toString() + docKide);//fyh
					}
					
					FileOutputStream fos = new FileOutputStream(file2);
					fos.write(bytes);
					fos.flush();
					fos.close();

//					files[i] = file2;//把图像文件装进数组，以便整体打包

				}catch(FileNotFoundException e){
					logger.error("file streams not find:"+e);
					throw new uContentException("下载失败", HttpStatus.INTERNAL_SERVER_ERROR);
				}catch (IOException e) {
					logger.error("download fail:"+e);
					throw new uContentException("下载失败", HttpStatus.INTERNAL_SERVER_ERROR);
				}catch(Exception e){
					logger.error("download fail:"+e);
					throw new uContentException("下载失败", HttpStatus.INTERNAL_SERVER_ERROR);
				}

			}
			/**
			 * 根据图像文件数组调用压缩工具类进行压缩
			 */
			File dirfile = new File(path);
//			System.out.println(path);
//			try{
//			boolean directory = dirfile.isDirectory();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
			if (dirfile.isDirectory()){
				String[] files = dirfile.list();
				if(files.length > 0){
					String FilePath = cfsConfig.getDownloadzip() + File.separator + "temp" + File.separator;
					try {
						String tempFileName = code + ".zip";
						ZipUtil.zip(path, FilePath+tempFileName);
						this.zipOutStream.service(request, response,tempFileName,FilePath);
						//删除临时目录
						//File file5 = new File(Path);
						File file5 = new File(FilePath);
						this.deleteFile(file5);
						//删除压缩包
						//File file6 = new File(FilenameUtils.concat(FilePath, code+Constant.CFS_COMPRESS_FILE_POSTFIX));		
						//file6.delete();
					} catch (IOException | ServletException e) {
						throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}else{
					logger.error("not find file streams");
					throw new uContentException("下载失败", HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}else{
				logger.error("not find file streams");
				throw new uContentException("下载失败", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}else{
			logger.error(String.format("custom_code:%s buss_id:%s ,not find image data", CustomerCode,BusinessCode));
			throw new uContentException("下载失败",HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/*
	 * 根据type_code获取图片路径
	 */
	public  String getURI(String type_code){
		//如果type_code为空或者为0，设置路径为未分类
		if (StringUtils.isBlank(type_code)||Constant.CFS_COMMON_DIGIT_STRING_ZERO.equals(type_code)||StringUtils.isEmpty(type_code)) {
			return File.separator + Constant.CFS_UNCLASSIFIED_FOLDER_NAME;
		}
		try {
			//获取到目录树
			XContentBuilder xContentBuilder = cfsTreeService.query(Constant.CFS_TREEINDEX_TYPE,0,1000, type_code,Constant.CFS_COMMON_DIGIT_STRING_ONE);
			logger.info(xContentBuilder.string());
			Map map=new HashMap();
			Map map2=new HashMap();
//			System.out.println(xContentBuilder.string());
			map=Json.parseToMap(xContentBuilder.string());//把xContentBuilder转换成map集合
			ArrayList arr= (ArrayList) map.get(Constant.CFS_DOCUMENTS_STRING);//获取文档部分
			StringBuilder stringBuilder1=new StringBuilder();//拼接type_code
			StringBuilder stringBuilder2=new StringBuilder();//拼接路径
//			Date date=new Date(); 
			stringBuilder2.append(Constant.CFS_EMPTY_STRING);
			List <String>list=new ArrayList<String>();//用于分解type_code之后的字符串
			String id1 = type_code.substring(0,2);//type_code截取前两位，因为后面都是截取三位，所以这里单独列出
			list.add(id1);// 把截取到得放到集合
			/*
			 * 截取type_code除前两位后面的字符串
			 */
			for(int i=2;i<type_code.length();i=i+3){
				list.add(type_code.substring(i, i+3));
			}
			ArrayList arr2=new ArrayList();
			for(int j=0;j<list.size();j++){
				/*
				 * 循环的第一个需要单独处理，因为第一个并没有在childrens属性中
				 */
				if(j==0){
					stringBuilder1.append(list.get(j));	
					map2=(Map) arr.get(0);//document元素中只有一个对象
					stringBuilder2.append(Constant.CFS_BACKSLASH_DOUBLE+map2.get(Constant.CFS_TREE_MSG_NAME));
					arr2=(ArrayList)map2.get("childrens");
					continue;
				}
				stringBuilder1.append(list.get(j));
				for (int k = 0; k < arr2.size(); k++) {
					map2=(Map) arr2.get(k);
					if(map2.get(Constant.CFS_TREE_MSG_CODE).equals(stringBuilder1.toString())){
						stringBuilder2.append(Constant.CFS_BACKSLASH_DOUBLE+map2.get(Constant.CFS_TREE_MSG_NAME));
						break;
					}
				}
				if(j+1<list.size()){
					arr2=(ArrayList) map2.get("childrens");
				}
			}
			return stringBuilder2.toString();
		} catch (IOException e) {
			throw new uContentException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	} 

	/**
	 * 此方法是递归删除
	 * @param file
	 */
	public void deleteFile(File file) {
		try {
			if (file.isDirectory()) {
				for (File sub : file.listFiles()) {
					deleteFile(sub);
				}
			}
			file.delete();
		} catch (Exception e) {
			throw new uContentException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

}
