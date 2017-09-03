/**
 * 
 */
package starter.cfsrest.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import starter.FileSizeLimit;
import starter.uContentException;
import starter.cfsrest.bean.CFSLogBean;
import starter.cfsrest.bean.Constant;
import starter.cfsrest.bean.MsgBundle;
import starter.cfsrest.exception.CFSImgException;
import starter.cfsrest.factory.MsgBundleFactory;
import starter.cfsrest.service.CFSCommonService;
import starter.cfsrest.util.CFSXMLParseUtils;
import starter.cfsrest.util.ResultXmlHelper;
import starter.rest.Json;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author JackIce
 * @date  2016年5月27日
 * @description 
 */
@RestController
@RequestMapping(value = "/httpfiletrans")
public class CFSImgFileRest {
	@Autowired

	private CFSLogBean cfsLog;
	@Autowired
	private CFSCommonService cfsCommonService;
	@Autowired
	private FileSizeLimit fileSizeLimit;
	private Logger logger = Logger.getLogger(CFSImgFileRest.class);
	private static ObjectMapper objectMapper = new ObjectMapper();
	/*
	 * 根据xml文件中的信息，进行相应的操作
	 * 包括 归档/取消归档/修改归档/取消修改归档/拷贝归档/取消拷贝归档/阶段标示绑定
	 * funNumberByFunName为1时，当file_flag=1，执行归档操作，file_flag=0时，执行取消归档操作
	 * funNumberByFunName为2时，当 copy_flag=1，执行拷贝归档操作， copy_flag=0时，执行取消拷贝归档操作
	 * funNumberByFunName为3时，当 update_flag=1，执行拷贝归档操作， uodate_flag=0时，执行取消拷贝归档操作
	 * funNumberByFunName为4时，根据客户号、业务号、功能号进行查询。
	 * funNumberByFunName为5时，根据stage_flag设置归档标示，根据_id属性在service将阶段标示STAGE_FLAG设置为相应的阶段标示。
		如果_id不存在，则设置阶段标示为S0;

	 * funNumberByFunName为0时,报错：not exists the option type
	 */
	@RequestMapping(value = "/ImgFileServlet",method = {RequestMethod.POST})
	public void dispatchService(@RequestBody String xml,HttpServletResponse response){
		URLDecoder urlDecoder = new URLDecoder();
		String paramStr = null;
		try {
			paramStr = urlDecoder.decode(xml, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			logger.info("读取请求数据异常，请检查xml格式");
		}
		logger.info(paramStr);
		String responseXmlString = null;
		Map xmlAttrAndValue = null;
		try {
			if (StringUtils.isNotEmpty(paramStr)) {
				xmlAttrAndValue = CFSXMLParseUtils.getXmlAttrAndValue(paramStr,Constant.NODE_ATTR_NAME,Constant.XML_ATTRANDVALUE_MAP_NAME);
			}else{
				throw new uContentException("xmlparse error",HttpStatus.INTERNAL_SERVER_ERROR);
			}
			logger.info("xmlAttrAndValue:"+xmlAttrAndValue);
			if (xmlAttrAndValue==null) {
				throw new uContentException("xmlparse error",HttpStatus.INTERNAL_SERVER_ERROR);
			}
			String fun_name = (String)xmlAttrAndValue.get(Constant.CFS_XMLROOTNODE_NAME);
			Map xmlAttrMap = (Map)xmlAttrAndValue.get(Constant.XML_ATTRANDVALUE_MAP_NAME);
			String buss_id = (String) xmlAttrMap.get(Constant.CFS_BUSS_ID_KEY);
			String custom_code = (String) xmlAttrMap.get(Constant.CFS_CUSTOM_CODE_KEY);
			String fun_code = (String) xmlAttrMap.get(Constant.CFS_FUN_CODE_KEY);
			int funNumberByFunName = Constant.FunNames.getFunNumberByFunName(fun_name);
			HashMap<String, String> response_map = new HashMap<String, String>();
			//verify the legitimacy
			String[] checkData = {fun_code,buss_id,custom_code};
			MsgBundle<String> verifyParameter = verifyParameter(xmlAttrMap,checkData,Constant.CFS_SYS_CODE_KEY,Constant.CFS_FUN_CODE_KEY,Constant.CFS_OP_CODE_KEY);
			boolean verifyResult = verifyParameter.isSuccess();
			String option_ok="操作成功";
			if (verifyResult) {
				switch (funNumberByFunName) {
				case Constant.CFS_FUNNUMBER_ONE:
					String[] types = {Constant.CFS_SERVICEINDEX_TYPE};
					MsgBundle<String> msgBundle = null;
					String file_flag =(String) xmlAttrMap.get(Constant.CFS_FLAG_KEY);
					String op_code = (String) xmlAttrMap.get(Constant.CFS_OP_CODE_KEY);
					if (StringUtils.isNotEmpty(file_flag)&&StringUtils.isNotEmpty(op_code)) {
						if (Constant.CFS_COMMON_DIGIT_STRING_ONE.equals(file_flag)) {
							//archived file
							msgBundle = isFile(types, custom_code, buss_id,Constant.CFS_COMMON_DIGIT_STRING_ONE, fun_code,0, 10);
							option_ok = Constant.WarmPrompt.CFS_ISFILE_OK;
						} else {
							//cancel archived file
							msgBundle = isFile(types, custom_code, buss_id,Constant.CFS_COMMON_DIGIT_STRING_ZERO,fun_code,0, 10);
							option_ok = Constant.WarmPrompt.CFS_CANCEL_ISFILE_OK;
						}
						if(msgBundle!=null&&msgBundle.isSuccess()){
							response_map.put(Constant.CFS_IMG_RESULT_KEY, Constant.CFS_COMMON_DIGIT_STRING_ONE);
							response_map.put(Constant.CFS_RESULT_REMARK_KEY,option_ok);
						}else{
							response_map.put(Constant.CFS_IMG_RESULT_KEY,Constant.CFS_COMMON_DIGIT_STRING_ZERO);
							response_map.put(Constant.CFS_RESULT_REMARK_KEY, msgBundle.getT());
						}
					}else{
						response_map.put(Constant.CFS_IMG_RESULT_KEY,Constant.CFS_COMMON_DIGIT_STRING_ZERO);
						response_map.put(Constant.CFS_RESULT_REMARK_KEY,"flag不能为空");
					}
					responseXmlString = ResultXmlHelper.convertXml(xmlAttrAndValue, response_map,Constant.CFS_IMG_FILE);
					break;
				case Constant.CFS_FUNNUMBER_TWO:
					String[] types_master_copy = {Constant.CFS_MASTERINDEX_TYPE,Constant.CFS_COPYEINDEX_TYPE};
					MsgBundle<String> copyFileMsgBundle = null;
					String copy_flag =(String) xmlAttrMap.get(Constant.CFS_FLAG_KEY);
					if (StringUtils.isNotEmpty(copy_flag)) {
						if (Constant.CFS_COMMON_DIGIT_STRING_ONE.equals(copy_flag)) {
							//copy file
							option_ok = Constant.WarmPrompt.CFS_COPY_FILE_OK;
							copyFileMsgBundle = copyFile(types_master_copy, custom_code, buss_id,fun_code);
						} else {
							//cancel copy file
							option_ok = Constant.WarmPrompt.CFS_CANCEL_COPY_FILE_OK;
							copyFileMsgBundle = cancelCopyFile(types_master_copy, custom_code, buss_id,fun_code);
						}
						if (copyFileMsgBundle!=null&&copyFileMsgBundle.isSuccess()) {
							response_map.put(Constant.CFS_IMG_RESULT_KEY, Constant.CFS_COMMON_DIGIT_STRING_ONE);
							response_map.put(Constant.CFS_RESULT_REMARK_KEY,option_ok);
						}else{
							response_map.put(Constant.CFS_IMG_RESULT_KEY, Constant.CFS_COMMON_DIGIT_STRING_ZERO);
							response_map.put(Constant.CFS_RESULT_REMARK_KEY,copyFileMsgBundle.getT());
						}
					}else{
						response_map.put(Constant.CFS_IMG_RESULT_KEY, Constant.CFS_COMMON_DIGIT_STRING_ZERO);
						response_map.put(Constant.CFS_RESULT_REMARK_KEY,"flag不能为空");
					}
					responseXmlString = ResultXmlHelper.convertXml(xmlAttrAndValue, response_map,Constant.CFS_OPERATION_IMG_COPY);
					break;
					//update file
				case Constant.CFS_FUNNUMBER_THREE:
					String[] update_file_types={Constant.CFS_MASTERINDEX_TYPE,Constant.CFS_UPDATEINDEX_TYPE};
					MsgBundle<String> updateFileMsgBundle = null;
					String update_flag = (String) xmlAttrMap.get(Constant.CFS_FLAG_KEY);
					if (StringUtils.isNotEmpty(update_flag)) {

						if (Constant.CFS_COMMON_DIGIT_STRING_ONE.equals(update_flag)) {
							option_ok = Constant.WarmPrompt.CFS_UPDATE_FILE_OK;
							updateFileMsgBundle = updateFile(update_file_types, custom_code, buss_id,fun_code);
						}else{
							//cancel update file
							option_ok = Constant.WarmPrompt.CFS_CANCEL_UPDATE_FILE_OK;
							updateFileMsgBundle = cancelUpdateFile(update_file_types, custom_code, buss_id,fun_code);
						}
						if (updateFileMsgBundle.isSuccess()) {
							response_map.put(Constant.CFS_IMG_RESULT_KEY, Constant.CFS_COMMON_DIGIT_STRING_ONE);
							response_map.put(Constant.CFS_RESULT_REMARK_KEY,option_ok);
						}else{
							response_map.put(Constant.CFS_IMG_RESULT_KEY,Constant.CFS_COMMON_DIGIT_STRING_ZERO);
							response_map.put(Constant.CFS_RESULT_REMARK_KEY, updateFileMsgBundle.getT());
						}
					}else{
						response_map.put(Constant.CFS_IMG_RESULT_KEY,Constant.CFS_COMMON_DIGIT_STRING_ZERO);
						response_map.put(Constant.CFS_RESULT_REMARK_KEY,"flag 不能为空");
					}
					responseXmlString = ResultXmlHelper.convertXml(xmlAttrAndValue, response_map,Constant.CFS_OPERATION_IMG_COUNT);
					break;
				case Constant.CFS_FUNNUMBER_FOUR:
					String[] types_1 = {Constant.CFS_MASTERINDEX_TYPE};
					MsgBundle<String> imgCountMsgbundle = imgCount(types_1,custom_code,buss_id,fun_code);
					if (imgCountMsgbundle.isSuccess()) {
						response_map.put(Constant.CFS_IMG_RESULT_KEY, Constant.CFS_COMMON_DIGIT_STRING_ONE);
						response_map.put(Constant.CFS_RESULT_REMARK_KEY,Constant.WarmPrompt.CFS_IS_COUNT_OK);
					}else{
						response_map.put(Constant.CFS_IMG_RESULT_KEY,Constant.CFS_COMMON_DIGIT_STRING_ZERO);
						response_map.put(Constant.CFS_RESULT_REMARK_KEY, Constant.WarmPrompt.CFS_IS_COUNT_FAIL);
					}
					xmlAttrAndValue.put(Constant.CFS_RES_IMG_COUNT_IMGLIST, imgCountMsgbundle.getT());
					responseXmlString = ResultXmlHelper.convertXml(xmlAttrAndValue, response_map,Constant.CFS_OPERATION_IMG_COUNT);
					break;
				case Constant.CFS_FUNNUMBER_FIVE:
					String[] types_2 = {Constant.CFS_MASTERINDEX_TYPE,Constant.CFS_SERVICEINDEX_TYPE};
					String stage_flag = (String) xmlAttrMap.get(Constant.CFS_IMG_FILE_STAGE_FLAG);
					MsgBundle<String> updateStageMsgBundle = updateStage(types_2, custom_code, buss_id, stage_flag,fun_code);
					if (updateStageMsgBundle.isSuccess()) {
						response_map.put(Constant.CFS_IMG_RESULT_KEY, Constant.CFS_COMMON_DIGIT_STRING_ONE);
						response_map.put(Constant.CFS_RESULT_REMARK_KEY,updateStageMsgBundle.getT());
					}else{
						response_map.put(Constant.CFS_IMG_RESULT_KEY,Constant.CFS_COMMON_DIGIT_STRING_ZERO);
						response_map.put(Constant.CFS_RESULT_REMARK_KEY, updateStageMsgBundle.getT());
					}
					responseXmlString = ResultXmlHelper.convertXml(xmlAttrAndValue, response_map,Constant.CFS_OPERATION_UPDATE_STAGE);
					break;
				case Constant.CFS_FUNNUMBER_ZERO:
					throw new CFSImgException("not exists the option type", HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}else{
				response_map.put(Constant.CFS_IMG_RESULT_KEY,Constant.CFS_COMMON_DIGIT_STRING_ZERO);
				response_map.put(Constant.CFS_RESULT_REMARK_KEY, verifyParameter.getT());
				responseXmlString = ResultXmlHelper.convertXml(xmlAttrAndValue, response_map,fun_name);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw new CFSImgException(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}catch(Exception e){
			logger.error(e.getMessage());
			throw new CFSImgException(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/text");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.write(formatxml(responseXmlString));
		} catch (IOException e) {
			logger.info(String.format("做归档相关操作，输出流异常：%s",e.getMessage()));
		}
	}
	/*
	 * 公共api
	 */
	@RequestMapping(value = "/ActiveXHandle",method = {RequestMethod.POST},consumes = {"text/plain","application/*","multipart/*"},produces = MediaType.ALL_VALUE)
	public String commonApi(@RequestBody String xml,HttpServletRequest request){
		String xmlOfApi = "系统异常，操作失败";
		try {
			Map specialXmlAttrAndValue = CFSXMLParseUtils.getSpecialXmlAttrAndValue(xml,Constant.NODE_ATTR_NAME,Constant.XML_ATTRANDVALUE_MAP_NAME,Constant.XML_SPECIAL_ATTRANDVALUE_LIST_NAME);
			String fun_name = (String)specialXmlAttrAndValue.get(Constant.CFS_XMLROOTNODE_NAME);
			Map xmlAttrMap = (Map)specialXmlAttrAndValue.get(Constant.XML_ATTRANDVALUE_MAP_NAME);
			String buss_id = (String) xmlAttrMap.get(Constant.CFS_BUSS_ID_KEY);
			String custom_code = (String) xmlAttrMap.get(Constant.CFS_CUSTOM_CODE_KEY);
			String fun_code = (String) xmlAttrMap.get(Constant.CFS_FUNCODE);
			int funNumberByFunName = Constant.FunNames.getFunNumberByFunName(fun_name);
			HashMap<String, String> response_map = new HashMap<String, String>();
			switch (funNumberByFunName) {
			case Constant.CFS_FUNNUMBER_SIX:
				String img_id = (String) xmlAttrMap.get(Constant.CFS_IMG_ID);
				MsgBundle<String> imgDeleteBundle = imgDelete(Constant.CFS_MASTERINDEX_TYPE,custom_code,buss_id,img_id,fun_code);
				if (imgDeleteBundle.isSuccess()) {
					response_map.put(Constant.CFS_IMG_RESULT_KEY,Constant.CFS_COMMON_DIGIT_STRING_ONE);
					response_map.put(Constant.CFS_RESULT_REMARK_KEY, imgDeleteBundle.getT());
				}else{
					response_map.put(Constant.CFS_IMG_RESULT_KEY,Constant.CFS_COMMON_DIGIT_STRING_ZERO);
					response_map.put(Constant.CFS_RESULT_REMARK_KEY, imgDeleteBundle.getT());
				}
				xmlOfApi = ResultXmlHelper.convertXmlOfDeleteApi(xmlAttrMap, response_map, Constant.CFS_API_DELETE);
				break;
			case Constant.CFS_FUNNUMBER_SEVEN:
				//验证数据
				ArrayList<String> noVerify_list = new ArrayList<String>();
				noVerify_list.add("localClientIP");
				noVerify_list.add("metadata");
				MsgBundle<String> verifyParameterMsgBundle = verifyParameter(xmlAttrMap,noVerify_list);
				String batch_code = (String)xmlAttrMap.get(Constant.CFS_BATCH_CODE);
				if (verifyParameterMsgBundle.isSuccess()) {
					//get data
					Map<String,String> dataMap = new HashMap<String,String>();
					String acceptNo = (String)xmlAttrMap.get(Constant.CFS_ACCEPT_NO);
					String sys_code = (String) xmlAttrMap.get(Constant.CFS_SYSCODE);
					String org_no = (String)xmlAttrMap.get(Constant.CFS_ORGAN_NO);
					custom_code = (String)xmlAttrMap.get(Constant.CFS_CUS_ID);
					String batch_flag = StringUtils.isEmpty((String)xmlAttrMap.get(Constant.CFS_BATCH_FLG))?"0":(String)xmlAttrMap.get(Constant.CFS_BATCH_FLG);
					String scan_date = (String)xmlAttrMap.get(Constant.CFS_SCAN_TIME);
					String scan_man = (String)xmlAttrMap.get(Constant.CFS_OPERATOR_ID);
					String source_flag = (String)xmlAttrMap.get(Constant.CFS_SOURCE_FLAG);
					String type_code = Constant.CFS_COMMON_DIGIT_STRING_ZERO;
					String[] split = acceptNo.split(Constant.CFS_SPLIT_FLAG);
					buss_id = split[0];
					type_code = split[1];
					dataMap.put("SYS_CODE",sys_code);
					dataMap.put("FUN_CODE",fun_code);
					dataMap.put("BATCH_CODE",batch_code);
					dataMap.put("ORG_CODE",org_no);
					dataMap.put("CUSTOM_CODE",custom_code);
					dataMap.put("SOURCE_FLAG",source_flag);
					dataMap.put("SCAN_DATE",scan_date);
					dataMap.put("BATCH_FLAG",batch_flag);
					dataMap.put("FLW_CODE",buss_id);
					dataMap.put("TYPE_CODE",type_code);
					dataMap.put("SCAN_MAN",scan_man);
					dataMap.put("IS_DELETE", Constant.CFS_COMMON_DIGIT_STRING_ZERO);
					ArrayList<Map> map_list = new ArrayList<Map>();
					List<Map> xmlAttrList = (List<Map>)specialXmlAttrAndValue.get(Constant.XML_SPECIAL_ATTRANDVALUE_LIST_NAME);
					for (Map map : xmlAttrList) {
						String name = (String)map.get("name");
						int indexOf = name.lastIndexOf(".");
						String FILE_TYPE = name.substring(indexOf);
						dataMap.put("FILE_TYPE",FILE_TYPE);
						dataMap.put("IMAGE_ID",(String)map.get("img_id"));
						dataMap.put("IMAGE_NAME",(String)map.get("name"));
						dataMap.put("name",name);
						map_list.add(dataMap);
					}
					//上传数据
					MsgBundle<XContentBuilder> uploadMsgBundle = upload(Constant.CFS_MASTERINDEX_TYPE, Constant.CFS_MASTERINDEX_TYPE,map_list);
					HashMap<String, String> res_map = new HashMap<String,String>();
					if (uploadMsgBundle.isSuccess()) {
						int localPort = request.getLocalPort();
						Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
						String localInetAddress = Constant.CFS_EMPTY_STRING;
						InetAddress ip = null;
						while (networkInterfaces.hasMoreElements()) {
							NetworkInterface networkInterface = (NetworkInterface) networkInterfaces
									.nextElement();
							Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
							while (inetAddresses.hasMoreElements()) {
								ip = (InetAddress) inetAddresses
										.nextElement();
								if(ip!=null && ip instanceof Inet4Address){
									if (!"127.0.0.1".equals(ip.getHostAddress())) {
										localInetAddress = ip.getHostAddress();
									}
								}
							}
						}
						String url = "http://"+localInetAddress+":"+localPort+"/httpfiletrans/HttpFileTrans";
						res_map.put("syscode",sys_code);
						res_map.put("funcode",fun_code);
						res_map.put("uploadURL", url);
						xmlOfApi = ResultXmlHelper.convertXmlOfApi(xmlAttrList, batch_code, res_map,Constant.CFS_OPERATION_TRANS_RES);
					} else {
						res_map.put("syscode",sys_code);
						res_map.put("funcode",fun_code);
						res_map.put("batch_code",batch_code);
						res_map.put("organ_no",org_no);
						res_map.put("source_flag",source_flag);
						res_map.put("operator_id",scan_man);
						res_map.put("acceptNo",acceptNo);
						res_map.put("cusID",custom_code);
						StringBuilder builder = new StringBuilder();
						String error_message = uploadMsgBundle.getError_message();
						if (StringUtils.isNotEmpty(error_message)) {
							builder.append(error_message);
						}else{
							XContentBuilder xContentBuilder = uploadMsgBundle.getT();
							ArrayList result_list = objectMapper.readValue(xContentBuilder.string(), ArrayList.class);
							for (Object object : result_list) {
								if (object instanceof Map) {
									Map json = (Map) object;
									if (!(boolean) json.get("_created")) {
										String id = (String) json.get("_id");
										builder.append(String.format("%s:上传失败;",id));
									}
								}else{
									throw new CFSImgException("系统异常", HttpStatus.INTERNAL_SERVER_ERROR);
								}
							}

						}
						res_map.put("result",builder.toString());
						xmlOfApi = ResultXmlHelper.convertXmlOfApi(null, batch_code, res_map, Constant.CFS_OPERATION_TRANS_error);
					}
				}else{
					xmlAttrMap.put("result",verifyParameterMsgBundle.getT());
					xmlOfApi = ResultXmlHelper.convertXmlOfApi(null, batch_code, xmlAttrMap, Constant.CFS_OPERATION_TRANS_error);
				}
			default:
				break;
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			throw new CFSImgException("系统异常", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return xmlOfApi;
	}
	/*
	 * 上传图片
	 */
	@RequestMapping(value = "/HttpFileTrans",method = {RequestMethod.POST},consumes = {"text/plain","application/*","multipart/*"})
	public String uploadImage(HttpServletRequest request){
		logger.info("正在执行图片上传......");
		try {
			String file = request.getParameter("file");
			String filename = request.getParameter("filename");
			byte[] files = Base64.decodeBase64(file);
			logger.info("file:"+file+" filename:"+filename);
			XContentBuilder result = cfsCommonService.uploadStreamOfApi(Constant.CFS_MASTERINDEX_TYPE, filename, files);
			if (result==null&&!Boolean.valueOf((String) Json.parseToMap(result.string()).get("_created"))) {
				logger.info("图片上传失败");
				return String.format("{\"success\":\"false\",\"msg\":\"图片上传失败\"}");
			}
			logger.info(String.format("{\"success\":\"true\",\"msg\":\"图片上传成功\"}"));
			return String.format("{\"success\":\"true\",\"msg\":\"图片上传成功\"}");
		} catch (Exception e) {
			logger.error(e.getMessage());
			return String.format("{\"success\":\"false\",\"msg\":\"图片上传失败\"}");
		}
	}
	//上传图片 来自api
	private MsgBundle<XContentBuilder> upload(String index,String type,List<Map> jsons){
		MsgBundleFactory msgBundleFactory = MsgBundle<XContentBuilder>::new;
		MsgBundle<XContentBuilder> msgBundle = msgBundleFactory.create();
		try {
			XContentBuilder createOfApi = cfsCommonService.createOfApi(index, type, jsons);
			msgBundle.setSuccess(true);
			msgBundle.setT(createOfApi);
			logger.info("图片上传成功");
		} catch (IOException e) {
			msgBundle.setSuccess(false);
			msgBundle.setError_message("系统异常，上传失败");
			return msgBundle;
		}catch(uContentException e){
			msgBundle.setSuccess(false);
			msgBundle.setError_message("系统异常，上传失败");
			return msgBundle;
		}catch(Exception e){
			e.printStackTrace();
			msgBundle.setSuccess(false);
			msgBundle.setError_message("系统异常，上传失败");
			return msgBundle;
		}
		return msgBundle;
	}
	/*
	 * 根据类型types，客户号custom_code，业务号busniss_code，归档标示is_file，功能号fun_code进行文件的归档
	 */
	@SuppressWarnings("rawtypes")
	private  MsgBundle<String> isFile(String[] types,String custom_code,String busniss_code,String is_file,String fun_code,int start,int limit){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		try {
			Map queryResultMap = cfsCommonService.query(types, custom_code, busniss_code,fun_code, start, limit);
			List id_maps = (List)queryResultMap.get("documents");
			if (id_maps.size()!=0) {
				String id = null;
				Map id_map = (Map) id_maps.get(0);
				Map update_map = new HashMap();
				String old_isfile = (String)id_map.get("IS_FILE");
				update_map.put("IS_FILE",is_file);
				update_map.put("REMARK", "");
				id = (String) id_map.get("_id");
				logger.info("handlers to is file:"+update_map);
				//归档新增业务逻辑
				Map result_map = cfsCommonService.imgFileUpdate(types[0],id,old_isfile,fun_code,custom_code,busniss_code,Json.parse(update_map));
				if (!(boolean)result_map.get("_updated")) {
					msgBundle.setSuccess(false);
					msgBundle.setT("系统繁忙，归档失败");
				}else{
					msgBundle.setSuccess(true);
				}
			} else {
				msgBundle.setSuccess(false);
				msgBundle.setT("没有可归档的数据");
				logger.info("not exists data need to is_file");
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
			msgBundle.setSuccess(false);
			msgBundle.setT("系统繁忙，归档失败");
			return msgBundle;
		}
		return msgBundle;
	}
	/*
	 * cancel copy file取消拷贝归档
	 * type类型
	 * custom_code客户号
	 * business_code 业务号
	 * fun_code功能号
	 */
	private MsgBundle<String> cancelCopyFile(String[] types,String custom_code,String business_code,String fun_code){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		try {
			XContentBuilder xContentBuilder = cfsCommonService.cancelCopyFile(types[1], custom_code, business_code,fun_code);
			if(xContentBuilder==null){
				msgBundle.setSuccess(false);
				msgBundle.setT("没有对应的数据可取消拷贝");
				logger.info("没有对应的数据可取消拷贝");
				return msgBundle;
			}
			msgBundle.setSuccess(true);
		} catch (IOException e) {
			logger.info(e.getMessage());
			msgBundle.setSuccess(false);
			msgBundle.setT("系统异常，取消拷贝失败");
			return msgBundle;
		}
		return msgBundle;
	}

	/*
	 * copy file拷贝归档
	 * 根据类型types，客户号custom_code,业务号business_codem,功能号fun_code  进行拷贝归档
	 */
	private  MsgBundle<String> copyFile(String[] types,String custom_code,String busniss_code,String fun_code){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		try {
			XContentBuilder xContentBuilder = cfsCommonService.copyFile(types, custom_code, busniss_code,fun_code);
			if(xContentBuilder==null){
				msgBundle.setSuccess(false);
				msgBundle.setT(String.format("custom_code:%s business_code:%s 没有对应的数据可拷贝", custom_code,busniss_code));
				return msgBundle;
			}
			msgBundle.setSuccess(true);
		} catch (IOException e) {
			logger.info(e.getMessage());
			msgBundle.setSuccess(false);
			msgBundle.setT("系统异常，拷贝归档失败");
			return msgBundle;
		}catch (uContentException e){
			logger.info(e.getMessage());
			msgBundle.setSuccess(false);
			msgBundle.setT(String.format("拷贝归档失败:%s", e.getMessage()));
			return msgBundle;
		}catch(Exception e){
			logger.info(e.getMessage());
			msgBundle.setSuccess(false);
			msgBundle.setT("系统异常，拷贝归档失败");
			return msgBundle;
		}
		return msgBundle;
	}
	/*
	 * image type count
	 * type类型
	 * custom_code 客户号
	 * business_code业务号
	 * fun_code 功能号
	 */
	private MsgBundle<String> imgCount(String[] types,String custom_code,String business_code,String fun_code){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		try {
			String imgCount = cfsCommonService.imgCount(types,Constant.CFS_COUNT_FILED_NAME,custom_code,business_code,fun_code);	
			if (StringUtils.isEmpty(imgCount)) {
				msgBundle.setSuccess(false);
				msgBundle.setT("没有可分类数据");
				return msgBundle;
			}
			msgBundle.setSuccess(true);
			msgBundle.setT(imgCount);
		} catch (IOException e) {
			logger.info(e.getMessage());
			throw new CFSImgException(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}catch(Exception e){
			logger.info(e.getMessage());
			msgBundle.setSuccess(false);
			msgBundle.setT(e.getMessage());
			return msgBundle;
		}
		return msgBundle;
	}

	/*
	 * delete image 
	 * type类型
	 * custom_code 客户号
	 * business_code业务号
	 * fun_code 功能号
	 * img_id 图片id
	 */
	private MsgBundle<String> imgDelete(String type,String custom_code,String business_code,String img_id,String fun_code){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		if (StringUtils.isEmpty(img_id)) {
			msgBundle.setSuccess(false);
			msgBundle.setT("img_id为空");
			return msgBundle;
		}
		String[] ids = img_id.split(",");
		try {
			XContentBuilder xcontentBuilder = cfsCommonService.imgDelete(type, ids);
			List<Map> result_list = objectMapper.readValue(xcontentBuilder.string(), List.class);
			if (result_list.size()==0) {
				msgBundle.setSuccess(true);
				msgBundle.setT("删除成功");
				return msgBundle;
			}else{
				msgBundle.setSuccess(false);
				StringBuilder stringBuilder = new StringBuilder();
				for (Map map : result_list) {
					stringBuilder.append("id:").append(map.get("id")).append("删除失败；");
				}
				msgBundle.setT(stringBuilder.toString());
				return msgBundle;
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
			new CFSImgException(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}catch(Exception e){
			logger.info(e.getMessage());
			msgBundle.setSuccess(false);
			msgBundle.setT("删除失败");
			return msgBundle;
		}
		return msgBundle;
	}


	/*
	 * update stage，更新阶段标示
	 * 根据id，阶段标示stage_flag，客户号custom_code，业务号business_code，功能号fun_code，类型types进行阶段标示变更
	 */
	private MsgBundle<String> updateStage(String[] types,String custom_code,String business_code,String stage_flag,String fun_code){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		try {
			String[] serviceTypes = {types[1]};
			Map queryResultMap = cfsCommonService.query(serviceTypes, custom_code, business_code,fun_code, 0, 10);
			List id_maps = (List)queryResultMap.get("documents");
			if (id_maps.size()!=0) {
				String id = null;
				Map id_map = (Map) id_maps.get(0);
				id = (String) id_map.get("_id");
				Map result_map = null;
				if (StringUtils.isNotEmpty(id)) {
					result_map = cfsCommonService.imgFileUpdateStage(id,stage_flag.toUpperCase() , custom_code, business_code,fun_code, types);
				}else{
					if (Constant.CFS_STAGE_FLAG_S0.equals(stage_flag.toUpperCase())) {
						Map addStage = cfsCommonService.addStage(stage_flag, custom_code, business_code, fun_code, Constant.CFS_SERVICEINDEX_TYPE, Constant.CFS_SERVICEINDEX_TYPE);
						if((boolean) addStage.get("_created")){
							msgBundle.setT("业务流水号与阶段关系:绑定成功");
							msgBundle.setSuccess(true);
						}else{
							msgBundle.setSuccess(false);
							msgBundle.setT("系统异常，绑定失败");
							return msgBundle;
						}
					}
				}
				if (result_map!=null&&!(boolean)result_map.get("_updated")) {
					msgBundle.setSuccess(false);
					msgBundle.setT("系统异常，绑定失败");
					return msgBundle;
				}else{
					msgBundle.setT("'业务流水号与阶段关系:绑定成功");
					msgBundle.setSuccess(true);
				}
			} else {
				if (Constant.CFS_STAGE_FLAG_S0.equals(stage_flag.toUpperCase())) {
					Map addStage = cfsCommonService.addStage(stage_flag, custom_code, business_code, fun_code, Constant.CFS_SERVICEINDEX_TYPE, Constant.CFS_SERVICEINDEX_TYPE);
					if((boolean)addStage.get("_created")){
						msgBundle.setT("'业务流水号与阶段关系:绑定成功");
						msgBundle.setSuccess(true);
					}else{
						msgBundle.setSuccess(false);
						msgBundle.setT("系统异常，绑定失败");
						return msgBundle;
					}
				}
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
			new CFSImgException(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}catch(Exception e){
			logger.info(e.getMessage());
			msgBundle.setSuccess(false);
			msgBundle.setT("系统异常，修改失败");
			return msgBundle;
		}
		return msgBundle;
	}
	/*
	 * 修改归档
	 *  type类型
	 * custom_code客户号
	 * business_code 业务号
	 * fun_code功能号
	 */
	private MsgBundle<String> updateFile(String[] types,String custom_code,String business_code,String fun_code){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		try {
			XContentBuilder contentBuilder = cfsCommonService.updateFile(types, custom_code, business_code,fun_code);
			if(contentBuilder==null){
				msgBundle.setSuccess(false);
				msgBundle.setT("没有对应数据可修改归档");
				return msgBundle;
			}
			msgBundle.setSuccess(true);
		} catch (IOException e) {
			logger.info(e.getMessage());
			new CFSImgException(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}catch(Exception e){
			logger.info("process exists exception:"+e);
			msgBundle.setSuccess(false);
			msgBundle.setT(e.getMessage());
			return msgBundle;
		}
		return msgBundle;
	}

	/*
	 * 取消修改归档
	 */
	private MsgBundle<String> cancelUpdateFile(String[] types,String custom_code,String business_code,String fun_code){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		try {
			XContentBuilder contentBuilder = cfsCommonService.cancelCopyFile(types[1], custom_code, business_code,fun_code);
			if(contentBuilder==null){
				msgBundle.setSuccess(false);
				msgBundle.setT("没有对应数据可修改归档");
				return msgBundle;
			}
			msgBundle.setSuccess(true);
		} catch (IOException e) {
			logger.info(e.getMessage());
			new CFSImgException(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}catch(Exception e){
			logger.info(e.getMessage());
			msgBundle.setSuccess(false);
			msgBundle.setT("系统异常，修改失败");
			return msgBundle;
		}
		return msgBundle;
	}
	/*
	 * 检查客户号业务号的格式
	 */
	private MsgBundle<String> checkCustomAndBusinessCode(String ...args ){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		if (args.length==0) {
			msgBundle.setSuccess(false);
			msgBundle.setT(String.format("args length should 3:%d", args.length));
			return msgBundle;
		}
		if (args.length!=3) {
			msgBundle.setSuccess(false);
			msgBundle.setT(String.format("args length should 3:%d", args.length));
			return msgBundle;
		}
		int index = args[0].equals(Constant.CFS_QLCXD_BUSINESS)?1:2;
		if (StringUtils.isEmpty(args[index])) {
			msgBundle.setSuccess(false);
			msgBundle.setT(String.format("必填参数不能空"));
			return msgBundle;
		}
		msgBundle.setSuccess(true);
		return msgBundle;
	}
	/*
	 * 判定参数
	 */
	private MsgBundle<String> verifyParameter(Map<String,String> map,String[] checkData,String ... args){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		if (args.length==0) {
			msgBundle.setSuccess(true);
			msgBundle.setT(String.format("%s:args is null","needn't verify"));
		}
		if (map == null) {
			msgBundle.setSuccess(true);
			msgBundle.setT(String.format("%s:map is null","needn't verify"));
			return msgBundle;
		}
		Set<String> keySet = map.keySet();
		if (keySet.size()<args.length) {
			msgBundle.setSuccess(false);
			msgBundle.setT(String.format("args length overflow:%d",args.length));
		}
		for (int i = 0; i < args.length; i++) {
			if (StringUtils.isEmpty(map.get(args[i]))) {
				if(keySet.contains(args[i])){
					msgBundle.setSuccess(false);
					msgBundle.setT("必填参数不能为空");
					return msgBundle;
				}
			}
		}
		MsgBundle<String> checkCustomAndBusinessCode = checkCustomAndBusinessCode(checkData);
		if (!checkCustomAndBusinessCode.isSuccess()) {
			msgBundle.setSuccess(false);
			msgBundle.setT(checkCustomAndBusinessCode.getT());
			return msgBundle;
		}
		msgBundle.setSuccess(true);
		return msgBundle;
	} 
	private MsgBundle<String> verifyParameter(Map<String,String> parameter_map,List keys){
		MsgBundleFactory msgBundleFactory = MsgBundle<String>::new;
		MsgBundle<String> msgBundle = msgBundleFactory.create();
		Set<String> keySet = parameter_map.keySet();
		for (String key : keySet) {
			if (!keys.contains(key)&&StringUtils.isEmpty(parameter_map.get(key))) {
				msgBundle.setSuccess(false);
				msgBundle.setT(String.format("%s:不能为空", key));
				return msgBundle;
			}
		}
		msgBundle.setSuccess(true);
		return msgBundle;
	}
	/*
	 * 格式化xml
	 */
	private String formatxml(String xml){
		if (StringUtils.isNotEmpty(xml)) {
			StringBuilder stringBuilder = new StringBuilder();
			String[] split = xml.split(">");
			for (int i = 0; i < split.length; i++) {
				stringBuilder.append(split[i].trim());
				if (split.length-1!=i) {
					stringBuilder.append(">");
				}
			}
			return stringBuilder.toString();
		}else{
			return null;
		}
	}
}
