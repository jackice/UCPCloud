/**
 * 
 */
package starter.cfsrest.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import starter.uContentException;
import starter.cfsrest.exception.CFSImgException;
import starter.cfsrest.service.CFSCommonService;
import starter.cfsrest.service.CFSDocImportService;

/**
 * @author JackIce
 * @date  2016年5月30日
 * @description 
 */
@RestController
@RequestMapping(value = "/cfs", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommonRest {
	@Autowired
	private CFSCommonService commonService;

	@Autowired
	private CFSDocImportService cfsDocImportService;
	private Logger logger = Logger.getLogger(CommonRest.class);
	/*
	 * 上传图片压缩包
	 */
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = "multipart/*")
	public String uploadZip(MultipartHttpServletRequest request) {
		List<MultipartFile> zips = new ArrayList<>();
		Map<String, MultipartFile> fileMap = request.getFileMap();
		for (String name : fileMap.keySet()) {
			for(MultipartFile file : request.getFiles(name)){
				if (!file.isEmpty()) {
					zips.add(file);
				}
			}
		}
		try {
			XContentBuilder xContentBuilder = cfsDocImportService.uploadZip(zips);
			logger.info(xContentBuilder.string());
			return xContentBuilder.string();
		} catch (IOException e) {
			try {
				XContentBuilder errXContentBuilder = JsonXContent.contentBuilder();
				errXContentBuilder.startArray();
				errXContentBuilder.startObject().field("zipId","zip")
				.field("success", false)
				.field("msg", String.format("unzip %s failed","同步失败"))
				.endObject();
				errXContentBuilder.endArray();
				logger.info(errXContentBuilder.string());
				return errXContentBuilder.string();
			} catch (IOException e1) {
				logger.error("zip上传异常结果处理异常："+e1.getMessage());
			}
		}catch (Exception e) {
			try {
				XContentBuilder errXContentBuilder = JsonXContent.contentBuilder();
				errXContentBuilder.startArray();
				errXContentBuilder.startObject().field("zipId","zip")
				.field("success", false)
				.field("msg", String.format("unzip %s failed","同步失败"))
				.endObject();
				errXContentBuilder.endArray();
				logger.info(errXContentBuilder.string());
				return errXContentBuilder.string();
			} catch (IOException e1) {
				logger.error("zip上传异常结果处理异常："+e1.getMessage());
			}
		}
		logger.info("upload zip return null");
		return null;
	}
	/*
	 * 查询影响操作日志
	 * 根据页面通过ajax请求发送过来的消息，进行查询，然后返回json字符串
	 */
	@RequestMapping(value = "/logs/{type}", method = RequestMethod.GET)
	public void getLogs(@PathVariable String type,
			@RequestParam(defaultValue = "") String BIS_SYS_CODE,
			@RequestParam(defaultValue = "") String OPERATE_PERSON,
			@RequestParam(defaultValue = "") String CUSTOM_CODE,
			@RequestParam(defaultValue = "") String FLW_CODE,
			@RequestParam(defaultValue = "") String IMG_NAME,
			@RequestParam(defaultValue = "") String ORG_CODE,
			@RequestParam(defaultValue = "") String BATCH_CODE,
			@RequestParam(defaultValue = "") String OPERATE_DATE,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int limit,																																																																														                                                                                                                                      
			HttpServletRequest request,      
			HttpServletResponse response
			) {
		String[] types = {type};
		String[] conditions = {BIS_SYS_CODE,OPERATE_PERSON,CUSTOM_CODE,FLW_CODE,IMG_NAME,ORG_CODE,BATCH_CODE,OPERATE_DATE};
		String resultJsonString = null;
		PrintWriter writer=null;
		//XContentBuilder resultJsonString=null;
		try {
			resultJsonString = commonService.queryByCondition(types, page, limit, conditions).string();
			System.out.println(resultJsonString);
			StringBuilder builer=new StringBuilder(resultJsonString);
			builer.insert(1, "\"page\":"+page+",\"limit\":"+limit+",");
			System.out.println(builer.toString());
			resultJsonString=builer.toString();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			writer=response.getWriter();
			writer.write(resultJsonString);

		} catch (IOException e) {
			throw new CFSImgException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally{
			writer.close();
		}
	}
	/*
	 * 上传操作日志
	 */
	@RequestMapping(value = "/uploadLogs", method = RequestMethod.POST, consumes = "multipart/*")
	public String uploadLogsZip(MultipartHttpServletRequest request) {
		List<MultipartFile> zips = new ArrayList<>();
		Map<String, MultipartFile> fileMap = request.getFileMap();
		for (String name : fileMap.keySet()) {
			for(MultipartFile file : request.getFiles(name)){
				if (!file.isEmpty()) {
					zips.add(file);
				}
			}
		}
		try {
			XContentBuilder xContentBuilder = cfsDocImportService.uploadLogsZip(zips);
			return xContentBuilder.string();
		} catch (IOException e) {
			throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/*
	 *查询数据
	 *orgid 机构id
	 *query 查询语句
	 *fulltext 是否全文查询
	 *allowableActions 是否批量查询
	 *containsType 是否包含类型
	 */
	@RequestMapping(value = "/{type}", method = {RequestMethod.GET})
	public String query(@PathVariable String type,
			@RequestParam(defaultValue = "") String orgid,
			@RequestParam(defaultValue = "") String query,
			@RequestParam(defaultValue = "false") boolean fulltext,
			@RequestParam(defaultValue = "0") int start,
			@RequestParam(defaultValue = "10") int limit,
			@RequestParam(defaultValue = "[]") SortBuilder[] sort,
			@RequestParam(defaultValue = "false") boolean allowableActions,
			@RequestParam(defaultValue = "true") boolean containsType
			) {
		try {
			query = java.net.URLDecoder.decode(query, "UTF-8");
			logger.info("query string:"+query);
			String[] types = {type};
			XContentBuilder xDocument = commonService.query(types,orgid, query, start, limit, sort, allowableActions, fulltext, containsType);
			System.out.println(xDocument.string());
			return xDocument.string();
		} catch (IOException e) {
			throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
