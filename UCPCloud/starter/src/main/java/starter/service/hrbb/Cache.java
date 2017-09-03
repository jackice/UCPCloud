package starter.service.hrbb;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import starter.service.Constant;
import starter.service.TypeService;
import starter.service.UserService;
import starter.uContentException;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wbpanwei on 2016/3/4.
 * <p>
 * 提供类型ID列表、类型定义信息、用户所在的组列表的缓存
 * 非线程安全，不保证缓存数据绝对的同步
 */
@Service
public class Cache {

    @Autowired
    private UserService userService;

    @Autowired
    private Client client;

    private Logger logger = Logger.getLogger(Cache.class);


    private static Map<String, Map<String, Map<String, Object>>> typeDefinitions = new HashMap<>();

    private static Map<String, List<String>> groupsOfUser = new HashMap<>();


    public Map<String, Map<String, Object>> getProperties(String typeId) {
        return typeDefinitions.get(typeId);
    }

    public String[] typeIds(){
        return typeDefinitions.keySet().toArray(new String[]{});
    }


    public List<String> getGroupsOfUser(String userId) {
        List<String> groups = groupsOfUser.get(userId);
        if (groups == null) {
            try {
                groups = userService.getGroupsOfUser(userId);
                if (!groups.isEmpty()) {
                    groupsOfUser.put(userId, groups);
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                return groups;
            }
        }
        return groups;
    }


    private void flushTypes() throws IOException {
        typeDefinitions = getTypeDefinitions();
    }


    private void flushGroupsOfUser() throws IOException {
        if (groupsOfUser.isEmpty()) {
            return;
        }
        for (String userId : groupsOfUser.keySet()) {
            try {
                userService.get(userId, false);
            } catch (uContentException e) {
                groupsOfUser.remove(userId);//用户不存在，从缓存中移除该用户
                continue;
            }
            groupsOfUser.put(userId, userService.getGroupsOfUser(userId));
        }
    }


    private Map<String, Map<String, Map<String, Object>>> getTypeDefinitions() throws IOException {
        Map<String, Map<String, Map<String, Object>>> typeDefinitions = new HashMap<>();
        String[] indices = client.admin().indices().prepareGetIndex().get().indices();
        if (indices == null || indices.length == 0) {
            return typeDefinitions;
        }
        for(String index : indices) {
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = client.admin().indices().prepareGetMappings(index).get().getMappings();
            ImmutableOpenMap<String, MappingMetaData> types = mappings.get(index);
            for (ObjectObjectCursor<String, MappingMetaData> type : types) {
                String typeName = type.key;
                MappingMetaData mappingMetaData = types.get(typeName);
                Map<String, Object> source = mappingMetaData.getSourceAsMap();
                Object meta = source.get("_meta");
                if (meta == null) {
                    continue;
                }
                LinkedHashMap<String, Object> _metaMap = (LinkedHashMap<String, Object>) source.get("_meta");
                if (_metaMap!=null&&_metaMap.get("isDocType")!=null&&_metaMap.get("isDocType").toString().equals("true")) {
                    Map<String, Map<String, Object>> map = new HashMap<>();
                    //填装基本属性
                    map.put(Constant.FieldName.NAME, TypeService.makeProperty(Constant.FieldName.NAME, "string", Constant.FieldName.NOT_ANALYZED, "", true, "", "", "", ""));
                    map.put(Constant.FieldName.DESCRIPTION,TypeService.makeProperty(Constant.FieldName.DESCRIPTION, "string", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));
                    map.put(Constant.FieldName.CREATEDBY,TypeService.makeProperty(Constant.FieldName.CREATEDBY, "string", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));
                    map.put(Constant.FieldName.CREATEDON,TypeService.makeProperty(Constant.FieldName.CREATEDON, "date", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));
                    map.put(Constant.FieldName.LASTUPDATEDBY,TypeService.makeProperty(Constant.FieldName.LASTUPDATEDBY, "string", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));
                    map.put(Constant.FieldName.LASTUPDATEDON,TypeService.makeProperty(Constant.FieldName.LASTUPDATEDON, "date", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));
                    map.put(Constant.FieldName.TAG,TypeService.makeProperty(Constant.FieldName.TAG, "string", Constant.FieldName.NOT_ANALYZED, "", false, "", "", "", ""));

                    List<Map<String, Object>> properties = (List<Map<String, Object>>) _metaMap.get("properties");
                    for (Map<String, Object> propertyMap : properties) {
                        map.put(propertyMap.get("name").toString(), propertyMap);
                    }
                    typeDefinitions.put(typeName, map);
                }
            }
        }
        return typeDefinitions;
    }



    @Scheduled(cron = "*/10 * * * * ?")
    public void doJob() {
//        System.out.println("do cache job...");
        try {
            flushTypes();
            flushGroupsOfUser();
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
    }

}
