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
import org.elasticsearch.action.search.SearchType;
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
import java.util.*;

@Service
public class UserService {

    @Autowired
    private RequestContext context;

    @Autowired
    private Client client;

    @Autowired
    private GroupService groupService;

    private Logger logger = Logger.getLogger(UserService.class);

    public XContentBuilder all(String query, int start, int limit, SortBuilder[] sort) throws IOException {
        SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.USERTYPENAME);
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
        builder.startArray("users");
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            builder.startObject()
                    .field("_id", searchHitFields.getId())
                    .field(Constant.FieldName.USERID, searchHitFields.getSource().get(Constant.FieldName.USERID))
                    .field(Constant.FieldName.USERNAME, searchHitFields.getSource().get(Constant.FieldName.USERNAME))
                    .field(Constant.FieldName.EMAIL, searchHitFields.getSource().get(Constant.FieldName.EMAIL))
                    .field(Constant.FieldName.PASSWORD, searchHitFields.getSource().get(Constant.FieldName.PASSWORD))
                    .field(Constant.FieldName.CREATEDBY, searchHitFields.getSource().get(Constant.FieldName.CREATEDBY))
                    .field(Constant.FieldName.CREATEDON, searchHitFields.getSource().get(Constant.FieldName.CREATEDON))
                    .field(Constant.FieldName.LASTUPDATEDBY, searchHitFields.getSource().get(Constant.FieldName.LASTUPDATEDBY))
                    .field(Constant.FieldName.LASTUPDATEDON, searchHitFields.getSource().get(Constant.FieldName.LASTUPDATEDON))
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
        validateUser(body, "create", "");

        body.put(Constant.FieldName.CREATEDBY, context.getUserName());
        body.put(Constant.FieldName.CREATEDON, new Date());
        body.put(Constant.FieldName.LASTUPDATEDBY, null);
        body.put(Constant.FieldName.LASTUPDATEDON, null);
        IndexResponse indexResponse = client.prepareIndex(Constant.SYSTEM, Constant.FieldName.USERTYPENAME).
                //默认设置_id为userId
                setId(body.get(Constant.FieldName.USERID).toString()).
                setSource(body).execute().actionGet();
        builder.startObject()
                .field("_index", indexResponse.getIndex())
                .field("_type", indexResponse.getType())
                .field("_id", indexResponse.getId())
                .field("_version", indexResponse.getVersion())
                .field("created", indexResponse.isCreated())
                .endObject();

        //user创建成功后加入到everyone组
        if (indexResponse.isCreated()){
            //由于执行的同步问题会导致加不进去，暂时取消
            //synchronizeEveryoneGroup();
        }
        return builder;
    }

    public Json get(String id, boolean getAll) {
//        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.USERTYPENAME, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        if (getResponse.isExists()){
            Json json = new Json();
            json.put(Constant.FieldName._ID, id);
            json.put(Constant.FieldName.USERID, source.get(Constant.FieldName.USERID));
            json.put(Constant.FieldName.USERNAME, source.get(Constant.FieldName.USERNAME));
            json.put(Constant.FieldName.EMAIL, source.get(Constant.FieldName.EMAIL));
            json.put(Constant.FieldName.PASSWORD, source.get(Constant.FieldName.PASSWORD));
            json.put(Constant.FieldName.CREATEDBY, source.get(Constant.FieldName.CREATEDBY));
            json.put(Constant.FieldName.CREATEDON, source.get(Constant.FieldName.CREATEDON));
            json.put(Constant.FieldName.LASTUPDATEDBY, source.get(Constant.FieldName.LASTUPDATEDBY));
            json.put(Constant.FieldName.LASTUPDATEDON, source.get(Constant.FieldName.LASTUPDATEDON));
            List<String> groups = new ArrayList<String>();
            if (getAll){
                QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.USERS, id);
                SearchResponse searchGroupResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
                SearchHits groupHits = searchGroupResponse.getHits();
                if (groupHits.totalHits()>0){
                    for (SearchHit g : groupHits.getHits()) {
                        String groupId  = (String)g.getSource().get(Constant.FieldName.GROUPID);
                        groups.add(groupId);
                    }
                }
            }
            json.put(Constant.FieldName.GROUPS, groups);
            return json;
        }else{
            logger.error(String.format(context.m("UserService.userNotExist"), id));
            throw new uContentException(String.format(context.m("UserService.userNotExist"), id), HttpStatus.NOT_FOUND);
        }
    }



    public XContentBuilder update(String id, Json body) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.USERTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("UserService.userNotExist"), id));
            throw new uContentException(String.format(context.m("UserService.userNotExist"), id), HttpStatus.NOT_FOUND);
        }else{
            if (getResponse.getId().equals(Constant.ADMIN)){
                logger.error(String.format(context.m("UserService.userCannotBeModified"), id));
                throw new uContentException(String.format(context.m("UserService.userCannotBeModified"), id), HttpStatus.BAD_REQUEST);
            }
        }

        body.remove(Constant.FieldName._ID);
        validateUser(body, "update", id);

        body.remove(Constant.FieldName.CREATEDBY);
        body.remove(Constant.FieldName.CREATEDON);
        body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
        body.put(Constant.FieldName.LASTUPDATEDON, new Date());
        UpdateResponse updateResponse = context.getClient().prepareUpdate(Constant.SYSTEM, Constant.FieldName.USERTYPENAME, id).setDoc(body).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", Constant.SYSTEM)
                .field("_type", Constant.FieldName.USERTYPENAME)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }

    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.USERTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("UserService.userNotExist"), id));
            throw new uContentException(String.format(context.m("UserService.userNotExist"), id), HttpStatus.NOT_FOUND);
        }else{
            if (getResponse.getId().equals(Constant.ADMIN)){
                logger.error(String.format(context.m("UserService.userCannotBeDeleted"), id));
                throw new uContentException(String.format(context.m("UserService.userCannotBeDeleted"), id), HttpStatus.BAD_REQUEST);
            }
        }
        DeleteResponse deleteResponse = client.prepareDelete(Constant.SYSTEM, Constant.FieldName.USERTYPENAME, id).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", Constant.SYSTEM)
                .field("_type", Constant.FieldName.USERTYPENAME)
                .field("_id", id)
                .field("_version", deleteResponse.getVersion())
                .field("found", deleteResponse.isFound())
                .endObject();

        //同步everyone组数据
        if (deleteResponse.isFound()){
            //由于执行的同步问题会导致加不进去，暂时取消
            //synchronizeEveryoneGroup();

            //删除组中的此用户
            QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.USERS, id);
            SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
            for (SearchHit searchHitFields : searchResponse.getHits()) {
                String groupId = searchHitFields.getId();
                Object users = searchHitFields.getSource().get(Constant.FieldName.USERS);
                if (users instanceof ArrayList){
                    ((ArrayList) users).remove(id);
                }
                Json group = new Json();
                group.put(Constant.FieldName.GROUPID, groupId);
                group.put(Constant.FieldName.USERS, users);
                groupService.update(groupId, group);
            }
        }
        return builder;
    }

    public XContentBuilder getGroups(String id) throws IOException {
        Client client = context.getClient();
        QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.USERS, id);
        SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject().field("total", searchResponse.getHits().totalHits());
        builder.startArray("groups");
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            builder.startObject()
                    .field(Constant.FieldName._ID, searchHitFields.getId())
                    .field(Constant.FieldName.GROUPID, searchHitFields.getSource().get(Constant.FieldName.GROUPID))
                    .field(Constant.FieldName.GROUPNAME, searchHitFields.getSource().get(Constant.FieldName.GROUPNAME))
                    .field(Constant.FieldName.USERS, searchHitFields.getSource().get(Constant.FieldName.USERS))
                    .field(Constant.FieldName.CREATEDBY, searchHitFields.getSource().get(Constant.FieldName.CREATEDBY))
                    .field(Constant.FieldName.CREATEDON, searchHitFields.getSource().get(Constant.FieldName.CREATEDON))
                    .field(Constant.FieldName.LASTUPDATEDBY, searchHitFields.getSource().get(Constant.FieldName.LASTUPDATEDBY))
                    .field(Constant.FieldName.LASTUPDATEDON, searchHitFields.getSource().get(Constant.FieldName.LASTUPDATEDON))
                    .endObject();
        }
        builder.endArray();
        return builder;
    }

    public List<String> getGroupsOfUser(String id) throws IOException {
//        Client client = context.getClient();
        QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.USERS, id);
        SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
        Set<String> groups = new HashSet<String>();
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            String groupId = (String)searchHitFields.getSource().get(Constant.FieldName.GROUPID);
            groups.add(groupId);
            QueryBuilder groupQb = QueryBuilders.termQuery(Constant.FieldName.CHILDGROUPS, groupId);
            SearchResponse groupSr = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(groupQb).execute().actionGet();
            SearchHits groupSh = groupSr.getHits();
            if (groupSh.totalHits()>0){
                for (SearchHit shf : groupSh.getHits()) {
                    String pGroup  = (String)shf.getSource().get(Constant.FieldName.GROUPID);
                    getParentGroup(groups, pGroup);
                }
            }
        }
        groups.add(Constant.EVERYONE);
        return new ArrayList<String>(groups);
    }

    private void getParentGroup(Set<String> returnGroups, String parentGroup){
        if (parentGroup!=null){
            returnGroups.add(parentGroup);
            Client client = context.getClient();
            QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.CHILDGROUPS, parentGroup);
            SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (searchHits.totalHits()>0){
                for (SearchHit searchHitFields : searchResponse.getHits()) {
                    String pGroup  = (String)searchHitFields.getSource().get(Constant.FieldName.GROUPID);
                    getParentGroup(returnGroups, pGroup);
                }
            }
        }
    }

    public void initialUserData() throws IOException {
        Client client = context.getClient();

        //创建user Mapping
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(Constant.SYSTEM).addTypes(Constant.FieldName.USERTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if(mappings.size()==0){
            //冇得，那就搞一个吧。。。
            XContentBuilder builder= XContentFactory.jsonBuilder();
            builder.startObject();
            builder.startObject(Constant.FieldName.USERTYPENAME);
            builder.startObject("properties")
                    .startObject(Constant.FieldName.USERID).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.USERNAME).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.EMAIL).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.PASSWORD).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.CREATEDBY).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.CREATEDON).field("type", "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.LASTUPDATEDBY).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.LASTUPDATEDON).field("type", "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();
            builder.endObject();//end of typeName
            builder.endObject();
            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(Constant.SYSTEM).type(Constant.FieldName.USERTYPENAME).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
        }else{
            //艹，居然有！！！！！
        }

        //创建ADMIN
        if (!client.prepareGet(Constant.SYSTEM, Constant.FieldName.USERTYPENAME, Constant.ADMIN).execute().actionGet().isExists()) {
            Map<String, Object> adminGroup = new HashMap<String, Object>();
            adminGroup.put(Constant.FieldName.USERID, Constant.ADMIN);
            adminGroup.put(Constant.FieldName.USERNAME, Constant.ADMIN);
            adminGroup.put(Constant.FieldName.EMAIL, "");
            adminGroup.put(Constant.FieldName.PASSWORD, Constant.DEFAULTPASSWORD);
            adminGroup.put(Constant.FieldName.CREATEDBY, Constant.ADMIN);
            adminGroup.put(Constant.FieldName.CREATEDON, new Date());
            adminGroup.put(Constant.FieldName.LASTUPDATEDBY, null);
            adminGroup.put(Constant.FieldName.LASTUPDATEDON, null);

            IndexResponse adminGroupResponse = client.prepareIndex(Constant.SYSTEM, Constant.FieldName.USERTYPENAME
            ).setId(Constant.ADMIN).setSource(adminGroup).execute().actionGet();
        }else{

        }
    }

    private void validateUser(Json body, String action, String id) {
        //校验userId
        Object userId = body.get(Constant.FieldName.USERID);
        Object userName = body.get(Constant.FieldName.USERNAME);
        if (action.equals("create")){
            if (StringUtils.isEmpty(userId)||StringUtils.isEmpty(userName)){
                logger.error(context.m("UserService.cannotBeBlank"));
                throw new uContentException(context.m("UserService.cannotBeBlank"), HttpStatus.BAD_REQUEST);
            }
        }else if(action.equals("update")){
            if ("".equals(userId)||"".equals(userName)){
                logger.error(context.m("UserService.cannotBeBlank"));
                throw new uContentException(context.m("UserService.cannotBeBlank"), HttpStatus.BAD_REQUEST);
            }
        }

        Client client = context.getClient();
        //校验userId
        if (!StringUtils.isEmpty(userId)){
            if (action.equals("create")){
                QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.USERID, userId);
                SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).
                        setSearchType(SearchType.QUERY_AND_FETCH).
                        setTypes(Constant.FieldName.USERTYPENAME).
                        setQuery(queryBuilder).setExplain(true).
                        execute().actionGet();

                if (searchResponse.getHits().totalHits()>0){
                    logger.error(String.format(context.m("UserService.userIdExist"), userId));
                    throw new uContentException(String.format(context.m("UserService.userIdExist"), userId), HttpStatus.BAD_REQUEST);
                }
            }else if(action.equals("update")){
                //修改时userId不可被修改
                GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.USERTYPENAME, id).execute().actionGet();
                Map<String, Object> source = getResponse.getSource();
                if(!userId.equals(source.get(Constant.FieldName.USERID))){
                    logger.error(String.format(context.m("UserService.userIdCannotBeModified"), userId));
                    throw new uContentException(String.format(context.m("UserService.userIdCannotBeModified"), userId), HttpStatus.BAD_REQUEST);
                }
            }
        }


        //校验userName是否存在
        if (!StringUtils.isEmpty(userName)){
            QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.USERNAME, userName);
            SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.USERTYPENAME).setQuery(queryBuilder).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (action.equals("create")){
                if (searchHits.totalHits()>0){
                    logger.error(String.format(context.m("UserService.userNameExist"), userName));
                    throw new uContentException(String.format(context.m("UserService.userNameExist"), userName), HttpStatus.BAD_REQUEST);
                }
            }else if(action.equals("update")){
                if (searchHits.totalHits()>0){
                    //修改时发现与其他user的userName相同
                    for (SearchHit searchHitFields : searchHits) {
                        if(!id.equals(searchHitFields.getId())){
                            logger.error(String.format(context.m("UserService.userNameExist"), userName));
                            throw new uContentException(String.format(context.m("UserService.userNameExist"), userName), HttpStatus.BAD_REQUEST);
                        }
                    }
                }
            }
        }

        //校验是否有多余的属性
        Iterator<Map.Entry<String, Object>> iterator = body.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            if(!(key.equals(Constant.FieldName.USERID)||key.equals(Constant.FieldName.USERNAME)||
                    key.equals(Constant.FieldName.EMAIL)||key.equals(Constant.FieldName.PASSWORD)||
                    key.equals(Constant.FieldName.CREATEDBY)||key.equals(Constant.FieldName.CREATEDON)||
                    key.equals(Constant.FieldName.LASTUPDATEDBY)||key.equals(Constant.FieldName.LASTUPDATEDON)
            )){
                logger.error(String.format(context.m("UserService.illegalProperty"), key));
                throw new uContentException(String.format(context.m("UserService.illegalProperty"), key), HttpStatus.BAD_REQUEST);
            }
        }

    }

    private void synchronizeEveryoneGroup(){
        Client client = context.getClient();
        //首先获取所有user
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.USERTYPENAME);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        List<String> users = new ArrayList<String>();
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            users.add(searchHitFields.getSource().get(Constant.FieldName.USERID).toString());
        }

        //更新Everyon
        Map<String, Object> source = new HashMap<String, Object>();
        source.put(Constant.FieldName.USERS, users);
        source.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
        source.put(Constant.FieldName.LASTUPDATEDON, new Date());
        UpdateResponse updateResponse = client.prepareUpdate(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, Constant.EVERYONE)
                .setDoc(source).execute().actionGet();
    }

}
