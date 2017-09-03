/**
 * 
 */
package starter.cfsrest.service;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import starter.RequestContext;
import starter.cfsrest.bean.Constant;
import starter.service.DocumentService;

/**
 * @author JackIce
 * @date  2016年5月26日
 * @description 
 */
@Service
public class CFSTreeService{
	@Autowired
	private RequestContext context;
	private Logger logger = Logger.getLogger(DocumentService.class);
	/*
	 * 根据类型，查询的文件类型，请求类型 进行查询
	 * 当请求类型为0时，根据fun_code查询
	 * 否则根据msg_code查询
	 */
	public XContentBuilder query(String type, int start, int limit,String queryFileValue,String request_from) throws IOException {
		SearchRequestBuilder searchRequestBuilder = null;
		searchRequestBuilder = context.getClient().prepareSearch(context.getAlias()).setTypes(type);
		//set from and limit
		searchRequestBuilder.setFrom(start).setSize(limit);
		//set query
		if (StringUtils.isNotBlank(queryFileValue)) {
			if (Constant.CFS_COMMON_DIGIT_STRING_ZERO.equals(request_from)) {
				BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
				booleanBuilder.should(QueryBuilders.matchQuery(Constant.CFS_FUN_CODE_KEY, queryFileValue));
				searchRequestBuilder.setQuery(booleanBuilder);
			} else {
				BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
				booleanBuilder.should(QueryBuilders.matchQuery(Constant.CFS_TREE_MSG_CODE, queryFileValue.substring(0,2)));
				searchRequestBuilder.setQuery(booleanBuilder);
			}
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
		return xContentBuilder;
	}

	/*
	 * 根据类型进行查询
	 */
	
	public XContentBuilder query(String type, int start, int limit) throws IOException {
		SearchRequestBuilder searchRequestBuilder = null;
		searchRequestBuilder = context.getClient().prepareSearch(context.getAlias()).setTypes(type);
		//set from and limit
		searchRequestBuilder.setFrom(start).setSize(limit);
		//_fullText field not return
		String[] exclude = {"_streams._fullText"};
		searchRequestBuilder.setFetchSource(null, exclude);
		//process result
		SearchResponse searchResponse = searchRequestBuilder.setVersion(true).execute().actionGet();
		XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
		xContentBuilder.field("text", "非结构化数据管理平台");
		xContentBuilder.field("classes", "folder");
		xContentBuilder.field("expanded", true);
		xContentBuilder.startArray("children");
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			xContentBuilder.startObject();
			Map<String, Object> source = hit.getSource();
			xContentBuilder.field("id", source.get("msg_code"));
			xContentBuilder.field("text", source.get("msg_name"));
			xContentBuilder.field("classes", "folder");
			xContentBuilder.field("expanded", true);
			while (source.containsKey("children")) {
				
			}
//			xContentBuilder.endObject();
		}
		xContentBuilder.endArray().endObject();
		return xContentBuilder;
	}
}
