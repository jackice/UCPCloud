package starter.service;


import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import starter.RequestContext;
import starter.rest.Json;
import starter.uContentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class TagService {

    @Autowired
    private RequestContext context;

    private Logger logger = Logger.getLogger(TagService.class);

    public void initialTagData() throws IOException {
        Client client = context.getClient();

        //创建Tag Mapping
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(Constant.SYSTEM).addTypes(Constant.FieldName.TAGTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if(mappings.size()==0){
            //冇得，那就搞一个吧。。。
            XContentBuilder builder= XContentFactory.jsonBuilder();
            builder.startObject();
            builder.startObject(Constant.FieldName.TAGTYPENAME);
            builder.startObject("properties")
                    .startObject(Constant.FieldName.TAGCONTEXT).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.DESCRIPTION).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();
            builder.endObject();//end of typeName
            builder.endObject();
            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(Constant.SYSTEM).type(Constant.FieldName.TAGTYPENAME).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
        }else{
            //艹，居然有！！！！！
        }
    }

    public List<Json> all() throws IOException {
        Client client = context.getClient();
        SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.TAGTYPENAME).execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        List<Json> tags = new ArrayList<Json>();
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            Json tag = new Json();
            tag.put(Constant.FieldName._ID, searchHitFields.getId());
            tag.put(Constant.FieldName.TAGCONTEXT, searchHitFields.getSource().get(Constant.FieldName.TAGCONTEXT));
            tag.put(Constant.FieldName.DESCRIPTION, searchHitFields.getSource().get(Constant.FieldName.DESCRIPTION));
            tags.add(tag);
        }
        return tags;
    }

    public XContentBuilder all(String query, int start, int limit, SortBuilder[] sort) throws IOException {
        SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.TAGTYPENAME);
        if (limit>0){
            searchRequestBuilder.setFrom(start).setSize(limit);
            if (query != null && !query.isEmpty()) {
                searchRequestBuilder.setQuery(query);
            }
        }
        //set sort
        if (sort != null && sort.length > 0) {
            for(SortBuilder sortBuilder : sort){
                searchRequestBuilder.addSort(sortBuilder);
            }
        }
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject().field("total", searchResponse.getHits().totalHits());
        builder.startArray("tags");
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            builder.startObject()
                    .field("_id", searchHitFields.getId())
                    .field(Constant.FieldName.TAGCONTEXT, searchHitFields.getSource().get(Constant.FieldName.TAGCONTEXT))
                    .field(Constant.FieldName.DESCRIPTION, searchHitFields.getSource().get(Constant.FieldName.DESCRIPTION))
                    .endObject();

        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

    public XContentBuilder create(Json body) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();

        body.remove(Constant.FieldName._ID);
        Json json = checkTag(body);
        Object tagContext = body.get(Constant.FieldName.TAGCONTEXT);
        if (!StringUtils.isEmpty(tagContext)) {
            QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.TAGCONTEXT, tagContext);
            SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.TAGTYPENAME).setQuery(queryBuilder).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (searchHits.totalHits() > 0) {
                logger.error(String.format(context.m("TagService.tagContextExist"), tagContext));
                throw new uContentException(String.format(context.m("TagService.tagContextExist"), tagContext), HttpStatus.BAD_REQUEST);
            }
        }

        IndexResponse indexResponse = client.prepareIndex(Constant.SYSTEM, Constant.FieldName.TAGTYPENAME).setSource(body).execute().actionGet();
        builder.startObject()
                .field("_index", indexResponse.getIndex())
                .field("_type", indexResponse.getType())
                .field("_id", indexResponse.getId())
                .field("_version", indexResponse.getVersion())
                .field("created", indexResponse.isCreated())
                .endObject();
        return builder;
    }

    public Json get(String id){
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.TAGTYPENAME, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        if (getResponse.isExists()){
            Json json = new Json();
            json.put(Constant.FieldName._ID, id);
            json.put(Constant.FieldName.TAGCONTEXT, source.get(Constant.FieldName.TAGCONTEXT));
            json.put(Constant.FieldName.DESCRIPTION, source.get(Constant.FieldName.DESCRIPTION));
            return json;
        }else{
            logger.error(String.format(context.m("TagService.tagNotExist"), id));
            throw new uContentException(String.format(context.m("TagService.tagNotExist"), id), HttpStatus.NOT_FOUND);
        }
    }

    public XContentBuilder update(String id, Json body) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.TAGTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("TagService.tagNotExist"), id));
            throw new uContentException(String.format(context.m("TagService.tagNotExist"), id), HttpStatus.NOT_FOUND);
        }else{

        }

        body.remove(Constant.FieldName._ID);
        Json json = checkTag(body);

        Object tagContext = body.get(Constant.FieldName.TAGCONTEXT);
        if (!StringUtils.isEmpty(tagContext)) {
            QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.TAGCONTEXT, tagContext);
            SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.TAGTYPENAME).setQuery(queryBuilder).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (searchHits.totalHits()>0){
                for (SearchHit searchHitFields : searchHits) {
                    if(!id.equals(searchHitFields.getId())){
                        logger.error(String.format(context.m("TagService.tagContextExist"), tagContext));
                        throw new uContentException(String.format(context.m("TagService.tagContextExist"), tagContext), HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }

        UpdateResponse updateResponse = context.getClient().prepareUpdate(Constant.SYSTEM, Constant.FieldName.TAGTYPENAME, id).setDoc(body).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", Constant.SYSTEM)
                .field("_type", Constant.FieldName.TAGTYPENAME)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }


    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.TAGTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("TagService.tagNotExist"), id));
            throw new uContentException(String.format(context.m("TagService.tagNotExist"), id), HttpStatus.NOT_FOUND);
        }else{

        }

        DeleteResponse deleteResponse = client.prepareDelete(Constant.SYSTEM, Constant.FieldName.TAGTYPENAME, id).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", Constant.SYSTEM)
                .field("_type", Constant.FieldName.TAGTYPENAME)
                .field("_id", id)
                .field("_version", deleteResponse.getVersion())
                .field("found", deleteResponse.isFound())
                .endObject();
        return builder;
    }

    private Json checkTag(Json body){
        Json json = new Json();
        Iterator<Map.Entry<String, Object>> it = body.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            if((entry.getKey().equals(Constant.FieldName.DESCRIPTION)||
                    entry.getKey().equals(Constant.FieldName.TAGCONTEXT))){
                json.put(entry.getKey(), entry.getValue());
            }
        }
        return json;
    }

}

