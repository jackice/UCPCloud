package starter.rest;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import starter.EsConfig;
import starter.RequestContext;
import starter.service.*;
import starter.uContentException;

@RestController
@RequestMapping(value="initialization/",produces = MediaType.APPLICATION_JSON_VALUE)
public class Initialization {

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ViewService viewService;

    @Autowired
    private TagService tagService;

    @Autowired
    private LogService logService;

    @Autowired
    private EsConfig esConfig;

    @Autowired
    private RequestContext context;

    private Logger logger = Logger.getLogger(Initialization.class);

    @RequestMapping(value = "status", method = RequestMethod.GET)
    public boolean checkInitialized() {
        Client client = context.getClient();

        //check system indices
        IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(Constant.SYSTEM).execute().actionGet();
        if (!indicesExistsResponse.isExists()){
            return false;
        }
        
        //check log indices
        indicesExistsResponse = client.admin().indices().prepareExists(Constant.LOG_INDEX).execute().actionGet();
        if (!indicesExistsResponse.isExists()){
            return false;
        }

        //check user mapping
        GetMappingsResponse getUserMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(Constant.SYSTEM).addTypes(Constant.FieldName.USERTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> userMappings = getUserMappingsResponse.getMappings();
        if(userMappings.size()==0){
            return false;
        }

        //check admin user
        if (!client.prepareGet(Constant.SYSTEM, Constant.FieldName.USERTYPENAME, Constant.ADMIN).execute().actionGet().isExists()) {
            return false;
        }

        //check group mapping
        GetMappingsResponse getGroupMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(Constant.SYSTEM).addTypes(Constant.FieldName.GROUPTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> groupMappings = getGroupMappingsResponse.getMappings();
        if(groupMappings.size()==0){
            return false;
        }

        //check everyone adminGroup
        if (!client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, Constant.EVERYONE).execute().actionGet().isExists()) {
            return false;
        }
        if (!client.prepareGet(Constant.SYSTEM, Constant.FieldName.GROUPTYPENAME, Constant.ADMINGROUP).execute().actionGet().isExists()) {
            return false;
        }

        //check view mapping
        GetMappingsResponse getViewMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(Constant.SYSTEM).addTypes(Constant.FieldName.VIEWTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> viewMappings = getViewMappingsResponse.getMappings();
        if(viewMappings.size()==0){
            return false;
        }

        //check tag mapping
        GetMappingsResponse getTagMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(Constant.SYSTEM).addTypes(Constant.FieldName.TAGTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> tagMappings = getTagMappingsResponse.getMappings();
        if(tagMappings.size()==0){
            return false;
        }

        //check log mapping
        GetMappingsResponse getLogMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(Constant.LOG_INDDX_NAME).addTypes(Constant.LOG_TYPE_NAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> logMappings = getLogMappingsResponse.getMappings();
        if(logMappings.size()==0){
            return false;
        }

        return true;
    }

    @RequestMapping(value = "initial", method = RequestMethod.POST)
    public void systemDataInitialize() {
        try {
            Client client = context.getClient();

            //check and create indices
//            String indices = context.getAlias() + Constant.INDICES_SUFFIX;
            String indices = Constant.SYSTEM;
            IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(indices).execute().actionGet();
            if (!indicesExistsResponse.isExists()){
                //添加分片和副本设置
                //TODO:这里分片和副本如何设置？
                Settings settings = Settings.builder().put("number_of_shards", esConfig.getShards()).put("number_of_replicas", esConfig.getReplicas()).build();
                //设置indices
                //client.admin().indices().prepareCreate(indices).addAlias(new Alias(context.getAlias())).setSettings(settings).execute().actionGet();
                client.admin().indices().prepareCreate(indices).setSettings(settings).execute().actionGet();
            }

            String log_indices = Constant.LOG_INDDX_NAME;
            IndicesExistsResponse logIndicesExistsResponse = client.admin().indices().prepareExists(log_indices).execute().actionGet();
            if (!logIndicesExistsResponse.isExists()){
                //添加分片和副本设置
                Settings log_settings = Settings.builder().put("number_of_shards", esConfig.getShards()).put("number_of_replicas", esConfig.getReplicas()).build();
                //设置indices
                client.admin().indices().prepareCreate(log_indices).setSettings(log_settings).execute().actionGet();
            }

            //check again
            if (client.admin().indices().prepareExists(indices).execute().actionGet().isExists()){
                userService.initialUserData();
                groupService.initialGroupData();
                viewService.initialViewData();
                tagService.initialTagData();;
            }

            if (client.admin().indices().prepareExists(log_indices).execute().actionGet().isExists()){
                logService.initialLogData();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new uContentException(context.m("Initialization.failed"), HttpStatus.BAD_REQUEST);
        }
    }

}
