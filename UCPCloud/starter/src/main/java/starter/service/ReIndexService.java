package starter.service;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class ReIndexService {

    @Autowired
    private RequestContext context;


    public XContentBuilder getReindexLog(String operationId, int size) throws IOException {
        String[] indices = {"ucp_system"};
        TypesExistsRequest typesExistsRequest = new TypesExistsRequest(indices, "reindexLog");
        TypesExistsResponse typesExistsResponse = context.getClient().admin().indices().typesExists(typesExistsRequest).actionGet();
        if (!typesExistsResponse.isExists()) {
            return JsonXContent.contentBuilder();
        }
        SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch("ucp_system").setTypes("reindexLog").setSize(size).addSort("timestamp", SortOrder.DESC);
        String query = "{\"term\":{\"operationId\":" + operationId + "}}";
        searchRequestBuilder.setQuery(query);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        xContentBuilder.startArray("log");
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            xContentBuilder.startObject();
            Iterator<Map.Entry<String, Object>> iterator = hit.getSource().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                xContentBuilder.field(entry.getKey(), entry.getValue());
            }
            xContentBuilder.endObject();
        }
        xContentBuilder.endArray().endObject();
        return xContentBuilder;
    }


    public Json check() throws IOException {
        Json json = new Json();
        String[] indices = new String[1];
        indices[0] = "ucp_system";
        IndicesExistsResponse indicesExistsResponse = context.getClient().admin().indices().prepareExists(indices[0]).execute().actionGet();
        if (!indicesExistsResponse.isExists()) {
            json.put("isFinished", true);
            return json;
        }
        TypesExistsRequest typesExistsRequest = new TypesExistsRequest(indices, "reindexSummary");
        TypesExistsResponse typesExistsResponse = context.getClient().admin().indices().typesExists(typesExistsRequest).actionGet();
        if (!typesExistsResponse.isExists()) {
            json.put("isFinished", true);
            return json;
        }
        SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch("ucp_system").setTypes("reindexSummary").addSort("operationId", SortOrder.DESC).setSize(1);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits.length > 0) {
            SearchHit hit = hits[0];
            boolean isFinished = Boolean.valueOf(hit.getSource().get("isFinished").toString());
            json.put("isFinished", isFinished);
            json.put("operationId", hit.getSource().get("operationId").toString());
            json.put("srcIndex", hit.getSource().get("srcIndex").toString());
            json.put("targetIndex", hit.getSource().get("targetIndex").toString());
            json.put("total", hit.getSource().get("total").toString());
            json.put("type", hit.getSource().get("type").toString());
        }else{
            json.put("isFinished", true);
        }
        return json;
    }


    public void resetReindexStatus(){
        IndicesExistsResponse indicesExistsResponse = context.getClient().admin().indices().prepareExists("ucp_system").execute().actionGet();
        if (indicesExistsResponse.isExists()) {
            TypesExistsResponse typesExistsResponse = context.getClient().admin().indices().prepareTypesExists("ucp_system").setTypes("reindexSummary").execute().actionGet();
            if (typesExistsResponse.isExists()) {
                SearchResponse searchResponse = context.getClient().prepareSearch("ucp_system").setTypes("reindexSummary").addSort("operationId", SortOrder.DESC).setSize(1).execute().actionGet();
                SearchHit[] hits = searchResponse.getHits().getHits();
                if (hits.length > 0) {
                    Object isFinished = hits[0].getSource().get("isFinished");
                    if (isFinished != null) {
                        if ((Boolean)isFinished == false) {
                            context.getClient().prepareUpdate(hits[0].getIndex(), hits[0].getType(), hits[0].getId()).setDoc("isFinished", true).execute().actionGet();
                        }
                    }
                }
            }
        }
    }

    public static class ReindexJob implements Runnable {

        private Client client;
        private int bulkActions;
        private int flushInterval;
        private int bulkSize;
        private String alias;
        private String type;
        private String operationId;
        private long finished = 0l;

        public ReindexJob(Client client, String alias, String type, String operationId) {
            this.client = client;
            this.alias = alias;
            this.type = type;
            this.operationId = operationId;
        }

        public void setBulkActions(int bulkActions) {
            this.bulkActions = bulkActions;
        }

        public void setFlushInterval(int flushInterval) {
            this.flushInterval = flushInterval;
        }

        public void setBulkSize(int bulkSize) {
            this.bulkSize = bulkSize;
        }

        private Logger logger = Logger.getLogger(ReindexJob.class);


        @Override
        public void run() {
            try {
                String[] indices = originalName(type);
                if (indices.length < 0) {
                    logger.warn(String.format("Can not find any index on alias : %s, reindex process end.", type));
                    return;
                }
                for(int i = 0; i < indices.length; i++){
                    String target = name(indices[0]);
                    boolean isIndexExist = false;
                    do{
                        IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(target).execute().actionGet();
                        if (indicesExistsResponse.isExists()) {
                            logger.warn(String.format("target index %s exist, skip this name, continue to obtain another name", target));
                            isIndexExist = true;
                            target = name(target);
                        }else{
                            isIndexExist = false;
                        }
                    } while(isIndexExist);
                    logger.info(String.format("target index %s is OK, prepare to do reindex against on this index", target));
                    copyMappings(indices[i], target);
                    copyIndex(indices[0], target);
                    alias(indices[i], target, alias, type);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            } catch (ExecutionException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        private String[] originalName(String alias) {
            if (!client.admin().indices().prepareExists(alias).execute().actionGet().isExists()) {
                logger.error("The index: " + alias + " which to be reIndexed is not exist");
                throw new RuntimeException("The index: " + alias + " which to be reIndexed is not exist");
            }
            return client.admin().indices().prepareGetIndex().setIndices(alias).execute().actionGet().indices();
        }

        private void copyMappings(String index, String target) throws ExecutionException, InterruptedException, IOException {
            logger.info(String.format("copy mappings from %s to %s start......", index, target));
            GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings(index).get();
            Iterator<ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>>> iterator = getMappingsResponse.mappings().iterator();
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
            xContentBuilder.startObject();
            xContentBuilder.startObject("mappings");
            while (iterator.hasNext()) {
                ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>> entry = iterator.next();
                for (ObjectObjectCursor<String, MappingMetaData> typeEntry : entry.value) {
                    Map meta = (Map) typeEntry.value.getSourceAsMap().get("_meta");
                    if (meta != null && meta.get("isDeleted") != null && (Boolean) meta.get("isDeleted") == true) {
                        continue;
                    }
                    xContentBuilder.field(typeEntry.key);
                    xContentBuilder.map(typeEntry.value.sourceAsMap());
                }
            }
            xContentBuilder.endObject().endObject();
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(target);
            createIndexRequest.source(xContentBuilder);
            client.admin().indices().create(createIndexRequest).actionGet();
            logger.info(String.format("copy mappings from %s to %s end.", index, target));
        }

        public static String name(String index) {
            if (!index.contains("_v")) {
                return index + "_v1";
            }
            int v = index.lastIndexOf("_v");
            String prefix = index.substring(0, v + 2);
            String suffix = index.substring(v + 2);
            String newSuffix = String.valueOf(Integer.valueOf(suffix) + 1);
            return prefix + newSuffix;
        }

        private void copyIndex(String index, String target) throws IOException {
            BulkProcessor bulkProcessor = null;
            try {
                SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setSearchType(SearchType.SCAN).setScroll("1m").setVersion(true);
                SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
                long total = searchResponse.getHits().getTotalHits();
                summary(index, target, total);//记录此次reindex的总述信息
                if (total <= 0) {
                    logger.info(String.format("the index %s which to reindex has no data, reindex end.", index));
                    return;
                }
                bulkProcessor = initBulkProcessor(total);
                logger.info(String.format("reIndex index from %s to %s start...", index, target));
                do {
                    searchResponse = client.prepareSearchScroll(searchResponse.getScrollId()).setScroll("1m").execute().actionGet();
                    for (SearchHit hit : searchResponse.getHits().getHits()) {
                        Map<String, Object> source = hit.getSource();
                        bulkProcessor.add(new IndexRequest(target, hit.getType(), hit.getId()).source(source));
                    }
                } while (searchResponse.getHits().getHits().length > 0);
            } finally {
                if (bulkProcessor != null) {
                    bulkProcessor.flush();
                    bulkProcessor.close();
                }
                logger.info(String.format("copy index from %s to %s end.", index, target));
            }
        }

        private void summary(String index, String target, long total) throws IOException {
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("operationId", operationId)
                    .field("srcIndex", index)
                    .field("targetIndex", target)
                    .field("type", type)
                    .field("total", total)
                    .field("operationDate", new DateTime().toLocalDateTime());
                    if(total <= 0) {
                        xContentBuilder.field("isFinished", true);
                    }else{
                        xContentBuilder.field("isFinished", false);
                    }
            xContentBuilder.endObject();
            client.prepareIndex("ucp_system", "reindexSummary", operationId).setSource(xContentBuilder).execute().actionGet();
            logger.info(String.format("log reindexSummary, operationId = %s, total doc is %s", operationId, total));

        }

        private BulkProcessor initBulkProcessor(final long total) {
            BulkProcessor.Builder builder = BulkProcessor.builder(client, new BulkProcessor.Listener() {
                public void beforeBulk(long executionId, BulkRequest request) {
                    logger.info(String.format("executionId:%s, numberOfActions:%s", executionId, request.numberOfActions()));
                }

                public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse response) {
                    if (response.hasFailures()) {
                        logger.error(response.buildFailureMessage());
                    }
                    try {
                        Double _total = Double.valueOf(total);
                        double rate = new BigDecimal((bulkRequest.numberOfActions() + finished) / _total).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
                        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                                .startObject()
                                .field("executionId", executionId)
                                .field("operationId", operationId)
                                .field("numberOfActions", bulkRequest.numberOfActions())
                                .field("finished", bulkRequest.numberOfActions() + finished)
                                .field("total", total)
                                .field("rate", rate)
                                .field("timestamp", new Date().getTime())
                                .endObject();
                        client.prepareIndex("ucp_system", "reindexLog").setSource(xContentBuilder).execute().actionGet();
                        if (bulkRequest.numberOfActions() + finished == total) {
                            client.prepareUpdate("ucp_system", "reindexSummary", operationId).setDoc("isFinished", true).execute().actionGet();
                        }
                        finished += bulkRequest.numberOfActions();
                        logger.info(xContentBuilder.string());
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }

                public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                    logger.info(String.format("executionId: %s failed\r\n, %s", executionId, failure.getMessage()));
                }
            }).setConcurrentRequests(0);
            if (bulkActions > 0 || bulkActions == -1) {
                builder.setBulkActions(bulkActions);
            }
            if (flushInterval > 0) {
                builder.setFlushInterval(TimeValue.timeValueSeconds(flushInterval));
            }
            if (bulkSize > 0 || bulkSize == -1) {
                builder.setBulkSize(new ByteSizeValue(bulkSize, ByteSizeUnit.MB));
            }
            builder.setConcurrentRequests(0);
            return builder.build();
        }


        private void alias(String index, String target, String businessAlias, String alias) {
            IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
            indicesAliasesRequest.removeAlias(index, businessAlias);
            indicesAliasesRequest.addAlias(businessAlias, target);
            indicesAliasesRequest.addAlias(alias, target);
            logger.info(String.format("remove index %s from alias %s and add index %s instead", index, businessAlias, target));
            client.admin().indices().aliases(indicesAliasesRequest, new ActionListener<IndicesAliasesResponse>() {
                public void onResponse(IndicesAliasesResponse indicesAliasesResponse) {
                    //TODO 删除原索引
                    client.admin().indices().prepareDelete(index).execute();
                    logger.info(String.format("delete old index %s", index));
                }

                public void onFailure(Throwable e) {
                    //TODO
                    e.printStackTrace();
                }
            });
        }
    }


}
