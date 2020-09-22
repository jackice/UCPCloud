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
import org.elasticsearch.index.query.BoolQueryBuilder;
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
public class GroupService {

    @Autowired
    private RequestContext context;

    private Logger logger = Logger.getLogger(GroupService.class);

    public XContentBuilder all(String query, int start, int limit, SortBuilder[] sort) throws IOException {
        Client client = context.getClient();
        SearchResponse searchResponse = null;
        if (limit>0){
            SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(Constant.SYSTEM)
                    .setTypes(Constant.FieldName.GROUPTYPENAME).setFrom(start).setSize(limit);
            if (query != null && !query.isEmpty()) {
                searchRequestBuilder.setQuery(query);
            }
            if (sort != null && sort.length > 0) {
                for(SortBuilder sortBuilder : sort){
                    searchRequestBuilder.addSort(sortBuilder);
                }
            }
            searchResponse = searchRequestBuilder.execute().actionGet();
        }else{
            searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(query).execute().actionGet();
        }

        SearchHits hits = searchResponse.getHits();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject().field("total", searchResponse.getHits().totalHits());
        builder.startArray("groups");
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            builder.startObject()
                    .field(Constant.FieldName._ID, searchHitFields.getId())
                    .field(Constant.FieldName.GROUPID, searchHitFields.getSource().get(Constant.FieldName.GROUPID))
                    .field(Constant.FieldName.GROUPNAME, searchHitFields.getSource().get(Constant.FieldName.GROUPNAME))
                    .field(Constant.FieldName.USERS, searchHitFields.getSource().get(Constant.FieldName.USERS))
                    .field(Constant.FieldName.CHILDGROUPS, searchHitFields.getSource().get(Constant.FieldName.CHILDGROUPS))
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
        validateGroup(body, "create", "", new ArrayList<String>());


        body.put(Constant.FieldName.USERS, removeDuplicate((removeDuplicate((List<Object>)body.get(Constant.FieldName.USERS)))));
        body.put(Constant.FieldName.CHILDGROUPS, removeDuplicate((removeDuplicate((List<Object>)body.get(Constant.FieldName.CHILDGROUPS)))));

        body.put(Constant.FieldName.CREATEDBY, context.getUserName());
        body.put(Constant.FieldName.CREATEDON, new Date());
        body.put(Constant.FieldName.LASTUPDATEDBY, null);
        body.put(Constant.FieldName.LASTUPDATEDON, null);
        IndexResponse indexResponse = client.prepareIndex(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME).
                setId(body.get(Constant.FieldName.GROUPID).toString()).setSource(body).execute().actionGet();
        builder.startObject()
                .field("_index", indexResponse.getIndex())
                .field("_type", indexResponse.getType())
                .field("_id", indexResponse.getId())
                .field("_version", indexResponse.getVersion())
                .field("created", indexResponse.isCreated())
                .endObject();
        return builder;
    }

    //检查此组是否已经拥有直接的父组
    private void checkHavingParentGroup(String childGroupId, String groupId){
        Client client = context.getClient();
        QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.CHILDGROUPS, childGroupId);
        SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
        SearchHits searchHits = searchResponse.getHits();
        if (searchHits.totalHits()>0){
            logger.error(String.format(context.m("GroupService.hasParentGroup"), childGroupId, groupId));
            throw new uContentException(String.format(context.m("GroupService.hasParentGroup"), childGroupId, groupId), HttpStatus.BAD_REQUEST);
        }
    }

    //检查是否存在死循环的情况
    private void checkGroupCycling(String childGroupId, String groupId){
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, childGroupId).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        if (getResponse.isExists()){
            Object childGroups = source.get(Constant.FieldName.CHILDGROUPS);
            if (childGroups!=null){
                if (childGroups instanceof List){
                    for(Object subGId:(ArrayList<Object>)childGroups){
                        if (subGId.equals(groupId)){
                            logger.error(String.format(context.m("GroupService.checkGroupCycling"), childGroupId, groupId));
                            throw new uContentException(String.format(context.m("GroupService.checkGroupCycling"), childGroupId, groupId), HttpStatus.BAD_REQUEST);
                        }else {
                            checkGroupCycling((String)subGId, groupId);
                        }
                    }
                }
            }
        }
    }

    public Json get(String id){
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        if (getResponse.isExists()){
            Json json = new Json();
            json.put(Constant.FieldName._ID, id);
            json.put(Constant.FieldName.GROUPID, source.get(Constant.FieldName.GROUPID));
            json.put(Constant.FieldName.GROUPNAME, source.get(Constant.FieldName.GROUPNAME));
            json.put(Constant.FieldName.USERS, source.get(Constant.FieldName.USERS));
            json.put(Constant.FieldName.CHILDGROUPS, source.get(Constant.FieldName.CHILDGROUPS));
            json.put(Constant.FieldName.CREATEDBY, source.get(Constant.FieldName.CREATEDBY));
            json.put(Constant.FieldName.CREATEDON, source.get(Constant.FieldName.CREATEDON));
            json.put(Constant.FieldName.LASTUPDATEDBY, source.get(Constant.FieldName.LASTUPDATEDBY));
            json.put(Constant.FieldName.LASTUPDATEDON, source.get(Constant.FieldName.LASTUPDATEDON));
            return json;
        }else{
            logger.error(String.format(context.m("GroupService.groupNotExist"), id));
            throw new uContentException(String.format(context.m("GroupService.groupNotExist"), id), HttpStatus.NOT_FOUND);
        }
    }

    public XContentBuilder getUsers(String id) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        builder.startObject();
        builder.startArray(Constant.FieldName.USERS);
        ArrayList<Object> users = (ArrayList<Object>)source.get(Constant.FieldName.USERS);
        if (users!=null){
            for (Object user:users){
                GetResponse getUserResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.USERTYPENAME, user.toString()).execute().actionGet();
                if (getUserResponse.isExists()){
                    Map<String, Object> userSource = getUserResponse.getSource();
                    builder.startObject()
                            .field("_id", id)
                            .field(Constant.FieldName.USERID, userSource.get(Constant.FieldName.USERID))
                            .field(Constant.FieldName.USERNAME, userSource.get(Constant.FieldName.USERNAME))
                            .field(Constant.FieldName.EMAIL, userSource.get(Constant.FieldName.EMAIL))
                            .field(Constant.FieldName.PASSWORD, userSource.get(Constant.FieldName.PASSWORD))
                            .field(Constant.FieldName.CREATEDBY, userSource.get(Constant.FieldName.CREATEDBY))
                            .field(Constant.FieldName.CREATEDON, userSource.get(Constant.FieldName.CREATEDON))
                            .field(Constant.FieldName.LASTUPDATEDBY, userSource.get(Constant.FieldName.LASTUPDATEDBY))
                            .field(Constant.FieldName.LASTUPDATEDON, userSource.get(Constant.FieldName.LASTUPDATEDON))
                            .endObject();
                }
            }
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

    public XContentBuilder update(String id, Json body) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        ArrayList<String> groups =  null;
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("GroupService.groupNotExist"), id));
            throw new uContentException(String.format(context.m("GroupService.groupNotExist"), id), HttpStatus.NOT_FOUND);
        }else{
            groups =  (ArrayList<String>)getResponse.getSource().get(Constant.FieldName.CHILDGROUPS);
            if (getResponse.getId().equals(Constant.EVERYONE)){
                logger.error(String.format(context.m("GroupService.groupCannotBeModified"), id));
                throw new uContentException(String.format(context.m("GroupService.groupCannotBeModified"), id), HttpStatus.BAD_REQUEST);
            }
        }

        body.remove(Constant.FieldName._ID);
        validateGroup(body, "update", id, groups);

        body.put(Constant.FieldName.USERS, removeDuplicate((removeDuplicate((List<Object>)body.get(Constant.FieldName.USERS)))));
        body.put(Constant.FieldName.CHILDGROUPS, removeDuplicate((removeDuplicate((List<Object>)body.get(Constant.FieldName.CHILDGROUPS)))));

        body.remove(Constant.FieldName.CREATEDBY);
        body.remove(Constant.FieldName.CREATEDON);
        body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
        body.put(Constant.FieldName.LASTUPDATEDON, new Date());
        UpdateResponse updateResponse = context.getClient().prepareUpdate(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, id).setDoc(body).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", Constant.SYSTEM)
                .field("_type", Constant.FieldName.GROUPTYPENAME)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }

    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("GroupService.groupNotExist"), id));
            throw new uContentException(String.format(context.m("GroupService.groupNotExist"), id), HttpStatus.NOT_FOUND);
        }else{
            if (getResponse.getId().equals(Constant.ADMINGROUP)||getResponse.getId().equals(Constant.EVERYONE)){
                logger.error(String.format(context.m("GroupService.groupCannotBeDeleted"), id));
                throw new uContentException(String.format(context.m("GroupService.groupCannotBeDeleted"), id), HttpStatus.BAD_REQUEST);
            }
        }

        DeleteResponse deleteResponse = client.prepareDelete(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", Constant.SYSTEM)
                .field("_type", Constant.FieldName.GROUPTYPENAME)
                .field("_id", id)
                .field("_version", deleteResponse.getVersion())
                .field("found", deleteResponse.isFound())
                .endObject();
        if (deleteResponse.isFound()){
            //删除其父组的中的该组数据
            QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.CHILDGROUPS, id);
            SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
            for (SearchHit searchHitFields : searchResponse.getHits()) {
                String groupId = searchHitFields.getId();
                Object childGroups = searchHitFields.getSource().get(Constant.FieldName.CHILDGROUPS);
                if (childGroups instanceof ArrayList){
                    ((ArrayList) childGroups).remove(id);
                }
                Json group = new Json();
                group.put(Constant.FieldName.GROUPID, groupId);
                group.put(Constant.FieldName.CHILDGROUPS, childGroups);
                update(groupId, group);
            }
        }
        return builder;
    }

    public XContentBuilder refUsers(String id, Json userIds) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("GroupService.groupNotExist"), id));
            throw new uContentException(String.format(context.m("GroupService.groupNotExist"), id), HttpStatus.NOT_FOUND);
        }

        //传入的users必须为数组
        Object users = userIds.get(Constant.FieldName.USERS);
        if (users!=null){
            if (users instanceof List){
                checkUsersOrGroups((ArrayList<String>)users, "user");
            }else{
                logger.error(String.format(context.m("GroupService.usersShouldBeList"), id));
                throw new uContentException(String.format(context.m("GroupService.usersShouldBeList"), id), HttpStatus.BAD_REQUEST);
            }
        }

        Map<String, Object> source = new HashMap<String, Object>();
        source.put(Constant.FieldName.USERS, removeDuplicate((removeDuplicate((List<Object>)users))));
        source.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
        source.put(Constant.FieldName.LASTUPDATEDON, new Date());
        UpdateResponse updateResponse = context.getClient().prepareUpdate(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, id).setDoc(source).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", Constant.SYSTEM)
                .field("_type", Constant.FieldName.GROUPTYPENAME)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }

    public boolean checkUserInAdminGroup(){
        Client client = context.getClient();
        QueryBuilder qbUser = QueryBuilders.termQuery(Constant.FieldName.USERS, context.getUserName());
        QueryBuilder qbGroup = QueryBuilders.termQuery(Constant.FieldName.GROUPID, Constant.ADMINGROUP);
        BoolQueryBuilder must = QueryBuilders.boolQuery().must(qbUser).must(qbGroup);
        SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(must).execute().actionGet();
        return searchResponse.getHits().totalHits()>0;
    }

    public void initialGroupData() throws IOException {
        Client client = context.getClient();

        //创建group Mapping
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(Constant.SYSTEM).addTypes(Constant.FieldName.GROUPTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if(mappings.size()==0){
            //冇得，那就搞一个吧。。。
            XContentBuilder builder= XContentFactory.jsonBuilder();
            builder.startObject();
            builder.startObject(Constant.FieldName.GROUPTYPENAME);
            builder.startObject("properties")
                    .startObject(Constant.FieldName.GROUPID).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.GROUPNAME).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.USERS).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.CHILDGROUPS).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.CREATEDBY).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.CREATEDON).field("type", "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.LASTUPDATEDBY).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.LASTUPDATEDON).field("type", "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();
            builder.endObject();//end of typeName
            builder.endObject();
            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(Constant.SYSTEM).type(Constant.FieldName.GROUPTYPENAME).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
        }else{
            //艹，居然有！！！！！
        }


        //创建ADMINGROUP
        if (!client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, Constant.ADMINGROUP).execute().actionGet().isExists()) {
            Map<String, Object> adminGroup = new HashMap<String, Object>();
            adminGroup.put(Constant.FieldName.GROUPID, Constant.ADMINGROUP);
            adminGroup.put(Constant.FieldName.GROUPNAME, Constant.ADMINGROUP);
            List<String> users = new ArrayList<String>();
            users.add(Constant.ADMIN);
            adminGroup.put(Constant.FieldName.USERS, users);
            adminGroup.put(Constant.FieldName.CHILDGROUPS, new ArrayList<String>());
            adminGroup.put(Constant.FieldName.CREATEDBY, Constant.ADMIN);
            adminGroup.put(Constant.FieldName.CREATEDON, new Date());
            adminGroup.put(Constant.FieldName.LASTUPDATEDBY, null);
            adminGroup.put(Constant.FieldName.LASTUPDATEDON, null);

            IndexResponse adminGroupResponse = client.prepareIndex(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME
            ).setId(Constant.ADMINGROUP).setSource(adminGroup).execute().actionGet();
        }else{

        }

        //创建EVERYONE
        if (!client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, Constant.EVERYONE).execute().actionGet().isExists()) {
            Map<String, Object> everyone = new HashMap<String, Object>();
            everyone.put(Constant.FieldName.GROUPID, Constant.EVERYONE);
            everyone.put(Constant.FieldName.GROUPNAME, Constant.EVERYONE);
            everyone.put(Constant.FieldName.USERS, new ArrayList<String>());
            everyone.put(Constant.FieldName.CHILDGROUPS, new ArrayList<String>());
            everyone.put(Constant.FieldName.CREATEDBY, Constant.ADMIN);
            everyone.put(Constant.FieldName.CREATEDON, new Date());
            everyone.put(Constant.FieldName.LASTUPDATEDBY, null);
            everyone.put(Constant.FieldName.LASTUPDATEDON, null);

            IndexResponse everyOneResponse = client.prepareIndex(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME
            ).setId(Constant.EVERYONE).setSource(everyone).execute().actionGet();
        }else{

        }
    }

    private void validateGroup(Json body, String action, String id, ArrayList<String> existGroups) {
        //校验groupId groupName
        Object groupId = body.get(Constant.FieldName.GROUPID);
        Object groupName = body.get(Constant.FieldName.GROUPNAME);
        if (action.equals("create")){
            if (StringUtils.isEmpty(groupId)||StringUtils.isEmpty(groupName)){
                logger.error(context.m("GroupService.cannotBeBlank"));
                throw new uContentException(context.m("GroupService.cannotBeBlank"), HttpStatus.BAD_REQUEST);
            }
        }else if(action.equals("update")){
            if ("".equals(groupId)||"".equals(groupName)){
                logger.error(context.m("GroupService.cannotBeBlank"));
                throw new uContentException(context.m("GroupService.cannotBeBlank"), HttpStatus.BAD_REQUEST);
            }
        }

        Client client = context.getClient();
        //校验groupId是否存在
        if (!StringUtils.isEmpty(groupId)){
            QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.GROUPID, groupId);
            SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (action.equals("create")){
                if (searchHits.totalHits()>0){
                    logger.error(String.format(context.m("GroupService.groupIdExist"), groupId));
                    throw new uContentException(String.format(context.m("GroupService.groupIdExist"), groupId), HttpStatus.BAD_REQUEST);
                }
            }else if(action.equals("update")){
                //修改时groupId不可被修改
                GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
                Map<String, Object> source = getResponse.getSource();
                if(!groupId.equals(source.get(Constant.FieldName.GROUPID))){
                    logger.error(String.format(context.m("GroupService.groupIdCannotBeModified"), groupId));
                    throw new uContentException(String.format(context.m("GroupService.groupIdCannotBeModified"), groupId), HttpStatus.BAD_REQUEST);
                }
            }
        }

        //校验groupName是否存在
        if (!StringUtils.isEmpty(groupName)){
            QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.FieldName.GROUPNAME, groupName);
            SearchResponse searchResponse = client.prepareSearch(Constant.SYSTEM).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (action.equals("create")){
                if (searchHits.totalHits()>0){
                    logger.error(String.format(context.m("GroupService.groupNameExist"), groupName));
                    throw new uContentException(String.format(context.m("GroupService.groupNameExist"), groupName), HttpStatus.BAD_REQUEST);
                }
            }else if(action.equals("update")){
                if (searchHits.totalHits()>0){
                    //修改时发现与其他group的groupname相同
                    for (SearchHit searchHitFields : searchHits) {
                        if(!id.equals(searchHitFields.getId())){
                            logger.error(String.format(context.m("GroupService.groupNameExist"), groupName));
                            throw new uContentException(String.format(context.m("GroupService.groupNameExist"), groupName), HttpStatus.BAD_REQUEST);
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
            if(!(key.equals(Constant.FieldName.GROUPID)||key.equals(Constant.FieldName.GROUPNAME)||key.equals(Constant.FieldName.USERS)||
                    key.equals(Constant.FieldName.CHILDGROUPS)||
                    key.equals(Constant.FieldName.CREATEDBY)||key.equals(Constant.FieldName.CREATEDON)||
                    key.equals(Constant.FieldName.LASTUPDATEDBY)||key.equals(Constant.FieldName.LASTUPDATEDON)
            )){
                logger.error(String.format(context.m("GroupService.illegalProperty"), key));
                throw new uContentException(String.format(context.m("GroupService.illegalProperty"), key), HttpStatus.BAD_REQUEST);
            }
        }

        //校验users
        Object users = body.get(Constant.FieldName.USERS);
        if (users!=null){
            if (users instanceof List){
                checkUsersOrGroups((ArrayList<String>)users, "user");
                if ((groupId.equals(Constant.ADMINGROUP)) && (!((ArrayList<String>) users).contains(Constant.ADMIN))){
                    logger.error(context.m("GroupService.cannotRemoveAdmin"));
                    throw new uContentException(context.m("GroupService.cannotRemoveAdmin"), HttpStatus.BAD_REQUEST);
                }
            }else{
                logger.error(String.format(context.m("GroupService.usersShouldBeList"), id));
                throw new uContentException(String.format(context.m("GroupService.usersShouldBeList"), id), HttpStatus.BAD_REQUEST);
            }
        }

        //校验childGroups
        Object childGroups = body.get(Constant.FieldName.CHILDGROUPS);
        if (childGroups!=null){
            if (childGroups instanceof List){
                ArrayList<String> cgroups =  (ArrayList<String>)childGroups;

                //检查数据是否存在
                checkUsersOrGroups(cgroups, "group");

                //将新增的childgroup放入集合中进行校验
                ArrayList<String> newGroups = new ArrayList<String>();
                if(action.equals("update")){
                    for (String g:cgroups){
                        if((!existGroups.contains(g))&&(!newGroups.contains(g))){
                            newGroups.add(g);
                        }
                    }
                }

                for(String childGroupId:newGroups){
                    if (childGroupId.equals((String)groupId)){
                        logger.error(String.format(context.m("GroupService.groupCannotBeAddedToItself"), groupId));
                        throw new uContentException(String.format(context.m("GroupService.groupCannotBeAddedToItself"), groupId), HttpStatus.BAD_REQUEST);
                    }

                    //检查此组织是否已经拥有直接的父组织，如果有，则抛出异常，保证一个组织永远只有一个直接父组织
                    checkHavingParentGroup(childGroupId, (String) groupId);

                    //检查是否存在死循环的情况，即将父组加入到其子组中
                    checkGroupCycling(childGroupId, (String)groupId);
                }
            }else{
                logger.error(String.format(context.m("GroupService.childGroupsShouldBeList"), id));
                throw new uContentException(String.format(context.m("GroupService.childGroupsShouldBeList"), id), HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void checkGroup(String id){
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("GroupService.groupNotExist"), id));
            throw new uContentException(String.format(context.m("GroupService.groupNotExist"), id), HttpStatus.BAD_REQUEST);
        }
    }

    private void checkUser(String id){
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(Constant.SYSTEM, Constant.FieldName.USERTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.error(String.format(context.m("UserService.userNotExist"), id));
            throw new uContentException(String.format(context.m("UserService.userNotExist"), id), HttpStatus.BAD_REQUEST);
        }
    }

    private void checkUsersOrGroups(ArrayList<String> ids, String action){
        if (ids!=null&&ids.size()>0){
            for(String id:ids){
                if(action.equals("user")){
                    checkUser(id);
                }else if(action.equals("group")){
                    checkGroup(id);
                }else{

                }
            }
        }
    }

    private List<Object> removeDuplicate(List<Object> objs){
        ArrayList<Object> list = new ArrayList<Object>();
        if ((objs!=null)&&(objs.size()>0)){
            for(Object obj:objs){
                if (!list.contains(obj)){
                    list.add(obj);
                }
            }
        }
        return list;
    }

}

