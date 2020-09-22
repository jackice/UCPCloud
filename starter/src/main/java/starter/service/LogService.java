package starter.service;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.RequestContext;

import java.io.IOException;
import java.util.Map;

@Service
public class LogService {
    @Autowired
    private RequestContext context;

    private final String LOG_TYPE_NAME = "logInfo";
    private final String LOG_INDDX_NAME = "ucp_log";

    public XContentBuilder query(String query, int from, int size, SortBuilder[] sorts) throws IOException {
        SearchRequestBuilder searchRequestBuilder = context.getClient()
                .prepareSearch(Constant.LOG_INDEX)
                .setTypes(LOG_TYPE_NAME)
                .setFrom(from)
                .setSize(size);

        if (sorts != null && sorts.length != 0) {
            for (SortBuilder sortBuilder : sorts) {
                searchRequestBuilder.addSort(sortBuilder);
            }
        }

        //因故替换参数类型:query改为String
        //if (query != null && !query.isEmpty()) {
        //    searchRequestBuilder.setQuery(query);
        //}
        if (StringUtils.isNotBlank(query)) {
            searchRequestBuilder.setQuery(query);
        }

        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        XContentBuilder builder = XContentFactory.jsonBuilder();
        SearchHits searchHits = searchResponse.getHits();
        builder.startObject()
                .field("total", searchHits.totalHits());
        builder.startArray("logInfos");
        for (SearchHit searchHitFields : searchHits) {
            Map<String, Object> source = searchHitFields.getSource();
            builder.value(source);
        }
        builder.endArray();
        builder.endObject();

        return builder;
    }


    public XContentBuilder createLog(XContentBuilder builder_in) throws IOException {
        Client client = context.getClient();
        IndexResponse indexResponse = client.prepareIndex(Constant.LOG_INDEX, LOG_TYPE_NAME)
                .setSource(builder_in).execute().actionGet();

        XContentBuilder builder_out = XContentFactory.jsonBuilder();
        builder_out.startObject()
                .field("_index", indexResponse.getIndex())
                .field("_type", indexResponse.getType())
                .field("_id", indexResponse.getId())
                .field("_version", indexResponse.getVersion())
                .field("created", indexResponse.isCreated())
                .endObject();

        return builder_out;
    }

    public void initialLogData() throws IOException {
        Client client = context.getClient();

        //创建View Mapping
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(LOG_INDDX_NAME).addTypes(LOG_TYPE_NAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if(mappings.size()==0){
            //冇得，那就搞一个吧。。。
            XContentBuilder builder= XContentFactory.jsonBuilder();
            builder.startObject();
            builder.startObject(LOG_TYPE_NAME);
            builder.startObject("properties")
                    .startObject("userName").field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()

                    .startObject("timeInfo")
                    .startObject(Constant.FieldName.PROPERTIES)
                    .startObject("start").field(Constant.FieldName.TYPE, "long").endObject()
                    .startObject("start_format").field(Constant.FieldName.TYPE, "string").endObject()
                    .startObject("end").field(Constant.FieldName.TYPE, "long").endObject()
                    .startObject("end_format").field(Constant.FieldName.TYPE, "string").endObject()
                    .startObject("consume").field(Constant.FieldName.TYPE, "long").endObject()
                    .startObject("consume_format").field(Constant.FieldName.TYPE, "string").endObject()
                    .endObject()
                    .endObject()

                    .startObject("requestInfo")
                    .startObject(Constant.FieldName.PROPERTIES)
                    .startObject("ipAddress").field(Constant.FieldName.TYPE, "string").endObject()
                    .startObject("url").field(Constant.FieldName.TYPE, "string").endObject()
                    .startObject("method").field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject("params").field(Constant.FieldName.TYPE, "string").endObject()
                    .startObject("header").field(Constant.FieldName.TYPE, "string").endObject()
                    .endObject()
                    .endObject()

                    .startObject("responseInfo")
                    .startObject(Constant.FieldName.PROPERTIES)
                    .startObject("statusCode").field(Constant.FieldName.TYPE, "long").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject("header").field(Constant.FieldName.TYPE, "string").endObject()
                    .startObject("result").field(Constant.FieldName.TYPE, "string").endObject()
                    .endObject()
                    .endObject()

                    .startObject("hasError").field(Constant.FieldName.TYPE, Constant.Type.BOOLEAN).endObject()
                         
                    .startObject("exceptionInfo")
                    .startObject(Constant.FieldName.PROPERTIES)
                    .startObject("msg").field(Constant.FieldName.TYPE, "string").field("analyzer","simple").endObject()
                    .startObject("statusCode").field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject("stackTrace").field(Constant.FieldName.TYPE, "string").field("analyzer","simple").endObject()
                    .endObject()
                    .endObject()

               
                    
                    .startObject("logDate").field("type", "date").endObject()
                    
                    //设置_all的分词类型
                     .startObject("_all").field(Constant.FieldName.TYPE, "string").field("analyzer", "simple").endObject()
                    ;
            builder.endObject();//end of typeName
            builder.endObject();
            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(LOG_INDDX_NAME).type(LOG_TYPE_NAME).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
        }else{
            //艹，居然有！！！！！
        }
    }

}
