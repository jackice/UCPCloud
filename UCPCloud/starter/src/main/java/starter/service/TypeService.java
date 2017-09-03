package starter.service;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import starter.EsConfig;
import starter.RequestContext;
import starter.rest.Json;
import starter.uContentException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class TypeService {

    @Autowired
    private RequestContext context;

    @Autowired
    private EsConfig esConfig;


    private Logger logger = Logger.getLogger(TypeService.class);

    public List<Json> getAll(boolean containProperties) throws IOException {

        List<Json> cts = new ArrayList<Json>();
        Client client = context.getClient();

        GetIndexResponse getIndexResponse = client.admin().indices().prepareGetIndex().get();
        String[] indices = getIndexResponse.indices();
        if(indices!=null&&indices.length>0){
            for(String index:indices){
                if (!index.equals(Constant.SYSTEM)){
                    GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(index).get();
                    ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
                    ImmutableOpenMap<String, MappingMetaData> types = mappings.get(index);
                    for (ObjectObjectCursor<String, MappingMetaData> type : types) {
                        String typeName = type.key;
                        MappingMetaData mappingMetaData = types.get(typeName);
                        if (mappingMetaData!=null){
                            String source = mappingMetaData.source().string();
                            Json parse = Json.parse(source);
                            LinkedHashMap<String, Object> typeObject = (LinkedHashMap<String, Object>)parse.get(typeName);
                            if (typeObject!=null){
                                LinkedHashMap<String, Object> typeInfo = (LinkedHashMap<String, Object>)typeObject.get("_meta");
                                if (typeInfo!=null){

                                    boolean isDocType = typeInfo.get(Constant.FieldName.ISDOCTYPE)==null?false:(boolean)typeInfo.get(Constant.FieldName.ISDOCTYPE);
                                    boolean isDeleted = typeInfo.get(Constant.FieldName.ISDELETED)==null?true:(boolean)typeInfo.get(Constant.FieldName.ISDELETED);
                                    String displayName = typeInfo.get(Constant.FieldName.DISPLAYNAME)==null?"":String.valueOf(typeInfo.get(Constant.FieldName.DISPLAYNAME));
                                    String description = typeInfo.get(Constant.FieldName.DESCRIPTION)==null?"":String.valueOf(typeInfo.get(Constant.FieldName.DESCRIPTION));

                                    if (isDocType&&(!isDeleted)){
                                        Json ct = new Json();
                                        //ct.put(Constant.FieldName.ID, typeName);
                                        ct.put(Constant.FieldName.NAME, typeName);
                                        ct.put(Constant.FieldName.DISPLAYNAME, displayName);
                                        ct.put(Constant.FieldName.DESCRIPTION, description);
                                        if (containProperties) {
                                            ArrayList<Object> properties = (ArrayList<Object>)typeInfo.get(Constant.FieldName.PROPERTIES);
                                            if ((properties==null)||properties.size()==0){

                                            }else{
                                                List<Json>  pros = new ArrayList<Json>();
                                                for(Object property:properties){
                                                    if (property!=null){
                                                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                                                        Json p = new Json();
                                                        p.put(Constant.FieldName.NAME, pro.get(Constant.FieldName.NAME)==null?"":pro.get(Constant.FieldName.NAME).toString());
                                                        p.put(Constant.FieldName.TYPE, pro.get(Constant.FieldName.TYPE)==null?"":pro.get(Constant.FieldName.TYPE).toString());
                                                        p.put(Constant.FieldName.INDEX, pro.get(Constant.FieldName.INDEX)==null?"":pro.get(Constant.FieldName.INDEX).toString());
                                                        p.put(Constant.FieldName.ANALYZER, pro.get(Constant.FieldName.ANALYZER)==null?"":pro.get(Constant.FieldName.ANALYZER).toString());
                                                        p.put(Constant.FieldName.REQUIRED, pro.get(Constant.FieldName.REQUIRED)==null?false:Boolean.valueOf(pro.get(Constant.FieldName.REQUIRED).toString()));
                                                        p.put(Constant.FieldName.DEFAULTVALUE, pro.get(Constant.FieldName.DEFAULTVALUE)==null?"":pro.get(Constant.FieldName.DEFAULTVALUE).toString());
                                                        p.put(Constant.FieldName.PATTERN, pro.get(Constant.FieldName.PATTERN)==null?"":pro.get(Constant.FieldName.PATTERN).toString());
                                                        p.put(Constant.FieldName.PROMPTMESSAGE, pro.get(Constant.FieldName.PROMPTMESSAGE)==null?"":pro.get(Constant.FieldName.PROMPTMESSAGE).toString());
                                                        p.put(Constant.FieldName.ORDER, pro.get(Constant.FieldName.ORDER)==null?"":pro.get(Constant.FieldName.ORDER).toString());
                                                        pros.add(p);
                                                    }
                                                }
                                                ct.put(Constant.FieldName.PROPERTIES, pros);
                                            }
                                        }
                                        cts.add(ct);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return cts;
    }

    public List<String> getAllTypes() throws IOException {
        List<String> allTypes = new ArrayList<String>();
        List<Json> all = getAll(false);
        for(Json ct:all){
            allTypes.add(ct.get(Constant.FieldName.NAME).toString());
        }
        return allTypes;
    }

    public XContentBuilder create(Json body) throws IOException {
        return createOrUpdateType("", body, "create");
    }

    private XContentBuilder createOrUpdateType(String id, Json body, String action) throws IOException {
        Client client = context.getClient();
        boolean acknowledged = false;

        if(StringUtils.isEmpty(body.get(Constant.FieldName.NAME))){
            logger.error(context.m("TypeService.typeNameCannotBeBlank"));
            throw new uContentException(context.m("TypeService.typeNameCannotBeBlank"), HttpStatus.BAD_REQUEST);
        }
        String name = String.valueOf(body.get(Constant.FieldName.NAME));

        if(StringUtils.isEmpty(body.get(Constant.FieldName.DISPLAYNAME))){
            logger.error(context.m("TypeService.displayNameCannotBeBlank"));
            throw new uContentException(context.m("TypeService.displayNameCannotBeBlank"), HttpStatus.BAD_REQUEST);
        }
        String displayName = String.valueOf(body.get(Constant.FieldName.DISPLAYNAME));

        String description = body.get(Constant.FieldName.DESCRIPTION)==null?"":String.valueOf(body.get(Constant.FieldName.DESCRIPTION));

        //校验name和displayname是否重名
        List<Json> all = getAll(false);
        if (action.equals("create")){
            for(Json ct:all){
                if (String.valueOf(ct.get(Constant.FieldName.NAME)).equals(name)){
                    logger.error(String.format(context.m("TypeService.typeNameExist"), name));
                    throw new uContentException(String.format(context.m("TypeService.typeNameExist"), name), HttpStatus.BAD_REQUEST);
                }

                if (String.valueOf(ct.get(Constant.FieldName.DISPLAYNAME)).equals(displayName)){
                    logger.error(String.format(context.m("TypeService.displayNameExist"), displayName));
                    throw new uContentException(String.format(context.m("TypeService.displayNameExist"), displayName), HttpStatus.BAD_REQUEST);
                }
            }
        }else if(action.equals("update")){
            for(Json ct:all){
                if ((String.valueOf(ct.get(Constant.FieldName.DISPLAYNAME)).equals(displayName))
                        &&(!String.valueOf(ct.get(Constant.FieldName.NAME)).equals(id))){
                    logger.error(String.format(context.m("TypeService.displayNameExist"), displayName));
                    throw new uContentException(String.format(context.m("TypeService.displayNameExist"), displayName), HttpStatus.BAD_REQUEST);
                }
            }
        }


        XContentBuilder builder= XContentFactory.jsonBuilder();
        if(body!=null){
            //获取properties
            ArrayList<Object> properties = (ArrayList<Object>)body.get(Constant.FieldName.PROPERTIES);
            if ((properties==null)||properties.size()==0){
                //Exception
            }

            builder.startObject();
            String label = "create".equals(action) == true ? name : id;
            builder.startObject(label);

            //组装_meta
            builder.startObject("_meta")
                    .field(Constant.FieldName.DISPLAYNAME, displayName)
                    .field(Constant.FieldName.DESCRIPTION, description)
                    .field(Constant.FieldName.ISDELETED, false)
                    .field(Constant.FieldName.ISDOCTYPE, true);
            builder.startArray(Constant.FieldName.PROPERTIES);
            if (properties != null) {
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;

                        String pname = "";
                        if(StringUtils.isEmpty(pro.get(Constant.FieldName.NAME))){
                            logger.error(context.m("TypeService.propertyNameCannotBeBlank"));
                            throw new uContentException(context.m("TypeService.propertyNameCannotBeBlank"), HttpStatus.BAD_REQUEST);
                        }else{
                            pname = pro.get(Constant.FieldName.NAME).toString();
                        }

                        String type = "";
                        if(StringUtils.isEmpty(pro.get(Constant.FieldName.TYPE))){
                            logger.error(context.m("TypeService.propertyTypeCannotBeBlank"));
                            throw new uContentException(context.m("TypeService.propertyTypeCannotBeBlank"), HttpStatus.BAD_REQUEST);
                        }else{
                            type = pro.get(Constant.FieldName.TYPE).toString();
                        }

                        String index = "";
                        if(StringUtils.isEmpty(pro.get(Constant.FieldName.INDEX))){
                            //默认为不分词
                            index = Constant.FieldName.NOT_ANALYZED;
                        }else{
                            index = pro.get(Constant.FieldName.INDEX).toString();
                        }

                        String analyzer = "";
                        if(StringUtils.isEmpty(pro.get(Constant.FieldName.ANALYZER))){

                        }else{
                            analyzer = pro.get(Constant.FieldName.ANALYZER).toString();
                        }

                        Boolean required = false;
                        if(StringUtils.isEmpty(pro.get(Constant.FieldName.REQUIRED))){

                        }else{
                            required = Boolean.valueOf(pro.get(Constant.FieldName.REQUIRED).toString());
                        }

                        String defaultValue = "";
                        if(StringUtils.isEmpty(pro.get(Constant.FieldName.DEFAULTVALUE))){

                        }else{
                            defaultValue = pro.get(Constant.FieldName.DEFAULTVALUE).toString();
                        }

                        String pattern = "";
                        if(StringUtils.isEmpty(pro.get(Constant.FieldName.PATTERN))){

                        }else{
                            pattern = pro.get(Constant.FieldName.PATTERN).toString();
                            try {
                                Pattern.compile(pattern);
                            }catch (Exception e){
                                logger.error(String.format(context.m("TypeService.illegalPattern"), pattern));
                                throw new uContentException(String.format(context.m("TypeService.illegalPattern"), pattern), HttpStatus.BAD_REQUEST);
                            }
                        }

                        String promptMessage = "";
                        if(StringUtils.isEmpty(pro.get(Constant.FieldName.PROMPTMESSAGE))){

                        }else{
                            promptMessage = pro.get(Constant.FieldName.PROMPTMESSAGE).toString();
                        }

                        String order = "";
                        if(StringUtils.isEmpty(pro.get(Constant.FieldName.ORDER))){

                        }else{
                            order = pro.get(Constant.FieldName.ORDER).toString();
                        }

                        builder.startObject()
                                .field(Constant.FieldName.NAME, pname)
                                .field(Constant.FieldName.TYPE, type)
                                .field(Constant.FieldName.INDEX, index)
                                .field(Constant.FieldName.ANALYZER, analyzer)
                                .field(Constant.FieldName.REQUIRED, required)
                                .field(Constant.FieldName.DEFAULTVALUE, defaultValue)
                                .field(Constant.FieldName.PATTERN, pattern)
                                .field(Constant.FieldName.PROMPTMESSAGE, promptMessage)
                                .field(Constant.FieldName.ORDER, order)
                                .endObject();
                    }
                }

            }
            builder.endArray();
            builder.endObject(); //end of _meta

            //组装properties
            builder.startObject(Constant.FieldName.PROPERTIES)
                    .startObject(Constant.FieldName.NAME).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.DESCRIPTION).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();

            //组装stream属性
            builder.startObject(Constant.FieldName.STREAMS).startObject(Constant.FieldName.PROPERTIES)
                    .startObject(Constant.FieldName.STREAMID).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, "no").endObject()
                    .startObject(Constant.FieldName.STREAMNAME).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, "no").endObject()
                    .startObject(Constant.FieldName.LENGTH).field(Constant.FieldName.TYPE, "long").field(Constant.FieldName.INDEX, "no").endObject()
                    .startObject(Constant.FieldName.CONTENTTYPE).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, "no").endObject()
                    .startObject(Constant.FieldName.FULLTEXT).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.ANALYZED)
                    .field(Constant.FieldName.ANALYZER, Constant.DEFAULT_INDEXANALYZER).endObject()
                    .endObject()
                    .endObject();

            //组装_acl属性
            builder.startObject("_acl")
                    .startObject(Constant.FieldName.PROPERTIES)
                    .startObject(Constant.FieldName.READ)
                    .startObject(Constant.FieldName.PROPERTIES)
                    .startObject(Constant.FieldName.USERS).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.GROUPS).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .endObject()
                    .endObject()
                    .startObject(Constant.FieldName.WRITE)
                    .startObject(Constant.FieldName.PROPERTIES)
                    .startObject(Constant.FieldName.USERS).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.GROUPS).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .endObject()
                    .endObject()
                    .endObject()
                    .endObject();


            //组装创建信息属性
            builder.startObject(Constant.FieldName.CREATEDBY).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.CREATEDON).field(Constant.FieldName.TYPE, "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.LASTUPDATEDBY).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.LASTUPDATEDON).field(Constant.FieldName.TYPE, "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();

            //组装Tag属性
            builder.startObject(Constant.FieldName.TAG).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();

            //组装自定义属性
            if (properties != null) {
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        builder.startObject(pro.get(Constant.FieldName.NAME).toString());
                        builder.field(Constant.FieldName.TYPE, pro.get(Constant.FieldName.TYPE).toString());
                        //非bool类型的字段才做是否分词控制
                        if(pro.get(Constant.FieldName.TYPE).toString().toUpperCase().equals("BOOLEAN"))
                        { 
                        	//continue;
                        }
                        if(StringUtils.isEmpty(pro.get(Constant.FieldName.INDEX))){
//                       //默认为不分词(如果为空则默认不分词)
                            builder.field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED);
                        }else{
                            builder.field(Constant.FieldName.INDEX, pro.get(Constant.FieldName.INDEX).toString());
                        }
//                        if(pro.get(Constant.FieldName.TYPE).toString().toUpperCase().equals("BOOLEAN")){
//
//                        }else{
//                            if(StringUtils.isEmpty(pro.get(Constant.FieldName.INDEX))){
//                                //默认为不分词(如果为空则默认不分词)
//                                builder.field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED);
//                            }else{
//                                if (pro.get(Constant.FieldName.INDEX).toString().equals(Constant.FieldName.ANALYZED)){
//                                    if(!StringUtils.isEmpty(pro.get(Constant.FieldName.ANALYZER))){
//                                        builder.field(Constant.FieldName.INDEX, Constant.FieldName.ANALYZED).field(Constant.FieldName.ANALYZER, pro.get(Constant.FieldName.ANALYZER).toString());
//                                    }else{
//                                        builder.field(Constant.FieldName.INDEX, Constant.FieldName.ANALYZED).field(Constant.FieldName.ANALYZER, Constant.DEFAULT_INDEXANALYZER);
//                                    }
//                                }else{
//                                    builder.field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED);
//                                }
//                            }
//                        }
                        builder.endObject();
                    }
                }

            }


            builder.endObject();//end of properties

            builder.endObject();//end of typeName
            builder.endObject();
        }


        //首先创建index
        String index = "";
        if (action.equals("create")){
            index = name.toLowerCase()+"_v0";
            String alia = name;
            IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(index).execute().actionGet();
            if (indicesExistsResponse.isExists()){

            }else {
                //添加分片和副本设置
                Settings settings = Settings.builder().put("number_of_shards", esConfig.getShards()).put("number_of_replicas", esConfig.getReplicas()).build();
                client.admin().indices().prepareCreate(index).addAlias(new Alias(context.getAlias())).addAlias(new Alias(alia)).setSettings(settings).execute().actionGet();
            }
        }else{
            name = id;//更新的时候文档类型唯一标识不允许修改
            GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(id).addTypes(id).get();
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
            if (mappings==null||mappings.size()==0){
                logger.error(String.format(context.m("TypeService.typeNotExist"), id));
                throw new uContentException(String.format(context.m("TypeService.typeNotExist"), id), HttpStatus.NOT_FOUND);
            }

            Iterator<ObjectCursor<String>> iterator = mappings.keys().iterator();
            if(iterator.hasNext()){
                index = iterator.next().value;
            }
        }

        try{
            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(index).type(name).source(builder).updateAllTypes(true);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
            acknowledged = putMappingResponse.isAcknowledged();
        } catch (Exception e) {
//            String metaRxp = "Mapper for" + "([^()]+)" +"conflicts with existing mapping in other types";
//            Pattern pattern = Pattern.compile(metaRxp);
//            Matcher matcher = pattern.matcher(e.getMessage());
//            if ((e instanceof IllegalArgumentException) && (matcher.find())){
//                logger.error(e.getMessage());
//                throw new uContentException(e.getMessage(), HttpStatus.BAD_REQUEST);
//            }else{
//                logger.error(e.getMessage());
//                throw new uContentException("Bad Data", e, HttpStatus.BAD_REQUEST);
//            }
            logger.error(e.getMessage());
            if (action.equals("create")){
                throw new uContentException(context.m("TypeService.createError"), e, HttpStatus.BAD_REQUEST);
            }else{
                throw new uContentException(context.m("TypeService.updateError"), e, HttpStatus.BAD_REQUEST);
            }
        }

        //返回相应
        return XContentFactory.jsonBuilder().startObject().field("acknowledged", acknowledged).field("typeId", name).endObject();
    }

//    public XContentBuilder get(String id) throws IOException {
//        //List<String> allTypes = getAllTypes();
//        Client client = context.getClient();
//        String index = id.toLowerCase()+"_v0";
//        XContentBuilder builder= XContentFactory.jsonBuilder();
//        //GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).addTypes(id).get();
//        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(index).addTypes(id).get();
//        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
//        if (mappings==null||mappings.size()==0){
//            logger.error(String.format("The type: %s does not exist", id));
//            throw new uContentException(String.format("The type: %s does not exist", id), HttpStatus.NOT_FOUND);
//        }
//        //MappingMetaData mappingMetaData = mappings.get(context.getIndex()).get(id);
//        MappingMetaData mappingMetaData = mappings.get(index).get(id);
//        if (mappingMetaData!=null){
//            String source = mappingMetaData.source().string();
//            Json parse = Json.parse(source);
//            LinkedHashMap<String, Object> type = (LinkedHashMap<String, Object>)parse.get(id);
//            LinkedHashMap<String, Object> meta = (LinkedHashMap<String, Object>)type.get("_meta");
//
//            boolean isDocType = (boolean)meta.get(Constant.FieldName.ISDOCTYPE);
//            boolean isDeleted = (boolean)meta.get(Constant.FieldName.ISDELETED);
//            if (isDocType&&(!isDeleted)){
//
//            }else{
//                logger.error(String.format("The type: %s does not exist", id));
//                throw new uContentException(String.format("The type: %s does not exist", id), HttpStatus.NOT_FOUND);
//            }
//
//            builder.startObject();
//
//            builder.field(Constant.FieldName.NAME, id)
//                    .field(Constant.FieldName.DISPLAYNAME, meta.get(Constant.FieldName.DISPLAYNAME).toString())
//                    .field(Constant.FieldName.DESCRIPTION, meta.get(Constant.FieldName.DESCRIPTION).toString());
//
//            ArrayList<Object> properties = (ArrayList<Object>)meta.get(Constant.FieldName.PROPERTIES);
//            if ((properties==null)||properties.size()==0){
//                //Exception
//            }else{
//                builder.startArray(Constant.FieldName.PROPERTIES);
//                for(Object property:properties){
//                    if (property!=null){
//                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
//                        builder.startObject()
//                                .field(Constant.FieldName.NAME, pro.get(Constant.FieldName.NAME) == null ? "" : pro.get(Constant.FieldName.NAME).toString())
//                                .field(Constant.FieldName.TYPE, pro.get(Constant.FieldName.TYPE) == null ? "" : pro.get(Constant.FieldName.TYPE).toString())
//                                .field(Constant.FieldName.INDEX, pro.get(Constant.FieldName.INDEX) == null ? "" : pro.get(Constant.FieldName.INDEX).toString())
//                                .field(Constant.FieldName.ANALYZER, pro.get(Constant.FieldName.ANALYZER) == null ? "" : pro.get(Constant.FieldName.ANALYZER).toString())
//                                .field(Constant.FieldName.REQUIRED, pro.get(Constant.FieldName.REQUIRED) == null ? false : Boolean.valueOf(pro.get(Constant.FieldName.REQUIRED).toString()))
//                                .field(Constant.FieldName.DEFAULTVALUE, pro.get(Constant.FieldName.DEFAULTVALUE) == null ? "" : pro.get(Constant.FieldName.DEFAULTVALUE).toString())
//                                .field(Constant.FieldName.PATTERN, pro.get(Constant.FieldName.PATTERN) == null ? "" : pro.get(Constant.FieldName.PATTERN).toString())
//                                .field(Constant.FieldName.PROMPTMESSAGE, pro.get(Constant.FieldName.PROMPTMESSAGE) == null ? "" : pro.get(Constant.FieldName.PROMPTMESSAGE).toString())
//                                .field(Constant.FieldName.ORDER, pro.get(Constant.FieldName.ORDER) == null ? "" : pro.get(Constant.FieldName.ORDER).toString())
//                                .endObject();
//                    }
//                }
//                builder.endArray();//end of properties
//
//            }
//            builder.endObject(); //end of _meta
//        }
//        return builder;
//    }

    public XContentBuilder get(String id) throws IOException {
        Client client = context.getClient();
        //String index = id.toLowerCase()+"_v1";
        XContentBuilder builder= XContentFactory.jsonBuilder();
        //GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).addTypes(id).get();
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(id).addTypes(id).get();

        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if (mappings==null||mappings.size()==0){
            logger.error(String.format(context.m("TypeService.typeNotExist"), id));
            throw new uContentException(String.format(context.m("TypeService.typeNotExist"), id), HttpStatus.NOT_FOUND);
        }

        String index = "";
        Iterator<ObjectCursor<String>> iterator = mappings.keys().iterator();
        if(iterator.hasNext()){
            index = iterator.next().value;
        }

        //MappingMetaData mappingMetaData = mappings.get(context.getIndex()).get(id);
        MappingMetaData mappingMetaData = mappings.get(index).get(id);
        if (mappingMetaData!=null){
            String source = mappingMetaData.source().string();
            Json parse = Json.parse(source);
            LinkedHashMap<String, Object> type = (LinkedHashMap<String, Object>)parse.get(id);
            LinkedHashMap<String, Object> meta = (LinkedHashMap<String, Object>)type.get("_meta");

            boolean isDocType = (boolean)meta.get(Constant.FieldName.ISDOCTYPE);
            boolean isDeleted = (boolean)meta.get(Constant.FieldName.ISDELETED);
            if (isDocType&&(!isDeleted)){

            }else{
                logger.error(String.format(context.m("TypeService.typeNotExist"), id));
                throw new uContentException(String.format(context.m("TypeService.typeNotExist"), id), HttpStatus.NOT_FOUND);
            }

            builder.startObject();

            builder.field(Constant.FieldName.NAME, id)
                    .field(Constant.FieldName.DISPLAYNAME, meta.get(Constant.FieldName.DISPLAYNAME).toString())
                    .field(Constant.FieldName.DESCRIPTION, meta.get(Constant.FieldName.DESCRIPTION).toString());

            ArrayList<Object> properties = (ArrayList<Object>)meta.get(Constant.FieldName.PROPERTIES);
            if ((properties==null)||properties.size()==0){
                //Exception
            }else{
                builder.startArray(Constant.FieldName.PROPERTIES);
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        builder.startObject()
                                .field(Constant.FieldName.NAME, pro.get(Constant.FieldName.NAME) == null ? "" : pro.get(Constant.FieldName.NAME).toString())
                                .field(Constant.FieldName.TYPE, pro.get(Constant.FieldName.TYPE) == null ? "" : pro.get(Constant.FieldName.TYPE).toString())
                                .field(Constant.FieldName.INDEX, pro.get(Constant.FieldName.INDEX) == null ? "" : pro.get(Constant.FieldName.INDEX).toString())
                                .field(Constant.FieldName.ANALYZER, pro.get(Constant.FieldName.ANALYZER) == null ? "" : pro.get(Constant.FieldName.ANALYZER).toString())
                                .field(Constant.FieldName.REQUIRED, pro.get(Constant.FieldName.REQUIRED) == null ? false : Boolean.valueOf(pro.get(Constant.FieldName.REQUIRED).toString()))
                                .field(Constant.FieldName.DEFAULTVALUE, pro.get(Constant.FieldName.DEFAULTVALUE) == null ? "" : pro.get(Constant.FieldName.DEFAULTVALUE).toString())
                                .field(Constant.FieldName.PATTERN, pro.get(Constant.FieldName.PATTERN) == null ? "" : pro.get(Constant.FieldName.PATTERN).toString())
                                .field(Constant.FieldName.PROMPTMESSAGE, pro.get(Constant.FieldName.PROMPTMESSAGE) == null ? "" : pro.get(Constant.FieldName.PROMPTMESSAGE).toString())
                                .field(Constant.FieldName.ORDER, pro.get(Constant.FieldName.ORDER) == null ? "" : pro.get(Constant.FieldName.ORDER).toString())
                                .endObject();
                    }
                }
                builder.endArray();//end of properties

            }
            builder.endObject(); //end of _meta
        }
        return builder;
    }

    public XContentBuilder getTypeByDisplayName(String displayName) throws IOException {
        XContentBuilder builder = null;
        List<Json> all = getAll(false);
        for(Json ct:all){
            if (String.valueOf(ct.get(Constant.FieldName.DISPLAYNAME)).equals(displayName)){
                builder = get(ct.get(Constant.FieldName.NAME).toString());
            }
        }
        return builder;
    }

    public void exportType(String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = id + ".json";
        InputStream is = createTempTypeFile(id, request);
        if(is==null){
            logger.error(String.format(context.m("TypeService.typeNotExist"), id));
            throw new uContentException(String.format(context.m("TypeService.typeNotExist"), id), HttpStatus.NOT_FOUND);
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/x-msdownload;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.addHeader("Content-Disposition", "attachment;filename=\"" + new String(fileName.getBytes()) + "\"");
        ServletOutputStream os = response.getOutputStream();
        OutputStream bos = new BufferedOutputStream(os);
        InputStream bis = new BufferedInputStream(is);
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        if (bos != null){
            bos.close();
        }
        if (bis != null){
            bis.close();
        }

    }

    public void  importType(HttpServletRequest request) throws IOException {
        MultipartFile[] streams = getStreams(request);
        if(streams!=null&&streams.length>0){
            for(MultipartFile stream:streams){
                InputStream inputStream = stream.getInputStream();
                InputStreamReader read = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(read);
                String str = null;
                while((str = bufferedReader.readLine()) != null){
                    Json body = null;
                    try{
                        body = Json.parse(str);
                    }catch (Exception e){
                        logger.error(context.m("TypeService.illegalFileFormat"));
                        throw new uContentException(context.m("TypeService.illegalFileFormat"), HttpStatus.NOT_FOUND);
                    }

                    create(body);
                }
                read.close();
            }
        }
    }

    private MultipartFile[] getStreams(HttpServletRequest request) {
        if (!(request instanceof MultipartHttpServletRequest))
            return new MultipartFile[] {};
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        return multipartRequest.getFileMap().values().toArray(new MultipartFile[] {});
    }



    private InputStream createTempTypeFile(String id, HttpServletRequest request) throws IOException {
        String path = request.getSession().getServletContext().getRealPath("/") + "/tempExportTypeFolder";
        File file = new File(path + "/" + id + ".txt");
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (file.exists()) {
            file.deleteOnExit();
        }
        file.createNewFile();
        String str = get(id).string();
        FileOutputStream fos = new FileOutputStream(file);
        Writer writer = new OutputStreamWriter(fos, "UTF-8");
        writer.write(str);
        writer.flush();
        writer.close();


        File newFile = new File(path + "/" + id + ".txt");
        if (!newFile.exists()) {
            return null;
        }
        return new FileInputStream(newFile);
    }

    public Map<String, Map<String, Object>> getProperties(String id) throws IOException {
        Client client = context.getClient();
        //String index = id.toLowerCase()+"_v0";
        Map<String, Map<String, Object>> pros = new HashMap<String, Map<String, Object>>();
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(id).addTypes(id).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if (mappings==null||mappings.size()==0){
            logger.error(String.format(context.m("TypeService.typeNotExist"), id));
            throw new uContentException(String.format(context.m("TypeService.typeNotExist"), id), HttpStatus.NOT_FOUND);
        }

        //填装基本属性
        pros.put(Constant.FieldName.NAME,makeProperty(Constant.FieldName.NAME, "string", Constant.FieldName.NOT_ANALYZED, "", true, "", "", "", ""));
        pros.put(Constant.FieldName.DESCRIPTION,makeProperty(Constant.FieldName.DESCRIPTION, "string", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));
        pros.put(Constant.FieldName.CREATEDBY,makeProperty(Constant.FieldName.CREATEDBY, "string", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));
        pros.put(Constant.FieldName.CREATEDON,makeProperty(Constant.FieldName.CREATEDON, "date", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));
        pros.put(Constant.FieldName.LASTUPDATEDBY,makeProperty(Constant.FieldName.LASTUPDATEDBY, "string", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));
        pros.put(Constant.FieldName.LASTUPDATEDON,makeProperty(Constant.FieldName.LASTUPDATEDON, "date", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));

        pros.put(Constant.FieldName.TAG,makeProperty(Constant.FieldName.TAG, "string", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));

        String index = "";
        Iterator<ObjectCursor<String>> iterator = mappings.keys().iterator();
        if(iterator.hasNext()){
            index = iterator.next().value;
        }

        //MappingMetaData mappingMetaData = mappings.get(context.getIndex()).get(id);
        MappingMetaData mappingMetaData = mappings.get(index).get(id);
        if (mappingMetaData!=null){
            String source = mappingMetaData.source().string();
            Json parse = Json.parse(source);
            LinkedHashMap<String, Object> type = (LinkedHashMap<String, Object>)parse.get(id);
            LinkedHashMap<String, Object> meta = (LinkedHashMap<String, Object>)type.get("_meta");

            boolean isDocType = (boolean)meta.get(Constant.FieldName.ISDOCTYPE);
            boolean isDeleted = (boolean)meta.get(Constant.FieldName.ISDELETED);
            if (isDocType&&(!isDeleted)){

            }else{
                logger.error(String.format(context.m("TypeService.typeNotExist"), id));
                throw new uContentException(String.format(context.m("TypeService.typeNotExist"), id), HttpStatus.NOT_FOUND);
            }


            ArrayList<Object> properties = (ArrayList<Object>)meta.get(Constant.FieldName.PROPERTIES);
            if ((properties==null)||properties.size()==0){
                //Exception
            }else{
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        pros.put(pro.get(Constant.FieldName.NAME).toString(),makeProperty(
                                pro.get(Constant.FieldName.NAME) == null ? "" : pro.get(Constant.FieldName.NAME).toString(),
                                pro.get(Constant.FieldName.TYPE) == null ? "" : pro.get(Constant.FieldName.TYPE).toString(),
                                pro.get(Constant.FieldName.INDEX) == null ? "" : pro.get(Constant.FieldName.INDEX).toString(),
                                pro.get(Constant.FieldName.ANALYZER) == null ? "" : pro.get(Constant.FieldName.ANALYZER).toString(),
                                pro.get(Constant.FieldName.REQUIRED) == null ? false : Boolean.valueOf(pro.get(Constant.FieldName.REQUIRED).toString()),
                                pro.get(Constant.FieldName.DEFAULTVALUE) == null ? "" : pro.get(Constant.FieldName.DEFAULTVALUE).toString(),
                                pro.get(Constant.FieldName.PATTERN) == null ? "" : pro.get(Constant.FieldName.PATTERN).toString(),
                                pro.get(Constant.FieldName.PROMPTMESSAGE) == null ? "" : pro.get(Constant.FieldName.PROMPTMESSAGE).toString(),
                                pro.get(Constant.FieldName.ORDER)==null?"":pro.get(Constant.FieldName.ORDER).toString()));
                    }
                }
            }
        }
        return pros;
    }

    public XContentBuilder update(String id, Json body) throws IOException {
        Client client = context.getClient();
        //String index = id.toLowerCase()+"_v0";
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(id).addTypes(id).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if (mappings==null||mappings.size()==0){
            logger.error(String.format(context.m("TypeService.typeNotExist"), id));
            throw new uContentException(String.format(context.m("TypeService.typeNotExist"), id), HttpStatus.NOT_FOUND);
        }

        String index = "";
        Iterator<ObjectCursor<String>> iterator = mappings.keys().iterator();
        if(iterator.hasNext()){
            index = iterator.next().value;
        }

        //MappingMetaData mappingMetaData = mappings.get(context.getIndex()).get(id);
        MappingMetaData mappingMetaData = mappings.get(index).get(id);
        if (mappingMetaData!=null) {
            String source = mappingMetaData.source().string();
            Json parse = Json.parse(source);
            LinkedHashMap<String, Object> type = (LinkedHashMap<String, Object>) parse.get(id);
            LinkedHashMap<String, Object> meta = (LinkedHashMap<String, Object>) type.get("_meta");

            boolean isDocType = (boolean) meta.get(Constant.FieldName.ISDOCTYPE);
            boolean isDeleted = (boolean) meta.get(Constant.FieldName.ISDELETED);
            if (isDocType && (!isDeleted)) {

            } else {
                logger.error(String.format(context.m("TypeService.typeNotExist"), id));
                throw new uContentException(String.format(context.m("TypeService.typeNotExist"), id), HttpStatus.NOT_FOUND);
            }
        }
        return createOrUpdateType(id, body, "update");
    }

    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        //String index = id.toLowerCase()+"_v0";
        boolean acknowledged = false;
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(id).addTypes(id).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if (mappings==null||mappings.size()==0){
            logger.error(String.format(context.m("TypeService.typeNotExist"), id));
            throw new uContentException(String.format(context.m("TypeService.typeNotExist"), id), HttpStatus.NOT_FOUND);
        }

        String index = "";
        Iterator<ObjectCursor<String>> iterator = mappings.keys().iterator();
        if(iterator.hasNext()){
            index = iterator.next().value;
        }

        //CountResponse countResponse = context.getClient().prepareCount(context.getIndex()).setTypes(id).execute().actionGet();
        CountResponse countResponse = context.getClient().prepareCount(index).setTypes(id).execute().actionGet();
        if(countResponse.getCount()>0){
            logger.error(String.format(context.m("TypeService.typeHasInstances"), id));
            throw new uContentException(String.format(context.m("TypeService.typeHasInstances"), id), HttpStatus.BAD_REQUEST);
        }

        //MappingMetaData mappingMetaData = mappings.get(context.getIndex()).get(id);
        MappingMetaData mappingMetaData = mappings.get(index).get(id);
        if (mappingMetaData!=null) {
            String source = mappingMetaData.source().string();
            Json parse = Json.parse(source);
            LinkedHashMap<String, Object> type = (LinkedHashMap<String, Object>) parse.get(id);
            LinkedHashMap<String, Object> meta = (LinkedHashMap<String, Object>) type.get("_meta");

            boolean isDocType = (boolean) meta.get(Constant.FieldName.ISDOCTYPE);
            boolean isDeleted = (boolean) meta.get(Constant.FieldName.ISDELETED);
            if (isDocType && (!isDeleted)) {

            } else {
                logger.error(String.format(context.m("TypeService.typeNotExist"), id));
                throw new uContentException(String.format(context.m("TypeService.typeNotExist"), id), HttpStatus.NOT_FOUND);
            }

            DeleteIndexResponse deleteIndexResponse = client.admin().indices().prepareDelete(index).execute().actionGet();
            acknowledged = deleteIndexResponse.isAcknowledged();
        }
        return XContentFactory.jsonBuilder().startObject().field("acknowledged",acknowledged).endObject();
    }

    public static Map<String, Object> makeProperty(String name, String type,
                                                String index, String analyzer,
                                                boolean required,
                                                String defaultValue, String pattern,
                                                String promptMessage, String order){
        Map<String, Object> property = new HashMap<String, Object>();
        property.put(Constant.FieldName.NAME, name);
        property.put(Constant.FieldName.TYPE, type);
        property.put(Constant.FieldName.INDEX, index);
        property.put(Constant.FieldName.ANALYZER, analyzer);
        property.put(Constant.FieldName.REQUIRED, required);
        property.put(Constant.FieldName.DEFAULTVALUE, defaultValue);
        property.put(Constant.FieldName.PATTERN, pattern);
        property.put(Constant.FieldName.PROMPTMESSAGE, promptMessage);
        property.put(Constant.FieldName.ORDER, order);
        return property;
    }

}
