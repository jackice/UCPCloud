/**
 * 
 */
package starter.cfsrest.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.AndQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.itextpdf.text.pdf.PRAcroForm;

import starter.RequestContext;
import starter.uContentException;
import starter.cfsrest.exception.CFSImgException;
import starter.rest.Json;
import starter.service.Constant;
import starter.service.DocumentService;
import starter.service.TypeService;
import starter.service.UserService;
import starter.service.fs.FileSystem;
import starter.service.hrbb.Cache;
/**
 * @author JackIce
 * @date  2016年5月30日
 * @description 
 */
@Service
public class CFSCommonService {
	@Autowired
	private RequestContext context;

	@Autowired
	private FileSystem fs;

	@Autowired
	private TypeService typeService;

	@Autowired
	private UserService userService;

	@Autowired
	private Cache cache;

	private Logger logger = Logger.getLogger(CFSCommonService.class);

	private static Tika tika = new Tika();

/*
 * 根据类型types，机构号org_id，查询语句query，查询开始start，查询的记录数limit，排序方式sort，是否批量查询allowableActions，是否全文查询fulltext，是否包含类型containsType
 */
	public XContentBuilder query(String[] types,String org_id, String query, int start, int limit, SortBuilder[] sort, boolean allowableActions, boolean fulltext, boolean containsType) throws IOException {
		SearchRequestBuilder searchRequestBuilder = null;
		//set types
		if (types == null || types.length == 0) {
			types = cache.typeIds();
			searchRequestBuilder = context.getClient().prepareSearch(context.getAlias()).setTypes(types);
		} else {
			searchRequestBuilder = context.getClient().prepareSearch(types).setTypes(types);
		}
		//set from and limit
		searchRequestBuilder.setFrom(start).setSize(limit);
		//set query
		if (StringUtils.isNotBlank(query)) {
			if (fulltext) {
				setQueryAndHighlight(searchRequestBuilder, query);
			} else {
				searchRequestBuilder.setQuery(query);
			}
		}
		//set sort
		if (sort != null && sort.length > 0) {
			for (SortBuilder sortBuilder : sort) {
				searchRequestBuilder.addSort(sortBuilder);
			}
		}
		//_fullText field not return
		String[] exclude = {"_streams._fullText"};
		searchRequestBuilder.setFetchSource(null, exclude);
		SearchResponse searchResponse = searchRequestBuilder.setVersion(true).execute().actionGet();
		long totalHits = searchResponse.getHits().getTotalHits();
		String stage_flag = starter.cfsrest.bean.Constant.CFS_STAGE_FLAG_S0;
		if (totalHits>0) {
			String flw_code = (String)searchResponse.getHits().getHits()[0].getSource().get("FLW_CODE");
			//获取阶段标识
			SearchRequestBuilder stageFlagSearchRequestBuilder = context.getClient().prepareSearch(starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE).setTypes(starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE);
			//set query
			BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
			booleanBuilder.should(QueryBuilders.matchQuery("FLW_CODE", flw_code));
			stageFlagSearchRequestBuilder.setQuery(booleanBuilder);
			SearchResponse stageFlagSearchResponse = stageFlagSearchRequestBuilder.setVersion(true).execute().actionGet();
			if (stageFlagSearchResponse.getHits().getTotalHits()!=0) {
				stage_flag = (String)stageFlagSearchResponse.getHits().getHits()[0].getSource().get("STAGE_FLAG");
			}
		}

		XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
		xContentBuilder.field("total", searchResponse.getHits().getTotalHits());
		xContentBuilder.startArray("documents");
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			Map<String, Object> source = hit.getSource();
			String type_code = (String)source.get("TYPE_CODE");
			String file_type = (String)source.get("FILE_TYPE");
			String matchs = "|.doc|.docx|.xls|.xlsx|.pdf|";
			logger.info("file_type:"+file_type+"type_code:"+type_code);
			if(StringUtils.isNotEmpty(file_type)&&file_type.matches(matchs)){
				if (starter.cfsrest.bean.Constant.CFS_TYPE_CODE_12003001003.equals(type_code)||starter.cfsrest.bean.Constant.CFS_TYPE_CODE_13001003.equals(type_code)||starter.cfsrest.bean.Constant.CFS_TYPE_CODE_12001001003.equals(type_code)) {
					logger.info("need to check org level");
					String old_orgId = (String)source.get("ORG_CODE");
					if (StringUtils.isNotEmpty(org_id)) {
						String org_level = getOrgLevel(org_id);
						boolean verity_org_code = verifyAuthority(org_id, org_level,old_orgId);
						if (!verity_org_code) {
							continue;
						}
					}
				}
			}
			xContentBuilder.startObject();
			xContentBuilder.field("_index", hit.getIndex())
			.field("_id", hit.getId())
			.field("_score", hit.getScore())
			.field("_version", hit.getVersion())
			.field("_highlight", hit.getHighlightFields());
			Iterator<Map.Entry<String, Object>> iterator = source.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = iterator.next();
				//ES兼容信用卡 SCAN_TIME
				if ("SCAN_TIME".equals(entry.getKey())) {
					xContentBuilder.field("SCAN_DATE", entry.getValue());
				}else{
					xContentBuilder.field(entry.getKey(), entry.getValue());
				}
			}
			xContentBuilder.field("STAGE_FLAG",stage_flag.toUpperCase());
			if (allowableActions) {
				xContentBuilder.field(Constant.FieldName.ALLOWABLEACTIONS, getUserPermission(context.getUserName(), source.get("_acl")));
			}
			if (containsType) {
				xContentBuilder.field("_type", Json.parseToMap(typeService.get(hit.getType()).string()));
			} else {
				xContentBuilder.field("_type", hit.getType());
			}
			xContentBuilder.endObject();
		}
		xContentBuilder.endArray().endObject();
		return xContentBuilder;
	}
/*
 * 判定权限
 * 根据操作人员机构id，权限等级，以及旧的机构id判定权限
 */
	private boolean verifyAuthority (String handlers_org_id,String handlers_org_level,String old_org_id){
		String old_orgLevel = getOrgLevel(old_org_id);
		logger.info(String.format("old_org_level:%s;handlers_org_level:%s", old_orgLevel,handlers_org_level));
		if (StringUtils.isNotEmpty(old_orgLevel)) {
			int old_level = Integer.parseInt(old_orgLevel);
			if (StringUtils.isNotEmpty(handlers_org_level)) {
				int handlers_level = Integer.parseInt(handlers_org_level);
				if (0==handlers_level) {
					return true;
				}else if (handlers_level<old_level&&isParantent(old_org_id, handlers_org_id)) {
					return true;
				}else if(handlers_level==old_level&&handlers_org_id.equals(old_org_id)){
					return true;
				}else{
					return false;
				}
			}else{
				logger.error("cfs_org_level:can't is empty");
				return false;
			}
		}else{
			return false;
		}
	}
/*
 * 权限相关
 * 根据原来的机构id old_org_id,存进来数据的操作人的机构id handlers_org_id进行判断，查看old_org_id是否匹配指定格式
 */
	private boolean isParantent(String old_org_id,String handlers_org_id){
		logger.info("old_org_id:"+old_org_id+"handlers_org_id"+handlers_org_id);
		MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("cfs_org_belongorgid", handlers_org_id);

		SearchResponse actionGet = context.getClient().prepareSearch(starter.cfsrest.bean.Constant.CFS_DATA_INDEX)
				.setTypes(starter.cfsrest.bean.Constant.CFS_ORG_INFO_TYPE).setQuery(matchQuery).execute().actionGet();
		StringBuilder stringBuilder = new StringBuilder();
		if (actionGet.getHits().getHits().length>0) {
			String orgid = (String) actionGet.getHits().getHits()[0].getSource().get("cfs_orgid");
			stringBuilder.append("|"+orgid);
		}else{
			logger.info("not find org message");
			return false;
		}
		stringBuilder.append("|");
		logger.info("orgmatchs:"+stringBuilder.toString());
		if (old_org_id.matches(stringBuilder.toString())) {
			return true;
		}
		return false;
	}
	/*
	 * 权限相关
	 * 根据机构id org_id获取机构等级
	 */
	private String getOrgLevel(String org_id){
		SearchResponse actionGet = context.getClient().prepareSearch(starter.cfsrest.bean.Constant.CFS_DATA_INDEX)
				.setQuery(QueryBuilders.idsQuery(starter.cfsrest.bean.Constant.CFS_ORG_INFO_TYPE)
						.addIds(org_id)).execute().actionGet();
		String org_level = starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_SIX;
		if (actionGet.getHits().getHits().length>0) {
			org_level = (String) actionGet.getHits().getHits()[0].getSource().get("cfs_org_level");
			logger.info("org level is"+org_level);
		}else{
			logger.info("not find org message");
		}
		return org_level;
	}
/*
 * 根据条件进行日志查询
 * 类型types，第几页page，每页记录数limit，查询条件conditions
 */
	public XContentBuilder queryByCondition(String[] types,  int page, int limit,String ... conditions) throws IOException {
		SearchRequestBuilder searchRequestBuilder = null;
		//set types
		if (types == null || types.length == 0) {
			//	            types = typeService.getAllTypes().toArray(new String[]{});
			types = cache.typeIds();
			searchRequestBuilder = context.getClient().prepareSearch(context.getAlias()).setTypes(types);
		} else {
			searchRequestBuilder = context.getClient().prepareSearch(types).setTypes(types);
		}
		//set from and limit
		int start = page*limit;
		searchRequestBuilder.setFrom(start).setSize(limit);
		//_fullText field not return
		String[] exclude = {"_streams._fullText","_acl"};
		searchRequestBuilder.setFetchSource(null, exclude);
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		if (StringUtils.isNotEmpty(conditions[0])) {
			boolQuery.must(QueryBuilders.matchQuery("BIS_SYS_CODE", conditions[0]));
		}
		if (StringUtils.isNotEmpty(conditions[1])) {
			boolQuery.must(QueryBuilders.matchQuery("OPERATE_PERSON", conditions[1]));
		}
		if (StringUtils.isNotEmpty(conditions[2])) {
			boolQuery.must(QueryBuilders.matchQuery("CUSTOM_CODE", conditions[2]));
		}
		if (StringUtils.isNotEmpty(conditions[3])) {
			boolQuery.must(QueryBuilders.matchQuery("FLW_CODE", conditions[3]));
		}
		if (StringUtils.isNotEmpty(conditions[4])) {
			boolQuery.must(QueryBuilders.matchQuery("IMG_NAME", conditions[4]));
		}
		if (StringUtils.isNotEmpty(conditions[5])) {
			boolQuery.must(QueryBuilders.matchQuery("ORG_CODE", conditions[5]));
		}
		if (StringUtils.isNotEmpty(conditions[6])) {
			boolQuery.must(QueryBuilders.matchQuery("BATCH_CODE", conditions[6]));
		}
		if (StringUtils.isNotEmpty(conditions[7])) {
			boolQuery.must(QueryBuilders.prefixQuery("OPERATE_DATE", conditions[7]));
		}
		//		BoolQueryBuilder filter = QueryBuilders.boolQuery().should(QueryBuilders.termQuery("_acl.read.users", context.getUserName()));
		//		//	        List<String> groups = userService.getGroupsOfUser(context.getUserName());
		//		List<String> groups = cache.getGroupsOfUser(context.getUserName());
		//		for (String group : groups) {
		//			filter.should(QueryBuilders.termQuery("_acl.read.groups", group));
		//		}
		//		filter.minimumNumberShouldMatch(1);
		//		searchRequestBuilder.setPostFilter(filter);
		searchRequestBuilder.setQuery(boolQuery);
		//process result
		SearchResponse searchResponse = searchRequestBuilder.setVersion(true).execute().actionGet();
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
		xContentBuilder.field("total", searchResponse.getHits().getTotalHits());
		xContentBuilder.startArray("documents");
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			xContentBuilder.startObject();
			Map<String, Object> source = hit.getSource();
			Iterator<Map.Entry<String, Object>> iterator = source.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = iterator.next();
				xContentBuilder.field(entry.getKey(), entry.getValue());
			}
			xContentBuilder.endObject();
		}
		xContentBuilder.endArray().endObject();
		return xContentBuilder;
	}

	/*
	 * 根据类型types，统计域的名称countFiledName，客户号custom_code，业务号business_code，功能号fun_code统计图片数量
	 */
	public String imgCount(String[] types,String countFiledName,String custom_code,String business_code,String fun_code) throws Exception {
		SearchRequestBuilder searchRequestBuilder = null;
		//set types
		if (types == null || types.length == 0) {
			//	            types = typeService.getAllTypes().toArray(new String[]{});
			types = cache.typeIds();
			searchRequestBuilder = context.getClient().prepareSearch(context.getAlias()).setTypes(types);
		} else {
			searchRequestBuilder = context.getClient().prepareSearch(types).setTypes(types);
		}
		//_fullText field not return
		String[] exclude = {"_streams._fullText"};
		searchRequestBuilder.setFetchSource(null, exclude);

		TermsBuilder termsBuilder = new TermsBuilder(starter.cfsrest.bean.Constant.CFS_OPERATION_IMG_COUNT);
		TermsBuilder field = termsBuilder.field(countFiledName);
		searchRequestBuilder.addAggregation(field);
		BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
		if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
			booleanBuilder.must(QueryBuilders.matchQuery("CUSTOM_CODE", custom_code));
		}else{
			booleanBuilder.must(QueryBuilders.matchQuery("FLW_CODE", business_code));
		}
		booleanBuilder.must(QueryBuilders.matchQuery("FUN_CODE", fun_code));
		booleanBuilder.must(QueryBuilders.matchQuery("IS_DELETE", starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ZERO));
		searchRequestBuilder.setQuery(booleanBuilder);
		//process result
		SearchResponse searchResponse = searchRequestBuilder.setVersion(true).execute().actionGet();
		StringBuilder stringBuilder = new StringBuilder("");
		try {

			Aggregations aggregations = searchResponse.getAggregations();
			Map<String, Aggregation> aggregation_map = aggregations.asMap();
			StringTerms term = (StringTerms)aggregation_map.get(starter.cfsrest.bean.Constant.CFS_OPERATION_IMG_COUNT);
			List<Bucket> buckets = term.getBuckets();
			for (int i=0;i<buckets.size();i++) {
				Bucket bucket = buckets.get(i);
				stringBuilder.append(String.format("%s:%s",bucket.getKey(),bucket.getDocCount()+""));
				if (i!=buckets.size()-1) {
					stringBuilder.append(",");
				}
			}
		} catch (Exception e) {
			throw new Exception(String.format("img_count is fail:%s",e.getMessage()));
		}
		return stringBuilder.toString();
	}
	
	/*
	 *  query data about compress to zipFile
	 *  根据客户号customerCode、业务号businessCode、功能号fun_code，类型types，分类码bisinfCode查询要压缩的图片信息
	 *  返回map集合，其中key为image_id,value为list集合，集合内容为{分类编码TYPE_CODE，文件类型FILE_TYPE，图片名称IMAGE_NAME}
	 */
	
	public Map queryZipData(String businessCode,String customerCode,String fun_code,String[] types,String bisinfCode) throws IOException {
		String code=businessCode;
		String esCode="FLW_CODE";
		String[] str = bisinfCode.split(",");
		if(starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)){
			code=customerCode;
			esCode="CUSTOM_CODE";
		}
		SearchResponse searchResponse = null;
		/*
		 * 
		 */
		List<String> list=new ArrayList<String>();
		if (str.length>1) {
			for (int i=0;i<str.length;i++){
				list.add(str[i]);
			}
		}else if(StringUtils.isNotEmpty(bisinfCode)){
			list.add(bisinfCode);
		}
		BoolQueryBuilder queryBuilder = null;
		if (list.size()>=1) {
			queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery(esCode, code)).must(QueryBuilders.matchQuery("IS_DELETE", starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ZERO)).must(QueryBuilders.matchQuery("FUN_CODE", fun_code.toUpperCase())).must(QueryBuilders.termsQuery("TYPE_CODE", list));
		}else{
			queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery(esCode, code)).must(QueryBuilders.matchQuery("IS_DELETE", starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ZERO)).must(QueryBuilders.matchQuery("FUN_CODE", fun_code.toUpperCase()));
		}

		//set types
		if (types == null || types.length == 0) {
			types = cache.typeIds();
			searchResponse = context.getClient().prepareSearch(context.getAlias()).setTypes(types).setScroll(new TimeValue(10000)).setQuery(queryBuilder).setSize(100).execute().actionGet();
		} else {
			searchResponse = context.getClient().prepareSearch(types).setTypes(types).setScroll(new TimeValue(10000)).setQuery(queryBuilder).setSize(100).execute().actionGet();
		}
		Map map = null;
		Map resMap = new HashMap();
		while(searchResponse.getHits().getHits().length!=0){
			for(SearchHit hit:searchResponse.getHits().getHits()){
				try {
					String id = hit.getId();
					map = Json.parseToMap(hit.getSourceAsString());
					List listes = new ArrayList();
					listes.add(map.get("TYPE_CODE"));
					listes.add(map.get("FILE_TYPE"));
					listes.add(map.get("IMAGE_NAME"));
					resMap.put(id,listes);
				} catch (IOException e) {
					e.printStackTrace();
				}    	
			}
			searchResponse = context.getClient().prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(10000)).execute().actionGet();
		}
		return resMap;
	}

	/*
	 *  根据客户号custom_Code、业务号busniss_Code、功能号fun_code，类型types查询
	 */
	public Map query(String[] types, String custom_code,String busniss_code, String fun_code,int start, int limit) throws IOException {
		SearchRequestBuilder searchRequestBuilder = null;
		//set types
		if (types == null || types.length == 0) {
			types = cache.typeIds();
			searchRequestBuilder = context.getClient().prepareSearch(context.getAlias()).setTypes(types);
		} else {
			searchRequestBuilder = context.getClient().prepareSearch(types).setTypes(types);
		}
		//set from and limit
		searchRequestBuilder.setFrom(start).setSize(limit);
		//set query
		if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
			BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
			booleanBuilder.should(QueryBuilders.matchQuery("CUSTOM_CODE", custom_code));
			searchRequestBuilder.setQuery(booleanBuilder);
		}else{
			BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
			booleanBuilder.should(QueryBuilders.matchQuery("FLW_CODE", busniss_code));
			searchRequestBuilder.setQuery(booleanBuilder);
		}
		//_fullText field not return
		String[] exclude = {"_streams._fullText"};
		searchRequestBuilder.setFetchSource(null, exclude);
		//process result
		SearchResponse searchResponse = searchRequestBuilder.setVersion(true).execute().actionGet();
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
		xContentBuilder.field("total", searchResponse.getHits().getTotalHits());
		xContentBuilder.startArray("documents");
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			xContentBuilder.startObject();
			xContentBuilder.field("_index", hit.getIndex())
			.field("_id", hit.getId())
			.field("_score", hit.getScore())
			.field("_version", hit.getVersion())
			.field("_highlight", hit.getHighlightFields());
			Map<String, Object> source = hit.getSource();
			Iterator<Map.Entry<String, Object>> iterator = source.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = iterator.next();
				xContentBuilder.field(entry.getKey(), entry.getValue());
			}
			xContentBuilder.endObject();
		}
		xContentBuilder.endArray().endObject();
		System.out.println(xContentBuilder.string());
		return Json.parseToMap(xContentBuilder.string());
	}
	
	/*
	 *对查询结果进行高亮处理
	 */
	private void setQueryAndHighlight(SearchRequestBuilder searchRequestBuilder, String query) {
		if (StringUtils.isNotBlank(query)) {
			boolean isInteger = false;
			boolean isFloat = false;
			boolean isDateTime = false;
			try {
				Integer.parseInt(query);
				isInteger = true;
			} catch (NumberFormatException e) {
			}
			try {
				Float.parseFloat(query);
				isFloat = true;
			} catch (NumberFormatException e) {
			}
			try {
				ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(query);
				isDateTime = true;
			} catch (Throwable e) {
			}
			try {
				List<Json> types = typeService.getAll(true);
				if (types.isEmpty()) {
					return;
				}
				Set<String> stringFields = new HashSet<>();
				stringFields.add("name");
				stringFields.add("createdBy");
				stringFields.add("lastUpdatedBy");
				stringFields.add("tag");
				stringFields.add("_acl");
				stringFields.add("_streams._fullText");
				stringFields.add("_streams.streamId");
				stringFields.add("_streams.streamName");
				stringFields.add("_streams.contentType");
				Set<String> intFields = new HashSet<>();
				Set<String> floatFields = new HashSet<>();
				Set<String> dateTimeFields = new HashSet<>();
				dateTimeFields.add("createdOn");
				dateTimeFields.add("lastUpdatedOn");
				for (Json json : types) {
					List<Json> properties = (List) json.get("properties");
					if (properties != null && !properties.isEmpty()) {
						for (Json j : properties) {
							String type = j.get("type").toString();
							if (type.equals("boolean")) {
								continue;
							}
							String name = j.get("name").toString();
							switch (type) {
							case "integer":
								intFields.add(name);
								break;
							case "float":
								floatFields.add(name);
								break;
							case "date":
								dateTimeFields.add(name);
								break;
							default:
								stringFields.add(name);
							}
						}
					}

				}
				BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
				for (String stringValue : stringFields) {
					booleanBuilder.should(QueryBuilders.matchQuery(stringValue, query));
					searchRequestBuilder.addHighlightedField(stringValue);
				}
				if (isInteger) {
					for (String intValue : intFields) {
						booleanBuilder.should(QueryBuilders.matchQuery(intValue, query));
					}
				}
				if (isFloat) {
					for (String floatValue : floatFields) {
						booleanBuilder.should(QueryBuilders.matchQuery(floatValue, query));
					}
				}
				if (isDateTime) {
					for (String dateTimeValue : dateTimeFields) {
						booleanBuilder.should(QueryBuilders.matchQuery(dateTimeValue, query));
					}
				}
				searchRequestBuilder.setQuery(booleanBuilder);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
/*
 * 进行创建图片记录，来自api
 */
	public XContentBuilder createOfApi(String index,String type,List<Map> jsons) throws IOException{
		XContentBuilder contentBuilder = JsonXContent.contentBuilder();
		contentBuilder.startArray();
		BulkRequestBuilder prepareBulk = context.getClient().prepareBulk();

		if (jsons.size()!=0) {
			Map map = jsons.get(0);
			GetRequestBuilder getRequestBuilder = context.getClient().prepareGet(starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE, starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE, (String)map.get("FLW_CODE"));
			GetResponse getResponse = getRequestBuilder.execute().actionGet();
			if (!getResponse.isExists()) {
				Map cfs_service_dataMap = new HashMap();
				cfs_service_dataMap.put("CUSTOM_CODE", (String)map.get("CUSTOM_CODE"));
				cfs_service_dataMap.put("name",(String)map.get("FLW_CODE"));
				cfs_service_dataMap.put("FLW_CODE", (String)map.get("FLW_CODE"));
				cfs_service_dataMap.put("IS_FILE", "0");
				cfs_service_dataMap.put("STAGE_FLAG", "s0");
				cfs_service_dataMap.put("REMARK", "");
				formatAcl(cfs_service_dataMap);
				validate(cfs_service_dataMap, starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE);
				//				beforeCreate(cfs_service_dataMap);
				IndexResponse indexResponse = context.getClient().prepareIndex(starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE, starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE).setSource(cfs_service_dataMap).setId((String)map.get("FLW_CODE")).execute().actionGet();
				XContentBuilder builder = JsonXContent.contentBuilder();
				builder.startObject()
				.field("_index", indexResponse.getIndex())
				.field("_type", indexResponse.getType())
				.field("_id", indexResponse.getId())
				.field("_version", indexResponse.getVersion())
				.field("_created", indexResponse.isCreated())
				.endObject();
				logger.info("create:\r\n" + Json.parse(cfs_service_dataMap).toXContentBuilder().string()+"\n result:"+builder.string());
			}
		}
		for (Map body : jsons) {
			formatAcl(body);
			validate(body, type);
			//			beforeCreate(body);
			IndexRequestBuilder indexRequest = context.getClient().prepareIndex(index, type).setId((String)body.get("IMAGE_ID")).setSource(body);
			//			IndexRequestBuilder indexRequest = context.getClient().prepareIndex(index, type).setSource(body);
			prepareBulk.add(indexRequest);
		}
		BulkResponse actionGet = prepareBulk.execute().actionGet();
		for (BulkItemResponse bulkItemResponse : actionGet) {
			contentBuilder.startObject()
			.field("_index", bulkItemResponse.getIndex())
			.field("_type", bulkItemResponse.getType())
			.field("_id", bulkItemResponse.getId())
			.field("_version", bulkItemResponse.getVersion())
			.field("_created", bulkItemResponse.isFailed())
			.endObject();
		}
		return contentBuilder;
	}
/*
 *创建图片记录 
 */
	public XContentBuilder create(String type, Map body) throws IOException {
		formatAcl(body);
		validate(body, type);
		beforeCreate(body);
		IndexResponse indexResponse = context.getClient().prepareIndex(type, type).setSource(body).execute().actionGet();
		XContentBuilder builder = JsonXContent.contentBuilder();
		builder.startObject()
		.field("_index", indexResponse.getIndex())
		.field("_type", indexResponse.getType())
		.field("_id", indexResponse.getId())
		.field("_version", indexResponse.getVersion())
		.field("_created", indexResponse.isCreated())
		.endObject();
		return builder;
	}
	/*
	 * 格式化权限数据
	 */
	public void formatAcl(Map body) {
		Object o = body.get("_acl");
		if (o != null && StringUtils.isNotBlank(o.toString())) {
			if (o instanceof Map) {
				return;
			}
			if (o instanceof String) {
				try {
					Map<String, Object> acl = Json.parseToMap(o.toString());
					body.put("_acl", acl);
				} catch (IOException e) {
					logger.error("The _acl in request body is not correct");
					throw new uContentException(context.m("Document.AclNotCorrect"), e, HttpStatus.BAD_REQUEST);
				}
			}
			//	        } else {
			//	            body.put("_acl", null);
		}
	}
	/*
	 *创建图片记录 
	 */
	public XContentBuilder create(String type, Map body, List<MultipartFile> files) throws IOException {
		if (!files.isEmpty()) {
			List<Map<String, Object>> streams = new ArrayList<Map<String, Object>>();
			for (MultipartFile file : files) {
				Map<String, Object> stream = uploadStream(type,file);
				streams.add(stream);
			}
			body.put(Constant.FieldName.STREAMS, streams);
		}
		return create(type, body);
	}
	/*
	 * 上传文件流
	 */
	public Map<String, Object> uploadStream(String type,MultipartFile file) throws IOException {
		Map<String, Object> stream = new HashMap<>();
		String fileId = null;
		try {
			fileId = fs.write(type,file.getBytes());
		} catch (IOException e) {
			logger.error(String.format("The stream %s store failed\r\n %s", file.getOriginalFilename(), e.getMessage()));
			throw new uContentException(String.format(context.m("Stream.StreamStoreFailed"), file.getOriginalFilename()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (StringUtils.isBlank(fileId)) {
			logger.error(String.format("The stream %s store failed", file.getOriginalFilename()));
			throw new uContentException(String.format(context.m("Stream.StreamStoreFailed"), file.getOriginalFilename()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		stream.put(Constant.FieldName.STREAMID, fileId);
		stream.put(Constant.FieldName.STREAMNAME, file.getOriginalFilename());
		stream.put(Constant.FieldName.LENGTH, file.getSize());
		TikaInputStream tikaInputStream = null;
		String mediaType = "";
		try {
			tikaInputStream = TikaInputStream.get(file.getInputStream());
			mediaType = tika.detect(tikaInputStream, file.getOriginalFilename());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch(ArrayIndexOutOfBoundsException e){
			new uContentException("文件内容为空，不能上传", HttpStatus.INTERNAL_SERVER_ERROR);
		}finally{
			IOUtils.closeQuietly(tikaInputStream);
		}
		stream.put(Constant.FieldName.CONTENTTYPE, mediaType);
		if (context.getTikaConfig().getMediaTypes().contains(mediaType)) {
			int maxStringLength = context.getTikaConfig().getMaxStringLength();
			if (maxStringLength > 0) {
				maxStringLength *= 1000;
				tika.setMaxStringLength(maxStringLength);
			} else if (maxStringLength == -1) {
				tika.setMaxStringLength(maxStringLength);
			}
			stream.put(Constant.FieldName.FULLTEXT, parse(file));
		}
		if ("image/tiff".equalsIgnoreCase(mediaType)) {
			ImageReader reader = ImageIO.getImageReadersByFormatName("tif").next();
			reader.setInput(ImageIO.createImageInputStream(file.getInputStream()));
			int pageCount = reader.getNumImages(true);
			stream.put(Constant.FieldName.PAGECOUNT, pageCount);
		}
		return stream;
	}
	/*
	 * 上传文件流，来自api
	 */
	public XContentBuilder uploadStreamOfApi(String type,String filename,byte[] files) throws IOException {
		SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(type).setTypes(type);
		//set query
		BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
		logger.info("filename:"+filename);
		booleanBuilder.should(QueryBuilders.matchQuery("IMAGE_NAME",filename));
		searchRequestBuilder.setQuery(booleanBuilder);
		XContentBuilder builder = null;
		SearchResponse actionGet = null;
		for (int i = 0; i < 10; i++) {
			actionGet = searchRequestBuilder.execute().actionGet();
			if (actionGet.getHits().getTotalHits()>=1) {
				break;
			}else{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error(String.format("api image upload %d count fail,thread sleep has exception:%s",i,e));
				}
				logger.info(String.format("%d count query fail,matadata is not find",i));
			}
		}
		if(actionGet.getHits().getTotalHits()>=1) {
			SearchHit searchHit = actionGet.getHits().getHits()[0];
			Map<String, Object> body = searchHit.getSource();
			String _id = searchHit.getId();
			logger.info("_id:"+_id);
			List<Map<String, Object>> streams = new ArrayList<>();
			Map<String, Object> stream = new HashMap<>();
			String fileId = null;
			try {
				fileId = fs.write(type,files);
				logger.info("streams upload success fileId:"+fileId);
			} catch (IOException e) {
				logger.error(String.format("The stream %s store failed\r\n %s",filename, e.getMessage()));
				throw new uContentException(String.format(context.m("Stream.StreamStoreFailed"), filename), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			if (StringUtils.isBlank(fileId)) {
				logger.error(String.format("The stream %s store failed", filename));
				throw new uContentException(String.format(context.m("Stream.StreamStoreFailed"), filename), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			stream.put(Constant.FieldName.STREAMID, fileId);
			stream.put(Constant.FieldName.STREAMNAME, filename);
			stream.put(Constant.FieldName.LENGTH,files.length);
			TikaInputStream tikaInputStream = null;
			String mediaType = "";
			ByteArrayInputStream inputStream = new ByteArrayInputStream(files);
			try {
				tikaInputStream = TikaInputStream.get(inputStream);
				mediaType = tika.detect(tikaInputStream,filename);
				logger.info("mediaType:"+mediaType);
			} catch (IOException e) {
				logger.error(e.getMessage());
			} finally{
				IOUtils.closeQuietly(tikaInputStream);
			}
			stream.put(Constant.FieldName.CONTENTTYPE, mediaType);
			if (context.getTikaConfig().getMediaTypes().contains(mediaType)) {
				int maxStringLength = context.getTikaConfig().getMaxStringLength();
				if (maxStringLength > 0) {
					maxStringLength *= 1000;
					tika.setMaxStringLength(maxStringLength);
				} else if (maxStringLength == -1) {
					tika.setMaxStringLength(maxStringLength);
				}
			}
			if ("image/tiff".equalsIgnoreCase(mediaType)) {
				ImageReader reader = ImageIO.getImageReadersByFormatName("tif").next();
				reader.setInput(ImageIO.createImageInputStream(inputStream));
				int pageCount = reader.getNumImages(true);
				stream.put(Constant.FieldName.PAGECOUNT, pageCount);
			}
			streams.add(stream);
			//upload file
			body.put(Constant.FieldName.STREAMS, streams);
			logger.info("图片元数据更新："+body);
			formatAcl(body);
			validate(body, type);
			//		beforeCreate(body);
			IndexResponse indexResponse = context.getClient().prepareIndex(type, type).setId(_id).setSource(body).execute().actionGet();
			String id = indexResponse.getId();
			boolean handle_result = false;
			if (StringUtils.isNotEmpty(id)) {
				GetResponse getResponse = context.getClient().prepareGet(type, type, id).execute().actionGet();
				logger.info("图片上传之后的元数据："+getResponse.getSourceAsString());
				ArrayList _streams = (ArrayList) getResponse.getSourceAsMap().get(Constant.FieldName.STREAMS);
				if (_streams.size()!=0) {
					handle_result = true;
				}
			}
			builder = JsonXContent.contentBuilder();
			builder.startObject()
			.field("_index", indexResponse.getIndex())
			.field("_type", indexResponse.getType())
			.field("_id", indexResponse.getId())
			.field("_version", indexResponse.getVersion())
			.field("_created", handle_result)
			.endObject();
		}else{
			logger.info("10 count query fail,matadata is not find");
		}
		logger.info(builder.string());
		return builder;
	}
	/*
	 * 根据id，是否批量查询 allowableActions,是否包含类型containsType进行查询
	 */
	public Json get(String type, String id, boolean head, boolean allowableActions, boolean containsType) throws IOException {
		GetResponse getResponse = checkPermission(type, id, context.getUserName(), Constant.Permission.read, true);
		Json json = new Json();

		json.put("_index", getResponse.getIndex());
		if (containsType) {
			json.put("_type", Json.parseToMap(typeService.get(type).string()));
		} else {
			json.put("_type", getResponse.getType());
		}
		json.put("_id", getResponse.getId());
		json.put("_found", getResponse.isExists());
		if (getResponse.isExists()) {
			json.put("_version", getResponse.getVersion());
			if (!head) {
				Map<String, Object> source = getResponse.getSource();
				if (source != null) {
					Iterator<Map.Entry<String, Object>> iterator = source.entrySet().iterator();
					while (iterator.hasNext()) {
						Map.Entry<String, Object> entry = iterator.next();
						json.put(entry.getKey(), entry.getValue());
					}
				}
			}
			if (allowableActions) {
				json.put(Constant.FieldName.ALLOWABLEACTIONS, getUserPermission(context.getUserName(), getResponse.getSource().get(Constant.FieldName.ACL)));
			}
		}
		return json;
	}
/*
 * 根据id，是否批量查询 allowableActions,是否包含类型containsType进行查询
 */
	public Json get(String id, boolean allowableActions, boolean containsType) throws IOException {
		SearchResponse searchResponse = context.getClient().prepareSearch(context.getAlias()).setQuery(QueryBuilders.termQuery("_id", id)).setVersion(true).execute().actionGet();
		if (searchResponse.getHits().getHits().length <= 0) {
			logger.warn(String.format("the doc %s is not exist", id, context.getAlias()));
			throw new uContentException(String.format(context.m("Document.DocNotExist"), id, context.getAlias()), HttpStatus.NOT_FOUND);
		}
		SearchHit searchHit = searchResponse.getHits().getHits()[0];
		if (!hasPermission(context.getUserName(), searchHit.getSource().get(Constant.FieldName.ACL), Constant.Permission.read)) {
			logger.warn(String.format("the user %s do not have the permission %s on doc %s", context.getUserName(), "read", id));
			throw new uContentException(String.format(context.m("Document.NoPermission"), context.getUserName(), "read", id), HttpStatus.FORBIDDEN);
		}
		Json json = new Json();
		json.put("_index", searchHit.getIndex());
		if (containsType) {
			json.put("_type", Json.parseToMap(typeService.get(searchHit.getType()).string()));
		} else {
			json.put("_type", searchHit.getType());
		}
		json.put("_id", searchHit.getId());
		json.put("_found", true);
		json.put("_version", searchHit.getVersion());
		Map<String, Object> source = searchHit.getSource();
		if (source != null) {
			Iterator<Map.Entry<String, Object>> iterator = source.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = iterator.next();
				json.put(entry.getKey(), entry.getValue());
			}
		}
		if (allowableActions) {
			json.put(Constant.FieldName.ALLOWABLEACTIONS, getUserPermission(context.getUserName(), searchHit.getSource().get(Constant.FieldName.ACL)));
		}
		return json;
	}

/*
 * 更新图片数据
 */
	public XContentBuilder update(String type, String id, Json body, List<MultipartFile> files) throws IOException {
		GetRequestBuilder getRequestBuilder = context.getClient().prepareGet(type, type, id);
		GetResponse getResponse = getRequestBuilder.execute().actionGet();
		List<Map<String, Object>> streams = new ArrayList<Map<String, Object>>();
		if (getResponse.isExists()) {//update
			if (!hasPermission(context.getUserName(), getResponse.getSource().get(Constant.FieldName.ACL), Constant.Permission.write)) {
				logger.warn(String.format("the user %s do not have the permission %s on doc %s", context.getUserName(), Constant.Permission.write, id));
				throw new uContentException(String.format(context.m("Document.NoPermission"), context.getUserName(), Constant.Permission.write, id), HttpStatus.FORBIDDEN);
			}
			Object _streams = getResponse.getSource().get("_streams");
			if (_streams != null) {
				List<Map<String, Object>> oldSteams = (List<Map<String, Object>>) _streams;
				Object o = body.get("_removeStreamIds");
				if (o != null && StringUtils.isNotBlank(o.toString())) {
					String[] split = o.toString().split(",");
					List<String> deleteList = new ArrayList<>();
					Collections.addAll(deleteList, split);
					Iterator<Map<String, Object>> iterator = oldSteams.iterator();
					while (iterator.hasNext()) {
						String streamId = iterator.next().get("streamId").toString();
						if (deleteList.contains(streamId)) {
							iterator.remove();
							continue;
						}
					}
				}
				streams.addAll(oldSteams);
			}

		}
		formatAcl(body);
		validate(body, type);
		if (files != null && !files.isEmpty()) {
			for (MultipartFile file : files) {
				Map<String, Object> stream = uploadStream(type,file);
				streams.add(stream);
			}
		}
		body.put(Constant.FieldName.STREAMS, streams);
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
		if (!getResponse.isExists()) {
			beforeCreate(body);
			IndexResponse indexResponse = context.getClient().prepareIndex(type, type, id).setSource(body).execute().actionGet();
			xContentBuilder.startObject()
			.field("_index", indexResponse.getIndex())
			.field("_type", indexResponse.getType())
			.field("_id", indexResponse.getId())
			.field("_version", indexResponse.getVersion())
			.field("_created", indexResponse.isCreated())
			.endObject();
		}else{
			beforeUpdate(body);
			addAdminGrouptoDoc(body);
			UpdateResponse updateResponse = context.getClient().prepareUpdate(type, type, id).setDoc(body).execute().actionGet();
			xContentBuilder.startObject()
			.field("_index", updateResponse.getIndex())
			.field("_type", updateResponse.getType())
			.field("_id", updateResponse.getId())
			.field("_version", updateResponse.getVersion())
			.field("_updated", true);
			xContentBuilder.endObject();
		}
		return xContentBuilder;
	}
	/*
	 * 文件归档
	 */
	public Map imgFileUpdate(String type, String id,String old_isfile,String fun_code,String custom_code,String business_code, Json body) throws IOException {
		//20160705 新增归档逻辑
		if (starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ONE.equals(old_isfile)&&
				starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ONE.equals(body.get("IS_FILE"))) {
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
				boolQuery.must(QueryBuilders.matchQuery("CUSTOM_CODE", custom_code));
			} else {
				boolQuery.must(QueryBuilders.matchQuery("FLW_CODE", business_code));
			}
			boolQuery.must(QueryBuilders.matchQuery("CHECK", starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ZERO));
			SearchResponse searchResponse = context.getClient().prepareSearch().setTypes(starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE).setScroll(new TimeValue(10000)).setQuery(boolQuery).setSize(100).execute().actionGet();
			BulkRequestBuilder prepareBulk = context.getClient().prepareBulk();
			BulkResponse actionGet = null;
			while(searchResponse.getHits().getTotalHits()!=0){
				for (SearchHit hits : searchResponse.getHits()) {
					String _id = hits.getId();
					Map<String, Object> doc = hits.getSource();
					doc.remove("CHECK");
					formatAcl(doc);
					validate(doc, starter.cfsrest.bean.Constant.CFS_MASTERINDEX_TYPE);
					//handle data 
					IndexRequestBuilder requestBuilder = context.getClient().prepareIndex(starter.cfsrest.bean.Constant.CFS_MASTERINDEX_TYPE, starter.cfsrest.bean.Constant.CFS_MASTERINDEX_TYPE).setId(_id).setSource(doc);
					prepareBulk.add(requestBuilder);
				}
				searchResponse = context.getClient().prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(10000)).execute().actionGet();
				//break contition
				if((searchResponse.getHits().getHits()).length==0){
					actionGet = prepareBulk.execute().actionGet();
					break;
				}
			}
			if (actionGet!=null) {
				for (BulkItemResponse bir : actionGet) {
					if (bir.isFailed()) {
						logger.info("data already is_file，copy new data fail，id:"+bir.getId());
					}else{	
						DeleteRequestBuilder prepareDelete = context.getClient().prepareDelete();
						prepareDelete.setIndex(starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE)
						.setType(starter.cfsrest.bean.Constant.CFS_UPDATEINDEX_TYPE).setId(bir.getId());
						DeleteResponse deleteResponse = prepareDelete.execute().actionGet();
						logger.info("data already is_file，after copy new data to delete data，id:"+bir.getId());
					}
				}
			}else{
				logger.info("data already is_file，copy new data fail");
			}
		}
		String is_file = (String) body.get("IS_FILE");
		logger.info("is_file:"+is_file);
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
			boolQuery.must(QueryBuilders.matchQuery("CUSTOM_CODE", custom_code));
		} else {
			boolQuery.must(QueryBuilders.matchQuery("FLW_CODE", business_code));
		}
		SearchResponse searchResponse = context.getClient().prepareSearch().setTypes(type,type).setScroll(new TimeValue(10000)).setQuery(boolQuery).setSize(100).execute().actionGet();
		BulkRequestBuilder prepareBulk = context.getClient().prepareBulk();
		BulkResponse actionGet = null;
		while(searchResponse.getHits().getTotalHits()!=0){
			for (SearchHit hits : searchResponse.getHits()) {
				String _id = hits.getId();
				//update
				Map<String, Object> doc = hits.getSource();
				List<Map<String, Object>> streams = new ArrayList<Map<String, Object>>();
				Object _streams = doc.get("_streams");
				if (_streams != null) {
					List<Map<String, Object>> oldSteams = (List<Map<String, Object>>) _streams;
					Object o = doc.get("_removeStreamIds");
					if (o != null && StringUtils.isNotBlank(o.toString())) {
						String[] split = o.toString().split(",");
						List<String> deleteList = new ArrayList<>();
						Collections.addAll(deleteList, split);
						Iterator<Map<String, Object>> iterator = oldSteams.iterator();
						while (iterator.hasNext()) {
							String streamId = iterator.next().get("streamId").toString();
							if (deleteList.contains(streamId)) {
								iterator.remove();
								continue;
							}
						}
					}
					streams.addAll(oldSteams);
				}
				doc.put("IS_FILE", is_file);
				formatAcl(doc);
				doc.put(Constant.FieldName.STREAMS, streams);
				//				beforeCreate(body);
				logger.info("update is_file,doc:"+doc);
				UpdateRequestBuilder updateRequestBuilder = context.getClient().prepareUpdate(type, type, _id).setDoc(doc);
				prepareBulk.add(updateRequestBuilder);

			}
			searchResponse = context.getClient().prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(10000)).execute().actionGet();
			//break contition
			if((searchResponse.getHits().getHits()).length==0){
				actionGet = prepareBulk.execute().actionGet();
				break;
			}
		}
		boolean isFileResult = true;
		if (actionGet!=null) {
			for (BulkItemResponse bir : actionGet) {
				if (bir.isFailed()) {
					isFileResult = false;
					logger.info("bulk update is_file fail");
					break;
				}
			}
		}else{
			logger.info("bulk update is_file fail ,reson:actionGet is null");
		}
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
		xContentBuilder.startObject()
		.field("_updated", isFileResult);
		xContentBuilder.endObject();

		return Json.parseToMap(xContentBuilder.string());
	}

	/*
	 * update stage，更新阶段标示
	 * 根据id，阶段标示stage_flag，客户号custom_code，业务号business_code，功能号fun_code，类型types进行阶段标示变更
	 */
	@SuppressWarnings("deprecation")
	public Map imgFileUpdateStage(String id,String stage_flag,String custom_code,String business_code,String fun_code,String ... types) throws IOException {
		String query_code = "FLW_CODE";
		String query_value = business_code;
		if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
			query_code = "CUSTOM_CODE";
			query_value = custom_code;
		} 
		if (!starter.cfsrest.bean.Constant.CFS_STAGE_FLAG_S0.equals(stage_flag)) {
			AndQueryBuilder andQuery = QueryBuilders.andQuery(QueryBuilders.termQuery(query_code, query_value),QueryBuilders.termQuery("FUN_CODE", fun_code),QueryBuilders.termQuery("TYPE_CODE",starter.cfsrest.bean.Constant.CFS_FUNNUMBER_ZERO));
			SearchResponse searchResponse = context.getClient().prepareSearch(types[0]).setTypes(types[0]).setScroll(new TimeValue(10000)).setQuery(andQuery).setSize(100).execute().actionGet();
			BulkRequestBuilder prepareBulk = context.getClient().prepareBulk();
			while(searchResponse.getHits().getTotalHits()!=0){
				for (SearchHit hits : searchResponse.getHits()) {
					//handle data 
					String img_id = hits.getId();
					Map<String,Object> doc = new HashMap<String, Object>();
					doc.put("IS_DELETE", "1");
					doc.put("REMARK", "图片未分类，修改阶段标识操作时删除此图片");
					UpdateRequestBuilder updateRequestBuilder = context.getClient().prepareUpdate(types[0],types[0],img_id).setDoc(doc);
					prepareBulk.add(updateRequestBuilder);

				}
				searchResponse = context.getClient().prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(10000)).execute().actionGet();
				//break contition
				if((searchResponse.getHits().getHits()).length==0){
					prepareBulk.execute().actionGet();
					break;
				}
			}
		}
		//update cfs_service

		MatchQueryBuilder matchQuery = QueryBuilders.matchQuery(query_code, query_value);
		SearchResponse searchResponse = context.getClient().prepareSearch(types[1]).setTypes(types[1]).setScroll(new TimeValue(10000)).setQuery(matchQuery).setSize(100).execute().actionGet();
		BulkRequestBuilder prepareBulk = context.getClient().prepareBulk();
		BulkResponse actionGet = null;
		while(searchResponse.getHits().getTotalHits()!=0){
			for (SearchHit hits : searchResponse.getHits()) {
				//handle data 
				//				Map<String, Object> doc = hits.getSource();
				Map<String,String> doc = new HashMap<String, String>();
				String _id = hits.getId();
				doc.put("STAGE_FLAG", stage_flag);
				formatAcl(doc);
				UpdateRequestBuilder updateRequestBuilder = context.getClient().prepareUpdate(types[1],types[1],_id).setDoc(doc);
				prepareBulk.add(updateRequestBuilder);

			}
			searchResponse = context.getClient().prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(10000)).execute().actionGet();
			//break contition
			if((searchResponse.getHits().getHits()).length==0){
				actionGet = prepareBulk.execute().actionGet();
				break;
			}
		}
		boolean isFileResult = true;
		if (actionGet!=null) {
			for (BulkItemResponse bir : actionGet) {
				if (bir.isFailed()) {
					isFileResult = false;
					logger.info("bulk update stage_flag fail");
					break;
				}
			}
		}else{
			logger.info("bulk update stage_flag fail ,reson:actionGet is null");
		}
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
		xContentBuilder.startObject()
		.field("_updated", isFileResult);
		xContentBuilder.endObject();
		return Json.parseToMap(xContentBuilder.string());
	}
	/*
	 * add stage添加阶段标示为S0
	 * 
	 * 根据客户号custom_code，业务号business_code,功能号fun_code,索引index,类型type进行标示绑定，绑定为S0
	 */
	@SuppressWarnings("unchecked")
	public Map addStage(String stage_flag,String custom_code,String business_code,String fun_code,String index,String type) throws IOException {
		Map cfs_service_dataMap = new HashMap();
		cfs_service_dataMap.put("CUSTOM_CODE", custom_code);
		cfs_service_dataMap.put("name", StringUtils.isNotEmpty(custom_code)?custom_code:business_code);
		cfs_service_dataMap.put("FLW_CODE", business_code);
		cfs_service_dataMap.put("IS_FILE", "0");
		cfs_service_dataMap.put("STAGE_FLAG", "S0");
		cfs_service_dataMap.put("REMARK", "");
		formatAcl(cfs_service_dataMap);
		validate(cfs_service_dataMap, starter.cfsrest.bean.Constant.CFS_SERVICEINDEX_TYPE);
		//		beforeCreate(cfs_service_dataMap);
		IndexResponse indexResponse = context.getClient().prepareIndex(index,type).setSource(cfs_service_dataMap).setId(business_code).execute().actionGet();
		XContentBuilder builder = JsonXContent.contentBuilder();
		builder.startObject()
		.field("_index", indexResponse.getIndex())
		.field("_type", indexResponse.getType())
		.field("_id", indexResponse.getId())
		.field("_version", indexResponse.getVersion())
		.field("_created", indexResponse.isCreated())
		.endObject();
		logger.info("create:\r\n" + Json.parse(cfs_service_dataMap).toXContentBuilder().string()+"\n result:"+builder.string());
		return Json.parseToMap(builder.string());
	}

	/*
	 * copy file拷贝归档
	 * 根据类型types，客户号custom_code,业务号business_codem,功能号fun_code  进行拷贝归档
	 */
	public XContentBuilder copyFile(String[] types,String custom_code,String business_code,String fun_code) throws IOException{
		QueryBuilder matchQuery = null;
		if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
			matchQuery = QueryBuilders.matchQuery("CUSTOM_CODE", custom_code);
		} else {
			matchQuery = QueryBuilders.matchQuery("FLW_CODE", business_code);
		}
		SearchResponse searchResponse = context.getClient().prepareSearch(types[0]).setTypes(types[0]).setScroll(new TimeValue(10000)).setQuery(matchQuery).setSize(100).execute().actionGet();
		BulkRequestBuilder prepareBulk = context.getClient().prepareBulk();
		BulkResponse actionGet = null;
		while(searchResponse.getHits().getTotalHits()!=0){
			for (SearchHit hits : searchResponse.getHits()) {
				String _id = hits.getId();
				Map<String, Object> body = hits.getSource();
				formatAcl(body);
				validate(body, types[1]);
				//handle data 
				IndexRequestBuilder requestBuilder = context.getClient().prepareIndex(types[1], types[1]).setId(_id).setSource(body);
				prepareBulk.add(requestBuilder);

			}
			searchResponse = context.getClient().prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(10000)).execute().actionGet();
			//break contition
			if((searchResponse.getHits().getHits()).length==0){
				actionGet = prepareBulk.execute().actionGet();
				break;
			}
		}
		XContentBuilder builder = null;
		if (actionGet!=null) {
			builder = JsonXContent.contentBuilder();
			for (BulkItemResponse bulkItemResponse : actionGet) {
				builder.startObject()
				.field("_index", bulkItemResponse.getIndex())
				.field("_type", bulkItemResponse.getType())
				.field("_id", bulkItemResponse.getId())
				.field("_version", bulkItemResponse.getVersion())
				.field(bulkItemResponse.getOpType(), bulkItemResponse.isFailed())
				.endObject();
			}
		}
		return builder;
	}

	/*
	 * cancel copy file取消拷贝归档
	 * type类型
	 * custom_code客户号
	 * business_code 业务号
	 * fun_code功能号
	 */
	public XContentBuilder cancelCopyFile(String type,String custom_code,String business_code,String fun_code) throws IOException{
		QueryBuilder matchQuery = null;
		if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
			matchQuery = QueryBuilders.matchQuery("CUSTOM_CODE", custom_code);
		} else {
			matchQuery = QueryBuilders.matchQuery("FLW_CODE", business_code);
		}
		SearchResponse searchResponse = context.getClient().prepareSearch(type).setTypes(type).setScroll(new TimeValue(10000)).setQuery(matchQuery).setSize(100).execute().actionGet();
		BulkRequestBuilder prepareBulk = context.getClient().prepareBulk();
		BulkResponse actionGet = null;
		while(searchResponse.getHits().getTotalHits()!=0){
			for (SearchHit hits : searchResponse.getHits()) {
				//handle data 
				DeleteRequestBuilder prepareDelete = context.getClient().prepareDelete(type, type, hits.getId());
				prepareBulk.add(prepareDelete);
			}
			searchResponse = context.getClient().prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(10000)).execute().actionGet();
			//break contition
			if((searchResponse.getHits().getHits()).length==0){
				actionGet = prepareBulk.execute().actionGet();
				break;
			}
		}
		XContentBuilder builder = null;
		if (actionGet!=null) {
			builder = JsonXContent.contentBuilder();
			builder.startArray();
			for (BulkItemResponse bulkItemResponse : actionGet) {
				builder.startObject()
				.field("_index", bulkItemResponse.getIndex())
				.field("_type", bulkItemResponse.getType())
				.field("_id", bulkItemResponse.getId())
				.field("_version", bulkItemResponse.getVersion())
				.field(bulkItemResponse.getOpType(), bulkItemResponse.isFailed())
				.endObject();
			}
			builder.endArray();
		}
		return builder;
	}
/*
 * 修改归档
 *  type类型
 * custom_code客户号
 * business_code 业务号
 * fun_code功能号
 */
	public XContentBuilder updateFile(String[] types,String custom_code,String business_code,String fun_code) throws IOException{
		QueryBuilder matchQuery = null;
		if (starter.cfsrest.bean.Constant.CFS_QLCXD_CUSTOMERS.equals(fun_code)) {
			matchQuery = QueryBuilders.matchQuery("CUSTOM_CODE", custom_code);
		} else {
			matchQuery = QueryBuilders.matchQuery("FLW_CODE", business_code);
		}
		SearchResponse searchResponse = context.getClient().prepareSearch(types[1]).setTypes(types[1]).setScroll(new TimeValue(10000)).setQuery(matchQuery).setSize(100).execute().actionGet();
		BulkRequestBuilder prepareBulk = context.getClient().prepareBulk();
		BulkResponse actionGet = null;
		while(searchResponse.getHits().getTotalHits()!=0){
			for (SearchHit hits : searchResponse.getHits()) {
				String _id = hits.getId();
				Map<String, Object> body = hits.getSource();
				String check = (String) body.get("CHECK");
				logger.info("check:"+check);
				formatAcl(body);
				validate(body, types[1]);
				if (starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ONE.equals(check)) {
					body.put("IS_DELETE", starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ONE);
				}
				logger.info("update file doc:"+body);
				//				beforeCreate(body);
				//handle data 
				IndexRequestBuilder requestBuilder = context.getClient().prepareIndex(types[0], types[0]).setId(_id).setSource(body);
				prepareBulk.add(requestBuilder);

			}
			searchResponse = context.getClient().prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(10000)).execute().actionGet();
			//break contition
			if((searchResponse.getHits().getHits()).length==0){
				actionGet = prepareBulk.execute().actionGet();
				break;
			}
		}
		XContentBuilder builder = null;
		if (actionGet!=null) {
			builder = JsonXContent.contentBuilder();
			boolean isFailed = true;
			builder.startArray();
			for (BulkItemResponse bulkItemResponse : actionGet) {
				if (isFailed&&!bulkItemResponse.isFailed()) {
					isFailed = bulkItemResponse.isFailed();
				}
				builder.startObject()
				.field("_index", bulkItemResponse.getIndex())
				.field("_type", bulkItemResponse.getType())
				.field("_id", bulkItemResponse.getId())
				.field("_version", bulkItemResponse.getVersion())
				.field(bulkItemResponse.getOpType(), bulkItemResponse.isFailed())
				.endObject();
			}
			logger.info("update file result isFailed:"+isFailed);
			if (!isFailed) {
				logger.info("begain delete cfs_update data");
				XContentBuilder xContentBuilder = cancelCopyFile(types[1], custom_code, business_code,fun_code);
			}
			builder.endArray();
		}else{
			logger.info("bulkaction is null");
		}
		return builder;
	}

	/*
	 * 删除打标记
	 * type 类型
	 * ids 图片id
	 */
	public XContentBuilder imgDelete(String type, String... ids) throws IOException {
		BulkRequestBuilder prepareBulk = context.getClient().prepareBulk();
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
		xContentBuilder.startArray();
		//handle data 
		for (int i = 0; i < ids.length; i++) {
			GetRequestBuilder getRequestBuilder = context.getClient().prepareGet(type, type, ids[i]);
			GetResponse getResponse = getRequestBuilder.execute().actionGet();
			if (getResponse.isExists()) {
				UpdateRequestBuilder updateRequest = context.getClient().prepareUpdate(type, type, ids[i]).setDoc("IS_DELETE",starter.cfsrest.bean.Constant.CFS_COMMON_DIGIT_STRING_ONE);
				prepareBulk.add(updateRequest);
			}else{
				xContentBuilder.startObject()
				.field("id",String.format(":%s不存在", ids[i]))
				.endObject();
			}
		}
		BulkResponse actionGet = prepareBulk.execute().actionGet();
		for (BulkItemResponse bulkItemResponse : actionGet) {
			if (bulkItemResponse.isFailed()) {
				xContentBuilder.startObject()
				.field("id", String.format(":%s删除失败",bulkItemResponse.getId()))
				.endObject();
			}
		}
		xContentBuilder.endArray();
		return xContentBuilder;
	}

/* 
 * 删除图片
 * type 类型
* ids 图片id
 */
	public XContentBuilder delete(String type, String id) throws IOException {
		checkPermission(type, id, context.getUserName(), Constant.Permission.write);
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
		DeleteResponse deleteResponse = context.getClient().prepareDelete(type, type, id).execute().actionGet();
		xContentBuilder.startObject()
		.field("_index", deleteResponse.getIndex())
		.field("_type", deleteResponse.getType())
		.field("_id", deleteResponse.getId());
		xContentBuilder.field("_version", deleteResponse.getVersion());
		xContentBuilder.field("_found", deleteResponse.isFound());
		xContentBuilder.endObject();
		return xContentBuilder;
	}
	/* 
	 * 删除图片
	* body  type和id的map集合组成的list集合
	 */
	public XContentBuilder delete(List<Map> body) {
		XContentBuilder xContentBuilder = null;
		try {
			xContentBuilder = JsonXContent.contentBuilder().startArray();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		for (Map<String, Object> map : body) {
			String type = map.get("type").toString();
			String id = map.get("id").toString();
			try {
				checkPermission(type, id, context.getUserName(), Constant.Permission.write);
				DeleteResponse deleteResponse = context.getClient().prepareDelete(type, type, id).execute().actionGet();
				xContentBuilder.startObject().field("_index", deleteResponse.getIndex())
				.field("_type", deleteResponse.getType())
				.field("_id", deleteResponse.getId())
				.field("success", deleteResponse.isFound()).endObject();
			} catch (Exception e) {
				logger.error(e.getMessage());
				try {
					xContentBuilder.startObject().field("_alias", type)
					.field("_type", type)
					.field("_id", id)
					.field("success", false)
					.field("msg", e.getMessage()).endObject();
				} catch (IOException e1) {
					logger.error(e.getMessage());
				}
			}
		}
		try {
			xContentBuilder.endArray();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return xContentBuilder;
	}

/*
 * 在创建操作之前，添加上时间和操作人的信息
 */
	public void beforeCreate(Map body) {
		if (body != null) {
			LocalDateTime localDateTime = new DateTime().toLocalDateTime();
			body.put(Constant.FieldName.CREATEDBY, context.getUserName());
			body.put(Constant.FieldName.CREATEDON, localDateTime);
			//			body.put(starter.cfsrest.bean.Constant.CFS_DOCUMENT_ID, body.get("IMAGE_ID"));
			Map<String, Object> acl = new HashMap<>();
			Object o = body.get(Constant.FieldName.ACL);
			if (o == null || o.toString().equals("")) {
				List<String> users = new ArrayList<>();
				users.add(context.getUserName());
				List<String> groups = new ArrayList<>();
				groups.add(Constant.ADMINGROUP);
				Map<String, List<String>> read = new HashMap<>();
				read.put("users", users);
				read.put("groups", groups);
				Map<String, List<String>> write = new HashMap<>();
				write.put("users", users);
				write.put("groups", groups);
				acl.put("read", read);
				acl.put("write", write);
				body.put(Constant.FieldName.ACL, acl);
			}else{
				addAdminGrouptoDoc(body);
			}
		}
	}

	/*
	 * 添加读写管理群组
	 */
	public void addAdminGrouptoDoc(Map body){
		doAddAdminGroup(body, "read");
		doAddAdminGroup(body, "write");
	}
/*
 * 添加管理群组
 */
	private void doAddAdminGroup(Map body, String op) {
		Object o = body.get(Constant.FieldName.ACL);
		if (o == null || StringUtils.isBlank(o.toString())) {
			List<String> users = new ArrayList<>();
			List<String> groups = new ArrayList<>();
			groups.add(Constant.ADMINGROUP);
			Map<String, List<String>> read = new HashMap<>();
			read.put("users", users);
			read.put("groups", groups);
			Map<String, List<String>> write = new HashMap<>();
			write.put("users", users);
			write.put("groups", groups);
			Map<String, Object> acl = new HashMap<>();
			acl.put("read", read);
			acl.put("write", write);
			body.put(Constant.FieldName.ACL, acl);
		}else{
			Map<String, Object> acl = (Map<String, Object>) o;
			Object v = acl.get(op);
			if (v != null) {
				Map<String, List<String>> opMap = (Map<String, List<String>>) v;
				List<String> groups = opMap.get("groups");
				if (groups == null) {
					groups = new ArrayList<>();
					groups.add(Constant.ADMINGROUP);
				}
				if (!groups.contains(Constant.ADMINGROUP)) {
					groups.add(Constant.ADMINGROUP);
				}
				opMap.put("groups", groups);
				List<String> users = opMap.get("users");
				if (users == null) {
					users = new ArrayList<>();
					opMap.put("users", users);
				}

			}else{
				List<String> groups = new ArrayList<>();
				groups.add(Constant.ADMINGROUP);
				List<String> users = new ArrayList<>();
				Map<String, List<String>> map = new HashMap<>();
				map.put("groups", groups);
				map.put("users", users);
				acl.put(op, map);
			}
		}
	}

/*
 * 验证权限
 */
	private void validateAcl(Map<String, Object> acl) {
		Iterator<Map.Entry<String, Object>> it = acl.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> pEntry = it.next();
			String pKey = pEntry.getKey();
			if (!Constant.Permission.getPermissionDeclaration().contains(pKey)) {
				it.remove();
				continue;
			}
			Map<String, Object> entry = (Map<String, Object>) pEntry.getValue();
			Iterator<Map.Entry<String, Object>> iterator = entry.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> en = iterator.next();
				String key = en.getKey();
				if (!key.equals("users") && !key.equals("groups")) {
					iterator.remove();
					continue;
				}
			}
		}
	}

/*
 * 获取用户权限
 */
	public Set getUserPermission(String user, Object acl) throws IOException {
		Map<String, Object> _acl = (Map<String, Object>) acl;
		Set uPermission = getPermissionByUser(user, _acl);
		List<String> groups = getGroups(user);
		Set gPermission = getPermissionByGroups(groups, _acl);
		uPermission.addAll(gPermission);
		return uPermission;
	}
	/*
	 * 根据用户获取所在群组
	 */
	private List<String> getGroups(String user) throws IOException {
		return userService.getGroupsOfUser(user);
	}
	/*
	 * 获取用户权限
	 */
	private Set getPermissionByUser(String user, Map<String, Object> acl) {
		Set permission = new HashSet();
		if (acl != null && !acl.isEmpty()) {
			Iterator<Map.Entry<String, Object>> it = acl.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				Map<String, Object> map = (Map<String, Object>) entry.getValue();
				Object users = map.get("users");
				if (users != null) {
					List<String> _users = (List<String>) users;
					if (_users.contains(user)) {
						permission.add(entry.getKey());
						continue;
					}
				}
			}
		}
		return permission;
	}
/*
 * 获取群组权限
 */
	private Set getPermissionByGroups(List<String> groups, Map<String, Object> acl) {
		Set permission = new HashSet();
		if (groups == null || groups.isEmpty()) {
			return permission;
		}
		if (acl != null && !acl.isEmpty()) {
			Iterator<Map.Entry<String, Object>> it = acl.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				Map<String, Object> map = (Map<String, Object>) entry.getValue();
				Object g = map.get("groups");
				if (g != null) {
					List<String> _groups = (List<String>) g;
					for (String s : groups) {
						if (_groups.contains(s)) {
							permission.add(entry.getKey());
							continue;
						}
					}
				}
			}
		}
		return permission;
	}
/*
 * 验证用户是否有相应的权限
 */
	private boolean hasPermission(String user, Object acl, Constant.Permission action) throws IOException {
		if (acl!=null&&user!=null) {
			Map<String, Object> _acl = (Map<String, Object>) acl;
			Set permission = getPermissionByUser(user, _acl);
			if (permission.contains(action.toString())) {
				return true;
			} else {
				List<String> groups = getGroups(user);
				permission = getPermissionByGroups(groups, _acl);
				return permission.contains(action.toString());
			}
		}else{
			return true;
		}
	}
/*
 * 更新前设置操作人名字以及操作时间
 */
	public void beforeUpdate(Map body) {
		if (body != null) {
			String userName = starter.cfsrest.bean.Constant.CFS_MOBILE__SYSTEM;
			try{
				userName = context.getUserName();
			}catch(Exception e){
				userName = starter.cfsrest.bean.Constant.CFS_MOBILE__SYSTEM;
			}
			LocalDateTime localDateTime = new DateTime().toLocalDateTime();
			body.put(Constant.FieldName.LASTUPDATEDBY, userName);
			body.put(Constant.FieldName.LASTUPDATEDON, localDateTime);
		}
	}

/*
 * 检查权限
 * type  类型
 * id 图片id
 * user 用户
 * permission 读写权限
 * includeFullText 时候是包含全文
 */
	public GetResponse checkPermission(String type, String id, String user, Constant.Permission permission, boolean includeFullText) throws IOException {
		GetRequestBuilder getRequestBuilder = context.getClient().prepareGet(type, type, id);
		if (!includeFullText) {
			String[] exclude = {"_streams._fullText"};
			getRequestBuilder.setFetchSource(null, exclude);
		}
		GetResponse getResponse = getRequestBuilder.execute().actionGet();
		if (!getResponse.isExists()) {
			logger.warn(String.format("the doc %s in type %s of alias %s is not exist", id, type, type));
			throw new uContentException(String.format(context.m("Document.DocOnTypeOfAliasNotExist"), id, type, type), HttpStatus.NOT_FOUND);
		}
		if (!hasPermission(user, getResponse.getSource().get(Constant.FieldName.ACL), permission)) {
			logger.warn(String.format("the user %s do not have the permission %s on doc %s", user, permission, id));
			throw new uContentException(String.format(context.m("Document.NoPermission"), user, permission, id), HttpStatus.FORBIDDEN);
		}
		return getResponse;
	}
	/*
	 * 检查权限
	 * index 索引
	 * type  类型
	 * id 图片id
	 * user 用户
	 * permission 读写权限
	 * includeFullText 时候是包含全文
	 */
	public GetResponse checkPermission(String index,String type, String id, String user, Constant.Permission permission, boolean includeFullText) throws IOException {
		GetRequestBuilder getRequestBuilder = context.getClient().prepareGet(index, type, id);
		if (!includeFullText) {
			String[] exclude = {"_streams._fullText"};
			getRequestBuilder.setFetchSource(null, exclude);
		}
		GetResponse getResponse = getRequestBuilder.execute().actionGet();
		if (!getResponse.isExists()) {
			logger.warn(String.format("the doc %s in type %s of alias %s is not exist", id, type, type));
			throw new uContentException(String.format(context.m("Document.DocOnTypeOfAliasNotExist"), id, type, type), HttpStatus.NOT_FOUND);
		}
		if (!hasPermission(user, getResponse.getSource().get(Constant.FieldName.ACL), permission)) {
			logger.warn(String.format("the user %s do not have the permission %s on doc %s", user, permission, id));
			throw new uContentException(String.format(context.m("Document.NoPermission"), user, permission, id), HttpStatus.FORBIDDEN);
		}
		return getResponse;
	}
	/*
	 * 检查权限
	 * type  类型
	 * id 图片id
	 * user 用户
	 * permission 读写权限
	 */
	public GetResponse checkPermission(String type, String id, String user, Constant.Permission permission) throws IOException {
		return this.checkPermission(type, id, user, permission, false);
	}
	/*
	 * 检查权限
	 * index 索引
	 * type  类型
	 * id 图片id
	 * user 用户
	 * permission 读写权限
	 */
	public GetResponse checkPermission(String index,String type, String id, String user, Constant.Permission permission) throws IOException {
		return this.checkPermission(index,type, id, user, permission, false);
	}
/*
 * 验证权限
 */
	public void validate(Map body, String type) throws IOException {
		//	        Map<String, Map<String, Object>> definition1 = typeService.getProperties(type);
		Map<String, Map<String, Object>> definition = cache.getProperties(type);
		Set<String> keySet = definition.keySet();
		Iterator<Map.Entry<String, Object>> iterator = body.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			if (key.equals(Constant.FieldName.STREAMS) || key.equals("tag")) {
				continue;
			}
			if (key.equals(Constant.FieldName.ACL)) {
				if (entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString())) {
					validateAcl((Map<String, Object>) entry.getValue());
				}
				continue;
			}
			if (key.equals("createdBy") || key.equals("createdOn") || key.equals("lastUpdatedBy") || key.equals("lastUpdatedOn")) {
				iterator.remove();
				continue;
			}
			if (!keySet.contains(key)) {//ignore undefined property
				iterator.remove();
				continue;
			}
			Map<String, Object> property = definition.get(key);
			entry.setValue(formatValue(property, entry.getValue()));
		}
		Iterator<Map<String, Object>> it = definition.values().iterator();
		while (it.hasNext()) {
			Map<String, Object> entry = it.next();
			if ((Boolean) entry.get(Constant.FieldName.REQUIRED) == true) {
				String propName = entry.get(Constant.FieldName.NAME).toString();
				Object v = body.get(propName);
				if (v == null) {
					Object defaultValue = entry.get(Constant.FieldName.DEFAULTVALUE);
					if (defaultValue == null || defaultValue.toString().equals("")) {
						logger.error(String.format("Property %s on type %s is required", propName, type));
						throw new uContentException(String.format(context.m("Document.PropertyRequired"), propName, type), HttpStatus.BAD_REQUEST);
					}
					body.put(propName, formatValue(entry, defaultValue));
				}
			}
		}
	}
/*
 * 格式化数据
 */
	private Object formatValue(Map<String, Object> property, Object value) {
		if (value == null || value.toString().equals("")) {
			return null;
		}
		Object pattern = property.get("pattern");
		String stringValue = value.toString();
		if (pattern != null && StringUtils.isNotBlank(pattern.toString())) {
			Pattern p = Pattern.compile(pattern.toString());
			Matcher matcher = p.matcher(stringValue);
			if (!matcher.matches()) {
				logger.error(String.format("Property %s value %s can not match the pattern %s", property.get("name").toString(), stringValue, property.get("pattern").toString()));
				throw new uContentException(String.format(context.m("Document.PropertyPatternNotMatch"), property.get("name").toString(), stringValue, property.get("pattern").toString()), HttpStatus.BAD_REQUEST);
			}
		}
		String propType = property.get(Constant.FieldName.TYPE).toString();
		try {
			switch (propType) {
			case Constant.Type.INTEGER:
				return Integer.valueOf(stringValue);
			case Constant.Type.FLOAT:
				//	                    String numberPattern = "([-\\+]?[1-9]([0-9]*)(\\.[0-9]+)?)|(^0$)";
				//	                    Pattern p = Pattern.compile(numberPattern);
				//	                    if(!p.matcher(stringValue).matches()){
				//	                        logger.error(String.format("the value %s of property %s is not a number", stringValue, property.get("name").toString()));
				//	                        throw new uContentException(String.format("", stringValue, property.get("name").toString()), HttpStatus.BAD_REQUEST);
				//	                    }
				return new BigDecimal(stringValue);
			case Constant.Type.DATE:
				DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateOptionalTimeParser();
				return dateTimeFormatter.parseDateTime(stringValue).toLocalDateTime();
			case Constant.Type.BOOLEAN:
				return Boolean.valueOf(stringValue);
			default:
				return stringValue;
			}
		} catch (Throwable e) {
			logger.error(String.format("Property %s value %s is not type of %s", property.get("name").toString(), stringValue, propType));
			throw new uContentException(String.format(context.m("Document.PropertyTypeNotCorrect"), property.get("name").toString(), propType), HttpStatus.BAD_REQUEST);
		}

	}
/*
 * 根据类型，统计图片数量
 */
	public XContentBuilder count(String type) throws IOException {
		CountResponse countResponse = context.getClient().prepareCount(type).setTypes(type).execute().actionGet();
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
		xContentBuilder.startObject()
		//	                .field("_index", context.getIndex())
		.field("alias", type)
		.field("_type", type)
		.field("count", countResponse.getCount());
		xContentBuilder.endObject();
		return xContentBuilder;
	}
/*
 * 根据查询条件，每页的显示记录数，以及类型，获取总页数
 */
	private long pageCount(String type,String query,int limit){
		SearchResponse searchResponse = context.getClient().prepareSearch(type).setTypes(type).setQuery(query).execute().actionGet();
		SearchHits hits = searchResponse.getHits();
		long totalHits = hits.getTotalHits();
		long pages = totalHits/limit;
		long pageCount = totalHits%limit>0?pages+1:pages;
		return pageCount;
	}
	/*
	 * 解析文件成xml
	 */
	public String parse(MultipartFile file) {
		InputStream is = null;
		try {
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, file.getOriginalFilename());
			is = TikaInputStream.get(file.getInputStream());
			ContentHandler handler = new BodyContentHandler();
			ParseContext context = new ParseContext();
			Parser parser = new AutoDetectParser();
			context.set(Parser.class, parser);
			parser.parse(is, handler, metadata, context);
			return handler.toString();
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new uContentException(String.format(context.m("Document.StreamCanNotExtract"), file.getOriginalFilename()), e, HttpStatus.BAD_REQUEST);
		} catch (SAXException e) {
			logger.error(e.getMessage());
			throw new uContentException(String.format(context.m("Document.StreamCanNotExtract"), file.getOriginalFilename()), e, HttpStatus.BAD_REQUEST);
		} catch (TikaException e) {
			logger.error(e.getMessage());
			throw new uContentException(String.format(context.m("Document.StreamCanNotExtract"), file.getOriginalFilename()), e, HttpStatus.BAD_REQUEST);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	/*
	 * 解析文件成xml
	 */
	public String parse(InputStream inputStream,String fileName) {
		InputStream is = null;
		try {
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);
			is = TikaInputStream.get(inputStream);
			ContentHandler handler = new BodyContentHandler();
			ParseContext context = new ParseContext();
			Parser parser = new AutoDetectParser();
			context.set(Parser.class, parser);
			parser.parse(is, handler, metadata, context);
			return handler.toString();
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new uContentException(String.format(context.m("Document.StreamCanNotExtract"), fileName), e, HttpStatus.BAD_REQUEST);
		} catch (SAXException e) {
			logger.error(e.getMessage());
			throw new uContentException(String.format(context.m("Document.StreamCanNotExtract"), fileName), e, HttpStatus.BAD_REQUEST);
		} catch (TikaException e) {
			logger.error(e.getMessage());
			throw new uContentException(String.format(context.m("Document.StreamCanNotExtract"), fileName), e, HttpStatus.BAD_REQUEST);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}


}


