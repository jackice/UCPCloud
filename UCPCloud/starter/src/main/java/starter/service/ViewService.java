package starter.service;


import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import starter.RequestContext;
import starter.rest.Json;
import starter.uContentException;

import java.io.IOException;
import java.util.*;

@Service
public class ViewService {

    @Autowired
    private RequestContext context;

    @Autowired
    private UserService userService;

    private Logger logger = Logger.getLogger(ViewService.class);

    public void initialViewData() throws IOException {
        Client client = context.getClient();

        //创建View Mapping
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(Constant.SYSTEM).addTypes(Constant.FieldName.VIEWTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if(mappings.size()==0){
            //冇得，那就搞一个吧。。。
            XContentBuilder builder= XContentFactory.jsonBuilder();
            builder.startObject();
            builder.startObject(Constant.FieldName.VIEWTYPENAME);
            builder.startObject("properties")
                    .startObject(Constant.FieldName.VIEWNAME).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.QUERYCONTEXT).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.USERS).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.GROUPS).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();
            builder.endObject();//end of typeName
            builder.endObject();
            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(Constant.SYSTEM).type(Constant.FieldName.VIEWTYPENAME).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
        }else{
            //艹，居然有！！！！！
        }
    }

    public List<Json> all() throws IOException {
        Client client = context.getClient();
        SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.VIEWTYPENAME).setSize(Integer.MAX_VALUE).execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        List<Json> views = new ArrayList<Json>();
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            Json view = new Json();
            view.put(Constant.FieldName._ID, searchHitFields.getId());
            view.put(Constant.FieldName.VIEWNAME, searchHitFields.getSource().get(Constant.FieldName.VIEWNAME));
            view.put(Constant.FieldName.QUERYCONTEXT, searchHitFields.getSource().get(Constant.FieldName.QUERYCONTEXT));
            view.put(Constant.FieldName.USERS, searchHitFields.getSource().get(Constant.FieldName.USERS));
            view.put(Constant.FieldName.GROUPS, searchHitFields.getSource().get(Constant.FieldName.GROUPS));
            views.add(view);
        }
        return views;
    }

    public XContentBuilder create(Json body) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();

        body.remove(Constant.FieldName._ID);
        Json json = checkView(body);
        Object viewName = body.get(Constant.FieldName.VIEWNAME);
        if (!StringUtils.isEmpty(viewName)) {
            QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.VIEWNAME, viewName);
            SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.VIEWTYPENAME).setQuery(queryBuilder).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (searchHits.totalHits() > 0) {
                logger.error(String.format(context.m("ViewService.viewNameExist"), viewName));
                throw new uContentException(String.format(context.m("ViewService.viewNameExist"), viewName), HttpStatus.BAD_REQUEST);
            }
        }

        IndexResponse indexResponse = client.prepareIndex(Constant.SYSTEM, Constant.FieldName.VIEWTYPENAME).setSource(json).execute().actionGet();
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
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.VIEWTYPENAME, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        if (getResponse.isExists()){
            Json json = new Json();
            json.put(Constant.FieldName._ID, id);
            json.put(Constant.FieldName.VIEWNAME, source.get(Constant.FieldName.VIEWNAME));
            json.put(Constant.FieldName.USERS, source.get(Constant.FieldName.USERS));
            json.put(Constant.FieldName.GROUPS, source.get(Constant.FieldName.GROUPNAME));
            json.put(Constant.FieldName.QUERYCONTEXT, source.get(Constant.FieldName.QUERYCONTEXT));
            return json;
        }else{
            logger.error(String.format(context.m("ViewService.viewNotExist"), id));
            throw new uContentException(String.format(context.m("ViewService.viewNotExist"), id), HttpStatus.NOT_FOUND);
        }
    }

    public XContentBuilder update(String id, Json body) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.VIEWTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("ViewService.viewNotExist"), id));
            throw new uContentException(String.format(context.m("ViewService.viewNotExist"), id), HttpStatus.NOT_FOUND);
        }else{

        }

        body.remove(Constant.FieldName._ID);
        Json json = checkView(body);

        Object viewName = body.get(Constant.FieldName.VIEWNAME);
        if (!StringUtils.isEmpty(viewName)) {
            QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.VIEWNAME, viewName);
            SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.VIEWTYPENAME).setQuery(queryBuilder).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (searchHits.totalHits()>0){
                //修改时发现与其他view的viewName相同
                for (SearchHit searchHitFields : searchHits) {
                    if(!id.equals(searchHitFields.getId())){
                        logger.error(String.format(context.m("ViewService.viewNameExist"), viewName));
                        throw new uContentException(String.format(context.m("ViewService.viewNameExist"), viewName), HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }

        UpdateResponse updateResponse = context.getClient().prepareUpdate(Constant.SYSTEM, Constant.FieldName.VIEWTYPENAME, id).setDoc(json).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", Constant.SYSTEM)
                .field("_type", Constant.FieldName.VIEWTYPENAME)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }


    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.VIEWTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("ViewService.viewNotExist"), id));
            throw new uContentException(String.format(context.m("ViewService.viewNotExist"), id), HttpStatus.NOT_FOUND);
        }else{

        }

        DeleteResponse deleteResponse = client.prepareDelete(Constant.SYSTEM, Constant.FieldName.VIEWTYPENAME, id).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", Constant.SYSTEM)
                .field("_type", Constant.FieldName.VIEWTYPENAME)
                .field("_id", id)
                .field("_version", deleteResponse.getVersion())
                .field("found", deleteResponse.isFound())
                .endObject();
        return builder;
    }

    public List<Json> getViewsByUser(String id) throws IOException {
        List<Json> views = new ArrayList<Json>();
        //获取用户的所有view
        Client client = context.getClient();
        QueryBuilder qbUsers = QueryBuilders.termQuery(Constant.FieldName.USERS, id);
        SearchResponse srUsers = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.VIEWTYPENAME).setQuery(qbUsers).execute().actionGet();
        for (SearchHit searchHitFields : srUsers.getHits()) {
            Json view = new Json();
            view.put(Constant.FieldName._ID, searchHitFields.getId());
            view.put(Constant.FieldName.VIEWNAME, searchHitFields.getSource().get(Constant.FieldName.VIEWNAME));
            view.put(Constant.FieldName.QUERYCONTEXT, searchHitFields.getSource().get(Constant.FieldName.QUERYCONTEXT));
            boolean exist = false;
            for(Json json:views){
                if (json.get(Constant.FieldName._ID).equals(searchHitFields.getId())){
                    exist = true;
                    break;
                }
            }
            if (!exist){
                views.add(view);
            }
        }

        //获取用户所在的所有组的所有view
        List<String> groups = userService.getGroupsOfUser(id);
        if(groups!=null && groups.size()>0){
            for(String groupId:groups){
                QueryBuilder quGroups = QueryBuilders.termQuery(Constant.FieldName.GROUPS, groupId);
                SearchResponse srGroups = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.VIEWTYPENAME).setQuery(quGroups).execute().actionGet();
                for (SearchHit searchHitFields : srGroups.getHits()) {
                    Json view = new Json();
                    view.put(Constant.FieldName._ID, searchHitFields.getId());
                    view.put(Constant.FieldName.VIEWNAME, searchHitFields.getSource().get(Constant.FieldName.VIEWNAME));
                    view.put(Constant.FieldName.QUERYCONTEXT, searchHitFields.getSource().get(Constant.FieldName.QUERYCONTEXT));
                    boolean exist = false;
                    for(Json json:views){
                        if (json.get(Constant.FieldName._ID).equals(searchHitFields.getId())){
                            exist = true;
                            break;
                        }
                    }
                    if (!exist){
                        views.add(view);
                    }
                }
            }
        }
        return views;
    }

    private Json checkView(Json body){
        Json json = new Json();
        Iterator<Map.Entry<String, Object>> it = body.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            if((entry.getKey().equals(Constant.FieldName.VIEWNAME)||entry.getKey().equals(Constant.FieldName.QUERYCONTEXT)||
                    entry.getKey().equals(Constant.FieldName.USERS)||entry.getKey().equals(Constant.FieldName.GROUPS))){
                json.put(entry.getKey(), entry.getValue());
            }
        }
        return json;
    }

}

