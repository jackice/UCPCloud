package starter.cfsrest.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.exists.ExistsResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import starter.cfsrest.util.CommonUtils;
import starter.rest.Json;
import starter.service.Constant;
import starter.service.DocumentService;
import starter.service.hrbb.Cache;
import starter.service.hrbb.DocImportService;
import starter.service.hrbb.HrbbConfig;
import starter.service.hrbb.HrbbFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by jackice on 2016/5/31.
 */
@Service
public class CFSDocImportService {

	@Autowired
	private HrbbConfig hrbbConfig;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private CFSCommonService cfsCommonService;

	@Autowired
	private Client client;

	@Autowired
	private Cache cache;

	private Logger logger = Logger.getLogger(DocImportService.class);

	private static ObjectMapper objectMapper = new ObjectMapper();
	/*
	 * 将上传的压缩包里面的内容，存到服务器
	 */
	@SuppressWarnings("unchecked")
	public XContentBuilder uploadZip(List<MultipartFile> zips) throws IOException {
		boolean deleteIdentify=true;
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
		xContentBuilder.startArray();
		for (MultipartFile zip : zips) {
			try {
				String name = zip.getName();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String root = getRoot();
			try{
				unZip(zip, root);//解压ZIP
			} catch (IOException e){
				logger.error(e.getMessage());
				xContentBuilder.startObject();
				xContentBuilder.field("zipId", zip.getName())
				.field("success", false)
				.field("msg", String.format("unzip %s failed",zip.getName()))
				.endObject();
				continue;
			}
			File file = new File(FilenameUtils.concat(root, "metadata.txt"));
			String jsonString = null;
			try {
				jsonString = FileUtils.readFileToString(file, "UTf-8");
			} catch (IOException e) {
				logger.error("获取元数据失败："+e);
				xContentBuilder.startObject();
				xContentBuilder.field("zipId", zip.getName())
				.field("success", false)
				.field("msg", e.getMessage())
				.endObject();
				continue;
			}
			logger.info("upload json is:\r\n" + jsonString);
			try {
				List<Map<String, Object>> json = objectMapper.readValue(jsonString, List.class);
				if (json == null || json.isEmpty()) {
					logger.error("the metadata is null");
					xContentBuilder.startObject();
					xContentBuilder.field("zipId", zip.getName())
					.field("success", false)
					.field("msg", "the metadata is null")
					.endObject();
					continue;
				}
				// type必须指定
				String old_type  = (String) json.get(0).get("_type");
				if (starter.cfsrest.bean.Constant.CFS_COPYEINDEX_TYPE.equals(old_type)) {
					logger.info("handlers updateing cfs_copy......");
					List<Map<String, Object>> docs = new ArrayList<>();
					for (Map<String, Object> body : json) {
						body.put("SCAN_TIME", CommonUtils.getServerTime(null));
						//20160722 兼容信用卡 更改字段
						String scan_data = (String) body.get("SCAN_DATE");
						body.remove("SCAN_DATE");
						body.put("SCAN_TIME", scan_data);
						Json doc = null;
						String type =starter.cfsrest.bean.Constant.CFS_COPYEINDEX_TYPE;
						if (StringUtils.isBlank(type)) {
							if (doc == null) {
								doc = Json.parse(body);
							}
							xContentBuilder.startObject();
							xContentBuilder.field("zipId", zip.getName())
							.field("success", false)
							.field("msg", "_type is required")
							.field("doc", doc.toXContentBuilder().string())
							.endObject();
							docs.clear();
							break;
						}
						//检查文档类型是否存在
						Arrays.asList(cache.typeIds());
						// 引用的图片文件必须存在
						String streams = (String) body.get("_streams");
						if (StringUtils.isNotBlank(streams)) {
							File f = new File(FilenameUtils.concat(root, streams));
							if (!f.exists()) {
								if (doc == null) {
									doc = Json.parse(body);
								}
								xContentBuilder.startObject();
								xContentBuilder.field("zipId", zip.getName())
								.field("success", false)
								.field("msg", String.format("file: %s is not exist", streams))
								.field("doc", doc.toXContentBuilder().string())
								.endObject();
								docs.clear();
								break;
							}
						}
						String id = (String) body.get("_id");
						String opType = (String) body.get("_opType");
						if (!(StringUtils.isBlank(opType) || "create".equals(opType) || "update".equals(opType) || "delete".equals(opType))) {
							if (doc == null) {
								doc = Json.parse(body);
							}
							xContentBuilder.startObject();
							xContentBuilder.field("zipId", zip.getName())
							.field("success", false)
							.field("msg", String.format("the opType %s is invalid", opType))
							.field("doc", doc.toXContentBuilder().string())
							.endObject();
							docs.clear();
							break;
						}

						if ("update".equals(opType) || "delete".equals(opType)) {
							if (StringUtils.isBlank(id)) {
								if (doc == null) {
									doc = Json.parse(body);
								}
								xContentBuilder.startObject();
								xContentBuilder.field("zipId", zip.getName())
								.field("success", false)
								.field("msg", String.format("the _id is required when opType is %s ", opType))
								.field("doc", doc.toXContentBuilder().string())
								.endObject();
								docs.clear();
								break;
							}
							IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery(type).ids(id);
							ExistsResponse existsResponse = client.prepareExists(type).setQuery(idsQueryBuilder).get();
							if (!existsResponse.exists()) {
								if (doc == null) {
									doc = Json.parse(body);
								}
								xContentBuilder.startObject();
								xContentBuilder.field("zipId", zip.getName())
								.field("success", false)
								.field("msg", String.format("the doc %s is not exist", id))
								.field("doc", doc.toXContentBuilder().string())
								.endObject();
								docs.clear();
								break;
							}
						}

						try {// 校验
							documentService.formatAcl(body);
							documentService.validate(body, type);
						} catch (Throwable e) {
							xContentBuilder.startObject();
							xContentBuilder.field("zipId", zip.getName())
							.field("success", false)
							.field("msg", e.getMessage())
							.endObject();
							docs.clear();
							logger.error("验证元数据失败："+e);
							break;
						}
						body.put("_type", type);
						body.put("_opType", opType);
						body.put("_id", id);
						docs.add(body);
					}


					if (!docs.isEmpty()) {
						//do bulk insert
						BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
						for (Map<String, Object> doc : docs) {
							Object _streams = doc.get("_streams");
							String type = (String) doc.get("_type");
							if (_streams != null && StringUtils.isNotBlank(_streams.toString())) {
								List<Map<String, Object>> streams = new ArrayList<>();
								String s = _streams.toString();
								Map<String, Object> stream = documentService.uploadStream(type,new HrbbFile(new File(FilenameUtils.concat(root, s))));
								streams.add(stream);
								doc.put(Constant.FieldName.STREAMS, streams);
							}

							doc.remove("_type");
							String id = (String) doc.get("_id");
							doc.remove("_id");
							String opType = (String) doc.get("_opType");
							doc.remove("_opType");
							if (StringUtils.isBlank(opType) || "create".equals(opType)) {
								cfsCommonService.beforeCreate(doc);
								IndexRequest indexRequest = new IndexRequest(type, type).source(doc);
								if (StringUtils.isNotBlank(id)) {
									indexRequest.id(id.toString());
								}
								logger.info("create:\r\n" + Json.parse(doc).toXContentBuilder().string());
								bulkRequestBuilder.add(indexRequest);
							}else {
								if ("delete".equals(opType)) {
									doc.remove("_acl");
									documentService.addAdminGrouptoDoc(doc);
								}
								cfsCommonService.beforeUpdate(doc);
								logger.info(opType + ":\r\n" + Json.parse(doc).toXContentBuilder().string());
								UpdateRequest updateRequest = new UpdateRequest(type, type, id).doc(doc);
								bulkRequestBuilder.add(updateRequest);
							}
						}

						BulkResponse response = bulkRequestBuilder.get();
						xContentBuilder.startObject();
						xContentBuilder.field("zipId", zip.getName())
						.field("success", !response.hasFailures())
						.field("msg", response.hasFailures() == true ? response.buildFailureMessage() : "")
						.endObject();
					}
				}else if(starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE.equals(old_type)){
					logger.info("handlers updateing cfs_update");
					List<Map<String, Object>> docs = new ArrayList<>();
					for (Map<String, Object> body : json) {
						body.put("SCAN_TIME", CommonUtils.getServerTime(null));
						//20160722 兼容信用卡 更改字段
						String scan_data = (String) body.get("SCAN_DATE");
						body.remove("SCAN_DATE");
						body.put("SCAN_TIME", scan_data);
						Json doc = null;
						String type =starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE;
						if (StringUtils.isBlank(type)) {
							if (doc == null) {
								doc = Json.parse(body);
							}
							xContentBuilder.startObject();
							xContentBuilder.field("zipId", zip.getName())
							.field("success", false)
							.field("msg", "_type is required")
							.field("doc", doc.toXContentBuilder().string())
							.endObject();
							docs.clear();
							break;
						}
						//检查文档类型是否存在
						Arrays.asList(cache.typeIds());
						// 引用的图片文件必须存在
						String streams = (String) body.get("_streams");
						if (StringUtils.isNotBlank(streams)) {
							File f = new File(FilenameUtils.concat(root, streams));
							if (!f.exists()) {
								if (doc == null) {
									doc = Json.parse(body);
								}
								xContentBuilder.startObject();
								xContentBuilder.field("zipId", zip.getName())
								.field("success", false)
								.field("msg", String.format("file: %s is not exist", streams))
								.field("doc", doc.toXContentBuilder().string())
								.endObject();
								docs.clear();
								break;
							}
						}
						String id = (String) body.get("_id");
						String opType = (String) body.get("_opType");
						if (!(StringUtils.isBlank(opType) || "create".equals(opType) || "update".equals(opType) || "delete".equals(opType))) {
							if (doc == null) {
								doc = Json.parse(body);
							}
							xContentBuilder.startObject();
							xContentBuilder.field("zipId", zip.getName())
							.field("success", false)
							.field("msg", String.format("the opType %s is invalid", opType))
							.field("doc", doc.toXContentBuilder().string())
							.endObject();
							docs.clear();
							break;
						}

						if ("update".equals(opType) || "delete".equals(opType)) {
							if (StringUtils.isBlank(id)) {
								if (doc == null) {
									doc = Json.parse(body);
								}
								xContentBuilder.startObject();
								xContentBuilder.field("zipId", zip.getName())
								.field("success", false)
								.field("msg", String.format("the _id is required when opType is %s ", opType))
								.field("doc", doc.toXContentBuilder().string())
								.endObject();
								docs.clear();
								break;
							}
							IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery(type).ids(id);
							ExistsResponse existsResponse = client.prepareExists(type).setQuery(idsQueryBuilder).get();
							if (!existsResponse.exists()) {
								if (doc == null) {
									doc = Json.parse(body);
								}
								xContentBuilder.startObject();
								xContentBuilder.field("zipId", zip.getName())
								.field("success", false)
								.field("msg", String.format("the doc %s is not exist", id))
								.field("doc", doc.toXContentBuilder().string())
								.endObject();
								docs.clear();
								break;
							}
						}

						try {// 校验
							documentService.formatAcl(body);
							documentService.validate(body, type);
						} catch (Throwable e) {
							xContentBuilder.startObject();
							xContentBuilder.field("zipId", zip.getName())
							.field("success", false)
							.field("msg", e.getMessage())
							.endObject();
							docs.clear();
							logger.error("验证元数据失败："+e);
							break;
						}
						body.put("_type", type);
						body.put("_opType", opType);
						body.put("_id", id);
						docs.add(body);
					}


					if (!docs.isEmpty()) {
						//do bulk insert
						BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
						for (Map<String, Object> doc : docs) {
							Object _streams = doc.get("_streams");
							String type = (String) doc.get("_type");
							if (_streams != null && StringUtils.isNotBlank(_streams.toString())) {
								List<Map<String, Object>> streams = new ArrayList<>();
								String s = _streams.toString();
								Map<String, Object> stream = documentService.uploadStream(type,new HrbbFile(new File(FilenameUtils.concat(root, s))));
								streams.add(stream);
								doc.put(Constant.FieldName.STREAMS, streams);
							}

							doc.remove("_type");
							String id = (String) doc.get("_id");
							doc.remove("_id");
							String opType = (String) doc.get("_opType");
							doc.remove("_opType");
							if (StringUtils.isBlank(opType) || "create".equals(opType)) {
								cfsCommonService.beforeCreate(doc);
								IndexRequest indexRequest = new IndexRequest(type, type).source(doc);
								if (StringUtils.isNotBlank(id)) {
									indexRequest.id(id.toString());
								}
								logger.info("create:\r\n" + Json.parse(doc).toXContentBuilder().string());
								bulkRequestBuilder.add(indexRequest);
							}else {
								if ("delete".equals(opType)) {
									doc.remove("_acl");
									documentService.addAdminGrouptoDoc(doc);
								}
								cfsCommonService.beforeUpdate(doc);
								logger.info(opType + ":\r\n" + Json.parse(doc).toXContentBuilder().string());
								UpdateRequest updateRequest = new UpdateRequest(type, type, id).doc(doc);
								bulkRequestBuilder.add(updateRequest);
							}
						}

						BulkResponse response = bulkRequestBuilder.get();
						xContentBuilder.startObject();
						xContentBuilder.field("zipId", zip.getName())
						.field("success", !response.hasFailures())
						.field("msg", response.hasFailures() == true ? response.buildFailureMessage() : "")
						.endObject();
					}

				}else{
					logger.info("handlers updateing cfs_master");
					//set type
					Map<String, Object> map = json.get(0);
					logger.info("json:"+map);
					String type = (String) map.get("_type");
					logger.info("_type:"+type);
					String custom_code = (String)map.get("CUSTOM_CODE");
					String flw_code = (String)map.get("FLW_CODE");
					String fun_code = (String)map.get("FUN_CODE");
					SearchResponse searchResponse = null;
					String stage_flag = null;
					if (starter.cfsrest.bean.Constant.CFS_OPERATION_IMG_CREATE.equals(map.get("_opType"))) {
						SearchRequestBuilder searchRequestBuilder = null;
						searchRequestBuilder = client.prepareSearch(starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE).setTypes(starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE);
						//set query
						if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
							BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
							booleanBuilder.should(QueryBuilders.matchQuery("CUSTOM_CODE", custom_code));
							searchRequestBuilder.setQuery(booleanBuilder);
						}else{
							BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
							booleanBuilder.should(QueryBuilders.matchQuery("FLW_CODE", flw_code));
							searchRequestBuilder.setQuery(booleanBuilder);
						}
						searchResponse = searchRequestBuilder.setVersion(true).execute().actionGet();
						long hits = searchResponse.getHits().getTotalHits();
						if(hits!=0){
							SearchHit hit = searchResponse.getHits().getAt(0);
							String is_file = (String)hit.getSource().get("IS_FILE");
							stage_flag = (String) hit.getSource().get("STAGE_FLAG");
							type = "0".equals(is_file)?starter.cfsrest.bean.Constant.CFS_MASTERINDEX_TYPE:starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE;
							logger.info("after reset type:"+type);
						}
						//保存业务数据
						if(json.size()!=0){
							if (hits==0) {
								Map cfs_service_dataMap = new HashMap();
								cfs_service_dataMap.put("CUSTOM_CODE", custom_code);
								cfs_service_dataMap.put("name", StringUtils.isNotEmpty(custom_code)?custom_code:flw_code);
								cfs_service_dataMap.put("FLW_CODE", flw_code);
								cfs_service_dataMap.put("IS_FILE", "0");
								cfs_service_dataMap.put("STAGE_FLAG", "S0");
								cfs_service_dataMap.put("REMARK", "");
								cfsCommonService.formatAcl(cfs_service_dataMap);
								cfsCommonService.validate(cfs_service_dataMap, starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE);
								cfsCommonService.beforeCreate(cfs_service_dataMap);
								IndexResponse indexResponse = client.prepareIndex(starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE, starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE).setSource(cfs_service_dataMap).setId(flw_code).execute().actionGet();
								String.format("_index:%s _type:%s _id:%s _version:%s _created:%s", indexResponse.getIndex(),
										indexResponse.getType(),indexResponse.getId(),indexResponse.getVersion(),indexResponse.isCreated());
								logger.info("create:\r\n" + Json.parse(cfs_service_dataMap).toXContentBuilder().string()+"\n result:"+String.format("_index:%s _type:%s _id:%s _version:%s _created:%s", indexResponse.getIndex(),
										indexResponse.getType(),indexResponse.getId(),indexResponse.getVersion(),indexResponse.isCreated()));
							}
						}
					}
					List<Map<String, Object>> docs = new ArrayList<>();
					for (Map<String, Object> body : json) {
						body.put("SCAN_TIME", CommonUtils.getServerTime(null));
						//20160722 兼容信用卡 更改字段
						String scan_data = (String) body.get("SCAN_DATE");
						body.remove("SCAN_DATE");
						body.put("SCAN_TIME", scan_data);
						Json doc = null;
						//检查文档类型是否存在
						Arrays.asList(cache.typeIds());
						// 引用的图片文件必须存在
						String streams = (String) body.get("_streams");
						String id = (String) body.get("_id");
						String ck_flag = (String)body.get("CK_FLAG");
						String opType = (String) body.get("_opType");
						if (!(StringUtils.isBlank(opType) || "create".equals(opType) || "update".equals(opType) || "delete".equals(opType))) {
							if (doc == null) {
								doc = Json.parse(body);
							}
							xContentBuilder.startObject();
							xContentBuilder.field("zipId", zip.getName())
							.field("success", false)
							.field("msg", String.format("the opType %s is invalid", opType))
							.field("doc", doc.toXContentBuilder().string())
							.endObject();
							docs.clear();
							break;
						}

						if ("update".equals(opType) || "delete".equals(opType)) {
							if (StringUtils.isBlank(id)) {
								if (doc == null) {
									doc = Json.parse(body);
								}
								xContentBuilder.startObject();
								xContentBuilder.field("zipId", zip.getName())
								.field("success", false)
								.field("msg", String.format("the _id is required when opType is %s ", opType))
								.field("doc", doc.toXContentBuilder().string())
								.endObject();
								docs.clear();
								break;
							}

							IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery(type).ids(id);
							ExistsResponse existsResponse = client.prepareExists(type).setQuery(idsQueryBuilder).get();
							if (!existsResponse.exists()) {
								if (doc == null) {
									doc = Json.parse(body);
								}
								xContentBuilder.startObject();
								xContentBuilder.field("zipId", zip.getName())
								.field("success", false)
								.field("msg", String.format("the doc %s is not exist", id))
								.field("doc", doc.toXContentBuilder().string())
								.endObject();
								docs.clear();
								break;
							}
						}

						try {// 校验
							documentService.formatAcl(body);
							documentService.validate(body, type);
						} catch (Throwable e) {
							xContentBuilder.startObject();
							xContentBuilder.field("zipId", zip.getName())
							.field("success", false)
							.field("msg", "元数据验证失败")
							.endObject();
							docs.clear();
							logger.error(e.getMessage());
							break;
						}
						body.put("_type", type);
						body.put("_opType", opType);
						body.put("_id", id);
						body.put("CK_FLAG", ck_flag);
						docs.add(body);
					}

					if (!docs.isEmpty()) {
						//do bulk insert
						BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
						XContentBuilder xDeleteContentBuilder = JsonXContent.contentBuilder();
						xDeleteContentBuilder.startArray();
						for (Map<String, Object> doc : docs) {
							Object _streams = doc.get("_streams");
							String id = (String) doc.get("_id");
							if (_streams != null && StringUtils.isNotBlank(_streams.toString())) {
								List<Map<String, Object>> streams = new ArrayList<>();
								String s = _streams.toString();
								File streamFile = new File(FilenameUtils.concat(root, s));
								if (streamFile.exists()) {
									String regex = "|.doc|.docx|.xls|.xlsx|.pdf|";
									String fileType = (String)doc.get("FILE_TYPE");
									if (fileType.matches(regex)&&!((String)doc.get("_opType")).equals(starter.cfsrest.bean.Constant.CFS_OPERATION_IMG_CREATE)) {
										GetResponse getResponse = client.prepareGet(type, type, id).execute().actionGet();
										Map<String, Object> source = getResponse.getSource();
										try {
											streams = (List<Map<String, Object>>) source.get("_streams");
										} catch (Exception e) {
											logger.info("_streams already breaked");
										}
										doc.put(Constant.FieldName.STREAMS, streams);
										logger.info(String.format("when synchronization data,doc file type is %s,so don't to update streams",fileType));
									}else{
										try {
											Map<String, Object> stream = cfsCommonService.uploadStream(type,new HrbbFile(new File(FilenameUtils.concat(root, s))));
											streams.add(stream);
										} catch (Exception e) {
											continue;
										}
										doc.put(Constant.FieldName.STREAMS, streams);
									}
								}else{
									logger.info(String.format("the doc %s not exist in zip", s)); 
									GetResponse getResponse = client.prepareGet(type, type, id).execute().actionGet();
									Map<String, Object> source = getResponse.getSource();
									try {
										streams = (List<Map<String, Object>>) source.get("_streams");
									} catch (Exception e) {
										logger.info("_streams already breaked");
										continue;
									}
									if (_streams != null && StringUtils.isNotBlank(_streams.toString())) {
										doc.put(Constant.FieldName.STREAMS, streams);
									}
									logger.info(String.format("when synchronization data,doc %s not exist in zip,so don't to update streams:%s",s,streams));
								}
							}
							doc.remove("_type");
							String type_code = (String) doc.get("TYPE_CODE");
							doc.remove("_id");
							String opType = (String) doc.get("_opType");
							doc.remove("_opType");
							String checkFlag = (String)doc.get(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG);
							if (StringUtils.isBlank(opType) || "create".equals(opType)) {
								documentService.beforeCreate(doc);
								documentService.validate(doc, type);
								//determine whether you need to upload to cfs_update
								if (starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE.equals(type)) {
									doc.put("CHECK",starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ZERO);
									if (stage_flag!=null&&stage_flag.matches("^[Ss][1-4]$")) {
										//determine whether you need to upload to cfs_update
										BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
										boolQuery.must(QueryBuilders.matchQuery("cfs_type_code", type_code));
										boolQuery.must(QueryBuilders.matchQuery("cfs_stage", stage_flag));
										SearchRequestBuilder searchRequestBuilderForStage = client.prepareSearch(starter.cfsrest.bean.Constant.CFS_DATA_INDEX).setTypes(starter.cfsrest.bean.Constant.CFS_DATA_STAGE_TYPE).setQuery(boolQuery);
										SearchResponse actionGet = searchRequestBuilderForStage.execute().actionGet();
										long totalHits = actionGet.getHits().getTotalHits();
										if (totalHits!=0) {
											doc.remove(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG);
											cfsCommonService.beforeCreate(doc);
											IndexRequest indexRequest = new IndexRequest(starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE, starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE).source(doc);
											if (StringUtils.isNotBlank(id)) {
												indexRequest.id(id.toString());
											}
											logger.info("create:\r\n" + Json.parse(doc).toXContentBuilder().string());
											bulkRequestBuilder.add(indexRequest);
										}else{
											logger.info("typecode and stageflag mismatching");
											String image_name = (String) doc.get("IMAGE_NAME");
											xDeleteContentBuilder.startObject();
											xDeleteContentBuilder.field("id",StringUtils.isNotBlank(image_name)?image_name:id)
											.field("success",false)
											.field("msg","分类码不属于该流水号阶段下，不能上传")
											.endObject();
										}
									} else {
										//S0 upload
										doc.remove(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG);
										cfsCommonService.beforeCreate(doc);
										IndexRequest indexRequest = new IndexRequest(starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE, starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE).source(doc);
										if (StringUtils.isNotBlank(id)) {
											indexRequest.id(id.toString());
										}
										logger.info("create:\r\n" + Json.parse(doc).toXContentBuilder().string());
										bulkRequestBuilder.add(indexRequest);
									}
								}else{
									doc.remove(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG);
									cfsCommonService.beforeCreate(doc);
									IndexRequest indexRequest = new IndexRequest(type,type).source(doc);
									if (StringUtils.isNotBlank(id)) {
										indexRequest.id(id.toString());
									}
									logger.info("create:\r\n" + Json.parse(doc).toXContentBuilder().string());
									bulkRequestBuilder.add(indexRequest);
								}
							}else if("delete".equals(opType)){
								//if is filed,can't delete
								logger.info("handlers deleteing :"+id);
								doc.remove(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG);
								SearchRequestBuilder searchRequestBuilder = client.prepareSearch("cfs_service").setTypes("cfs_service");
								//set query
								if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
									BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
									booleanBuilder.should(QueryBuilders.matchQuery("CUSTOM_CODE", custom_code));
									searchRequestBuilder.setQuery(booleanBuilder);
								}else{
									BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
									booleanBuilder.should(QueryBuilders.matchQuery("FLW_CODE", flw_code));
									searchRequestBuilder.setQuery(booleanBuilder);
								}
								searchResponse = searchRequestBuilder.setVersion(true).execute().actionGet();
								if(searchResponse!=null&&searchResponse.getHits().getHits().length!=0&&(searchResponse.getHits().getHits()[0].getSource().get("IS_FILE")).equals("1")){
									//20160714 配合控件修改bug
									//									doc.put("IS_DELETE", starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ZERO);
									if (starter.cfsrest.bean.Constant.CFS_CHECK_FLAG_VALUE_ONE.equals(checkFlag)) {
										//										doc.put("CHECK",starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ONE);
										String image_id = (String) doc.get("IMAGE_ID");
										String image_name=(String) doc.get("IMAGE_NAME");
										Map metadataOfStreams = null;
										if (StringUtils.isNotEmpty(image_id)) {
											metadataOfStreams = getMetadataOfImageId(image_id, starter.cfsrest.bean.Constant.CFS_MASTERINDEX_TYPE);
										} else {
											logger.error("IMAGE_ID is empty when sync delete");
											deleteIdentify = false;
											xDeleteContentBuilder.startObject();
											xDeleteContentBuilder.field("id",StringUtils.isNotBlank(image_name)?image_name:id)
											.field("success",false)
											.field("msg","metadata is not full")
											.endObject();
											continue;
										}
										if (metadataOfStreams!=null) {
											metadataOfStreams.put("CHECK",starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ONE);
											metadataOfStreams.remove(Constant.FieldName.LASTUPDATEDBY);
											metadataOfStreams.remove(Constant.FieldName.LASTUPDATEDON);
											cfsCommonService.beforeCreate(metadataOfStreams);
											IndexRequest indexRequest = new IndexRequest(starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE, starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE).source(metadataOfStreams);
											if (StringUtils.isNotBlank(id)) {
												indexRequest.id(id.toString());
											}
											logger.info("create:\r\n" + Json.parse(doc).toXContentBuilder().string());
											bulkRequestBuilder.add(indexRequest);
											deleteIdentify = false;
											xDeleteContentBuilder.startObject();
											xDeleteContentBuilder.field("id",StringUtils.isNotBlank(image_name)?image_name:id)
											.field("success",false)
											.field("msg","文件已经归档，不能删除，将转到审批流程")
											.endObject();
										} else {
											logger.error("IMAGE_ID is empty when sync delete");
											deleteIdentify = false;
											xDeleteContentBuilder.startObject();
											xDeleteContentBuilder.field("id",StringUtils.isNotBlank(image_name)?image_name:id)
											.field("success",false)
											.field("msg","metadata is not full")
											.endObject();
											continue;
										}
									}else{
										if (starter.cfsrest.bean.Constant.CFS_CHECK_FLAG_VALUE_TWO.equals(checkFlag)) {
											type = starter.cfsrest.bean.Constant.CFS_COPYEINDEX_TYPE;
										}else{
											type = starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE;
											SearchRequestBuilder sqb = client.prepareSearch(type).setTypes(type);
											if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
												BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
												booleanBuilder.should(QueryBuilders.matchQuery("CUSTOM_CODE", custom_code));
												sqb.setQuery(booleanBuilder);
											}else{
												BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
												booleanBuilder.should(QueryBuilders.matchQuery("FLW_CODE", flw_code));
												sqb.setQuery(booleanBuilder);
											}
											SearchResponse actionGet = sqb.execute().actionGet();
										}
										doc.remove(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG);
										cfsCommonService.beforeUpdate(doc);
										UpdateRequest updateRequest = new UpdateRequest(type, type, id).doc(doc);
										bulkRequestBuilder.add(updateRequest);
									}
								}
								//if isn't filed,need verdit operator
								else{
									if (starter.cfsrest.bean.Constant.CFS_CHECK_FLAG_VALUE_ONE.equals(checkFlag)) {
										type = starter.cfsrest.bean.Constant.CFS_MASTERINDEX_TYPE;
										GetResponse getResponse = client.prepareGet(type, type, id).execute().actionGet();
										if(!getResponse.getSourceAsMap().get("SCAN_MAN").equals(doc.get("SCAN_MAN"))){
											deleteIdentify = false;
											String image_id = (String) doc.get("IMAGE_ID");
											String image_name = (String) doc.get("IMAGE_NAME");
											Map metadataOfStreams = null;
											if (StringUtils.isNotEmpty(image_id)) {
												metadataOfStreams = getMetadataOfImageId(image_id, starter.cfsrest.bean.Constant.CFS_MASTERINDEX_TYPE);
											} else {
												logger.error("IMAGE_ID is empty when sync delete");
												deleteIdentify = false;
												xDeleteContentBuilder.startObject();
												xDeleteContentBuilder.field("id",StringUtils.isNotBlank(image_name)?image_name:id)
												.field("success",false)
												.field("msg","metadata is not full")
												.endObject();
												break;
											}
											if (metadataOfStreams!=null) {
												metadataOfStreams.put("CHECK",starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ONE);
												metadataOfStreams.remove(Constant.FieldName.LASTUPDATEDBY);
												metadataOfStreams.remove(Constant.FieldName.LASTUPDATEDON);
												cfsCommonService.beforeCreate(metadataOfStreams);
												IndexRequest indexRequest = new IndexRequest(starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE, starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE).source(metadataOfStreams);
												if (StringUtils.isNotBlank(id)) {
													indexRequest.id(id.toString());
												}
												logger.info("create_man:"+searchResponse.getHits().getHits()[0].getSource().get("SCAN_MAN")+"----option_man:"+doc.get("SCAN_MAN"));
												logger.info("create:\r\n" + Json.parse(doc).toXContentBuilder().string());
												bulkRequestBuilder.add(indexRequest);
												deleteIdentify = false;
												xDeleteContentBuilder.startObject();
												xDeleteContentBuilder.field("id",StringUtils.isNotBlank(image_name)?image_name:id)
												.field("success",false)
												.field("msg","上传者和操作不是同一个人，无法删除")
												.endObject();
											} else {
												logger.error("IMAGE_ID is empty when sync delete");
												deleteIdentify = false;
												xDeleteContentBuilder.startObject();
												xDeleteContentBuilder.field("id",StringUtils.isNotBlank(image_name)?image_name:id)
												.field("success",false)
												.field("msg","metadata is not full")
												.endObject();
												continue;
											}
											//											doc.remove(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG);
											//											doc.put("CHECK",starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ONE);
											//											cfsCommonService.beforeCreate(doc);
											//											IndexRequest indexRequest = new IndexRequest(starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE, starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE).source(doc);
											//											if (StringUtils.isNotBlank(id)) {
											//												indexRequest.id(id.toString());
											//											}
											//											logger.info("create_man:"+searchResponse.getHits().getHits()[0].getSource().get("SCAN_MAN")+"----option_man:"+doc.get("SCAN_MAN"));
											//											logger.info("create:\r\n" + Json.parse(doc).toXContentBuilder().string());
											//											bulkRequestBuilder.add(indexRequest);
											//											xDeleteContentBuilder.startObject();
											//											xDeleteContentBuilder.field("id",id)
											//											.field("success",false)
											//											.field("msg","上传者和操作不是同一个人，无法删除")
											//											.endObject();
										}else{
											doc.remove(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG);
											cfsCommonService.beforeUpdate(doc);
											logger.info(opType + ":\r\n" + Json.parse(doc).toXContentBuilder().string());
											UpdateRequest updateRequest = new UpdateRequest(type, type, id).doc(doc);
											bulkRequestBuilder.add(updateRequest);
										}
									}else{
										if (starter.cfsrest.bean.Constant.CFS_CHECK_FLAG_VALUE_TWO.equals(checkFlag)) {
											type = starter.cfsrest.bean.Constant.CFS_COPYEINDEX_TYPE;
										}else{
											type = starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE;
										}
										doc.remove(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG);
										cfsCommonService.beforeUpdate(doc);
										logger.info(opType + ":\r\n" + Json.parse(doc).toXContentBuilder().string());
										UpdateRequest updateRequest = new UpdateRequest(type, type, id).doc(doc);
										bulkRequestBuilder.add(updateRequest);
									}
								}
							}else{
								if (starter.cfsrest.bean.Constant.CFS_CHECK_FLAG_VALUE_ONE.equals(checkFlag)) {
									type = starter.cfsrest.bean.Constant.CFS_MASTERINDEX_TYPE;
								}else if(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG_VALUE_THREE.equals(checkFlag)){
									type = starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE;
								}else{
									type = starter.cfsrest.bean.Constant.CFS_COPYEINDEX_TYPE;
								}
								doc.remove(starter.cfsrest.bean.Constant.CFS_CHECK_FLAG);
								doc.remove("SCAN_MAN");
								cfsCommonService.beforeUpdate(doc);
								logger.info(opType + ":\r\n" + Json.parse(doc).toXContentBuilder().string());
								UpdateRequest updateRequest = new UpdateRequest(type, type, id).doc(doc);
								bulkRequestBuilder.add(updateRequest);
							}
						}
						int numberOfActions = bulkRequestBuilder.numberOfActions();
						logger.info("numberOfActions:"+numberOfActions);
						if (numberOfActions>0) {
							BulkResponse response = bulkRequestBuilder.get();
							if (deleteIdentify) {
								xContentBuilder.startObject();
								xContentBuilder.field("zipId", StringUtils.isNotEmpty(zip.getName())?zip.getName():null)
								.field("success",!response.hasFailures())
								.field("msg", response.hasFailures() == true ? "同步失败" : "")
								.endObject();
								if (response.hasFailures()) {
									logger.info("同步失败："+response.buildFailureMessage() );
								}
							}else{
								xContentBuilder = xDeleteContentBuilder;
							}
						} else{
							xContentBuilder = xDeleteContentBuilder;
						}
					}
				}
			} catch (Throwable e) {
				logger.error(e);
				xContentBuilder.startObject();
				xContentBuilder.field("zipId", zip.getName())
				.field("success", false)
				.field("msg","同步失败")
				.endObject();
			} finally {
				clean(root);
			}
		}
		xContentBuilder.endArray();
		logger.info(xContentBuilder.string());
		return xContentBuilder;
	}

	private Map<String,Object> getMetadataOfImageId(String image_id,String type){
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(type).setTypes(type);
		BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
		booleanBuilder.should(QueryBuilders.matchQuery("IMAGE_ID", image_id));
		searchRequestBuilder.setQuery(booleanBuilder);
		//process result
		SearchResponse searchResponse = searchRequestBuilder.setVersion(true).execute().actionGet();
		SearchHit[] hits = searchResponse.getHits().getHits();
		if (hits.length!=0) {
			Map<String, Object> source = hits[0].getSource();
			return source;
		} else {
			GetRequestBuilder getRequestBuilder = client.prepareGet(type, type, image_id);
	        GetResponse getResponse = getRequestBuilder.execute().actionGet();
			return getResponse.isExists()?getResponse.getSource():null;
		}
	}

	/*
	 * 上传压缩包里面的文件到服务器所产生的日志
	 */
	public XContentBuilder uploadLogsZip(List<MultipartFile> zips) throws IOException {
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
		xContentBuilder.startArray();
		for (MultipartFile zip : zips) {
			String root = getRoot();
			xContentBuilder.startObject();
			try{
				unZip(zip, root);//解压ZIP
			} catch (IOException e){
				logger.error(e.getMessage());
				xContentBuilder.field("zipId", zip.getName())
				.field("success", false)
				.field("msg", String.format("unzip %s failed" + zip.getName()))
				.endObject();
				continue;
			}

			File file = new File(FilenameUtils.concat(root, "metadata.txt"));
			String jsonString = null;
			try {
				jsonString = FileUtils.readFileToString(file, "UTf-8");
			} catch (IOException e) {
				logger.error(e.getMessage());
				xContentBuilder.field("zipId", zip.getName())
				.field("success", false)
				.field("msg", e.getMessage())
				.endObject();
				continue;
			}
			logger.info("upload json is:\r\n" + jsonString);
			try {
				List<Map<String, Object>> json = objectMapper.readValue(jsonString, List.class);
				if (json == null || json.isEmpty()) {
					logger.error("the metadata is null");
					xContentBuilder.field("zipId", zip.getName())
					.field("success", false)
					.field("msg", "the metadata is null")
					.endObject();
					continue;
				}

				List<Map<String, Object>> docs = new ArrayList<>();
				for (Map<String, Object> body : json) {
					body.put("SCAN_TIME", CommonUtils.getServerTime("yyyy-MM-dd HH:mm:ss"));
					Json doc = null;
					// type必须指定
					String type = (String) body.get("_type");
					if (StringUtils.isBlank(type)) {
						if (doc == null) {
							doc = Json.parse(body);
						}
						xContentBuilder.field("zipId", zip.getName())
						.field("success", false)
						.field("msg", "_type is required")
						.field("doc", doc.toXContentBuilder().string())
						.endObject();
						docs.clear();
						break;
					}

					//检查文档类型是否存在
					List<String> types = Arrays.asList(cache.typeIds());
					boolean isExists=false;
					for (String tp : types) {
						if(type.equals(tp)){
							isExists = true;
						}
					}
					if (!isExists) {
						xContentBuilder.field("zipId", zip.getName())
						.field("success", false)
						.field("msg", "_type is not exists")
						.field("doc", doc.toXContentBuilder().string())
						.endObject();
						docs.clear();
						break;
					}
					try {// 校验
						cfsCommonService.formatAcl(body);
						cfsCommonService.validate(body, type);
					} catch (Throwable e) {
						xContentBuilder.field("zipId", zip.getName())
						.field("success", false)
						.field("msg", e.getMessage())
						.endObject();
						docs.clear();
						logger.error(e.getMessage());
						break;
					}
					body.put("_type", type);
					docs.add(body);
				}


				if (!docs.isEmpty()) {
					//do bulk insert
					BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
					for (Map<String, Object> doc : docs) {
						String type = (String) doc.get("_type");
						documentService.beforeCreate(doc);
						IndexRequest indexRequest = new IndexRequest(type, type).source(doc);
						UUID randomUUID = UUID.randomUUID();
						String id = randomUUID.toString();
						indexRequest.id(id);
						logger.info("create:\r\n" + Json.parse(doc).toXContentBuilder().string());
						bulkRequestBuilder.add(indexRequest);
					}
					BulkResponse response = bulkRequestBuilder.get();
					xContentBuilder.field("zipId", zip.getName())
					.field("success", !response.hasFailures())
					.field("msg", response.hasFailures() == true ? response.buildFailureMessage() : "")
					.endObject();
				}
			} catch (Throwable e) {
				logger.error(e);
				xContentBuilder.field("zipId", zip.getName())
				.field("success", false)
				.field("msg", e.getMessage())
				.endObject();
			} finally {
				clean(root);
			}
		}
		xContentBuilder.endArray();
		logger.info(xContentBuilder.string());
		return xContentBuilder;
	}
	/*
	 * 获取根路径
	 */
	private String getRoot() {
		return FilenameUtils.concat(hrbbConfig.getUploadTempPath(), UUID.randomUUID().toString());
	}
	/*
	 * 解压文件
	 */
	private void unZip(MultipartFile zip, String path) throws IOException{
		InputStream is = null;
		logger.info("start unzip " + zip.getName());
		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		is = zip.getInputStream();
		ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(is));
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			FileOutputStream fos = new FileOutputStream(new File(FilenameUtils.concat(directory.getAbsolutePath(), zipEntry.getName())));
			IOUtils.copy(zipInputStream, fos);
			IOUtils.closeQuietly(fos);
		}
		logger.info("unzip success");
		IOUtils.closeQuietly(is);
	}

	/*
	 * 清空文件夹
	 */
	private void clean(String directoryPath) {
		File directory = new File(directoryPath);
		try {
			FileUtils.deleteDirectory(directory);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

}
