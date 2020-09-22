package starter.service;


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
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
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
import starter.RequestContext;
import starter.rest.Json;
import starter.service.fs.FileSystem;
import starter.service.hrbb.Cache;
import starter.uContentException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentService {

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

    private Logger logger = Logger.getLogger(DocumentService.class);

    private static Tika tika = new Tika();

/**
 * 查询数据
 * @param types 类型
 * @param query 查询语句
 * @param start 
 * @param limit
 * @param sort
 * @param allowableActions 是否批量查询
 * @param fulltext 是否全文检索
 * @param containsType 是否包含类型
 * @return
 * @throws IOException
 */
    public XContentBuilder query(String[] types, String query, int start, int limit, SortBuilder[] sort, boolean allowableActions, boolean fulltext, boolean containsType) throws IOException {
        SearchRequestBuilder searchRequestBuilder = null;
        //set types
        if (types == null || types.length == 0) {
//            types = typeService.getAllTypes().toArray(new String[]{});
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
        //全流程不做acl 20160708 注释 jackice 
        BoolQueryBuilder filter = QueryBuilders.boolQuery().should(QueryBuilders.termQuery("_acl.read.users", context.getUserName()));
//        List<String> groups = userService.getGroupsOfUser(context.getUserName());
        List<String> groups = cache.getGroupsOfUser(context.getUserName());
        for (String group : groups) {
            filter.should(QueryBuilders.termQuery("_acl.read.groups", group));
        }
        filter.minimumNumberShouldMatch(1);
        searchRequestBuilder.setPostFilter(filter);
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

/**
 * 设置查询结果高亮显示
 * @param searchRequestBuilder
 * @param query
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

/**
 * 创建记录
 * @param type
 * @param body
 * @return
 * @throws IOException
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
/**
 * 格式化权限数据
 * @param body
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
//        } else {
//            body.put("_acl", null);
        }
    }
/**
 * 创建图片记录 
 * @param type
 * @param body
 * @param files
 * @return
 * @throws IOException
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
/**
 * 上传文件流
 * @param type
 * @param file
 * @return
 * @throws IOException
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
        } finally {
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

/**
 * 查询记录
 * @param type
 * @param id
 * @param head
 * @param allowableActions
 * @param containsType
 * @return
 * @throws IOException
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
/**
 * 查询记录
 * @param id
 * @param allowableActions
 * @param containsType
 * @return
 * @throws IOException
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

/**
 * 更新记录
 * @param type
 * @param id
 * @param body
 * @param files
 * @return
 * @throws IOException
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
        } else {
            beforeUpdate(body);
            //默认文档加上管理员组的权限，防止客户端调用删掉所有权限。
            if(body.get(Constant.FieldName.ACL)!=null){
            	addAdminGrouptoDoc(body);
            }
            
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

/**
 * 删除记录
 * @param type
 * @param id
 * @return
 * @throws IOException
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
/**
 * 批量删除记录
 * @param body
 * @return
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

/**
 * 创建记录之前进行的操作
 * @param body
 */
    public void beforeCreate(Map body) {
        if (body != null) {
            LocalDateTime localDateTime = new DateTime().toLocalDateTime();
            body.put(Constant.FieldName.CREATEDBY, context.getUserName());
            body.put(Constant.FieldName.CREATEDON, localDateTime);

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
            } else {
                addAdminGrouptoDoc(body);
            }
        }
    }

/**
 * 添加管理群组
 * @param body
 */
    public void addAdminGrouptoDoc(Map body) {
        doAddAdminGroup(body, "read");
        doAddAdminGroup(body, "write");
    }
/**
 * 添加管理群组
 * @param body
 * @param op
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
        } else {
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

            } else {
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

/**
 * 验证权限
 * @param acl
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

/**
 * 获取操作人员权限
 * @param user
 * @param acl
 * @return
 * @throws IOException
 */
    public Set getUserPermission(String user, Object acl) throws IOException {
        Map<String, Object> _acl = (Map<String, Object>) acl;
        Set uPermission = getPermissionByUser(user, _acl);
        List<String> groups = getGroups(user);
        Set gPermission = getPermissionByGroups(groups, _acl);
        uPermission.addAll(gPermission);
        return uPermission;
    }
/**
 * 根据用户获取所在群组
 * @param user
 * @return
 * @throws IOException
 */
    private List<String> getGroups(String user) throws IOException {
        return userService.getGroupsOfUser(user);
    }
/**
 * 获取操作人员权限
 * @param user
 * @param acl
 * @return
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
/**
 * 获取群组权限
 * @param groups
 * @param acl
 * @return
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
/**
 * 判断是否拥有权限
 * @param user
 * @param acl
 * @param action
 * @return
 * @throws IOException
 */
    private boolean hasPermission(String user, Object acl, Constant.Permission action) throws IOException {
        Map<String, Object> _acl = (Map<String, Object>) acl;
        Set permission = getPermissionByUser(user, _acl);
        if (permission.contains(action.toString())) {
            return true;
        } else {
            List<String> groups = getGroups(user);
            permission = getPermissionByGroups(groups, _acl);
            return permission.contains(action.toString());
        }
    }
/**
 * 更新记录前的操作
 * @param body
 */
    public void beforeUpdate(Map body) {
        if (body != null) {
            LocalDateTime localDateTime = new DateTime().toLocalDateTime();
            body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
            body.put(Constant.FieldName.LASTUPDATEDON, localDateTime);
        }
    }

/**
 * 检查权限
 * @param type
 * @param id
 * @param user
 * @param permission
 * @param includeFullText
 * @return
 * @throws IOException
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
/**
 * 检查权限
 * @param type
 * @param id
 * @param user
 * @param permission
 * @return
 * @throws IOException
 */
    public GetResponse checkPermission(String type, String id, String user, Constant.Permission permission) throws IOException {
        return this.checkPermission(type, id, user, permission, false);
    }
/**
 * 验证权限
 * @param body
 * @param type
 * @throws IOException
 */
    public void validate(Map body, String type) throws IOException {
//        Map<String, Map<String, Object>> definition1 = typeService.getProperties(type);
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
/**
 * 格式化数据
 * @param property
 * @param value
 * @return
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
//                    String numberPattern = "([-\\+]?[1-9]([0-9]*)(\\.[0-9]+)?)|(^0$)";
//                    Pattern p = Pattern.compile(numberPattern);
//                    if(!p.matcher(stringValue).matches()){
//                        logger.error(String.format("the value %s of property %s is not a number", stringValue, property.get("name").toString()));
//                        throw new uContentException(String.format("", stringValue, property.get("name").toString()), HttpStatus.BAD_REQUEST);
//                    }
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
            throw new uContentException(String.format(context.m("Document.PropertyTypeNotCorrect"), property.get("name").toString(), stringValue,propType), HttpStatus.BAD_REQUEST);
        }

    }
/**
 * 根据类型，统计图片数量
 * @param type
 * @return
 * @throws IOException
 */
    public XContentBuilder count(String type) throws IOException {
        CountResponse countResponse = context.getClient().prepareCount(type).setTypes(type).execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startObject()
//                .field("_index", context.getIndex())
                .field("alias", type)
                .field("_type", type)
                .field("count", countResponse.getCount());
        xContentBuilder.endObject();
        return xContentBuilder;
    }

/**
 * 解析文件成xml
 * @param file
 * @return
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


}
