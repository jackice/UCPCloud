package starter.service;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.RequestContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Service
public class MonitorService {

    @Autowired
    private RequestContext context;
    public static final ToXContent.Params EMPTY_PARAMS = new EmptyParams();

    public XContentBuilder state() throws IOException {
        ClusterStateResponse response = context.getClient().admin().cluster().prepareState().execute().actionGet();
        return response.getState().toXContent(JsonXContent.contentBuilder().startObject(), EMPTY_PARAMS).endObject();
    }

    public XContentBuilder health() throws IOException {
        ClusterHealthResponse response = context.getClient().admin().cluster().prepareHealth().execute().actionGet();
        return response.toXContent(JsonXContent.contentBuilder().startObject(), EMPTY_PARAMS).endObject();
    }

    public XContentBuilder stats() throws IOException {
        IndicesStatsResponse response = context.getClient().admin().indices().prepareStats().execute().actionGet();
        return response.toXContent(JsonXContent.contentBuilder().startObject(), EMPTY_PARAMS).endObject();
    }

    public XContentBuilder nodes() throws IOException {
        NodesInfoResponse response = context.getClient().admin().cluster().prepareNodesInfo().execute().actionGet();
        XContentBuilder builder = JsonXContent.contentBuilder().startArray();

        for (NodeInfo nodeInfo : response.getNodes()) {
            builder.startObject().field("name", nodeInfo.getNode().name(), XContentBuilder.FieldCaseConversion.NONE);
            builder.field("transport_address", nodeInfo.getNode().address().toString());
            builder.field("host", nodeInfo.getNode().getHostName(), XContentBuilder.FieldCaseConversion.NONE);
            builder.field("ip", nodeInfo.getNode().getHostAddress(), XContentBuilder.FieldCaseConversion.NONE);
            builder.field("version", nodeInfo.getVersion());
            builder.field("build", nodeInfo.getBuild().hashShort()).endObject();
        }
        return builder.endArray();
    }

    public XContentBuilder os(String node) throws IOException {
        SearchResponse response = context.getClient()
                .prepareSearch("$system").setTypes("monitor").setQuery(QueryBuilders.termQuery("node", node)).setSize(30).addSort("timestamp", SortOrder.DESC).execute().actionGet();
        XContentBuilder builder = JsonXContent.contentBuilder().startArray();
        for (SearchHit searchHit : response.getHits().getHits()) {
            Map<String, Object> source = searchHit.sourceAsMap();
            Map<String, Object> os = (Map<String, Object>) source.get("os");
            Map<String, Object> os_cpu = (Map<String, Object>) os.get("cpu");
            Map<String, Object> os_mem = (Map<String, Object>) os.get("mem");
            Map<String, Object> jvm = (Map<String, Object>) source.get("jvm");
            Map<String, Object> jvm_mem = (Map<String, Object>) jvm.get("mem");

            builder.startObject()
                    .field("node", source.get("node"))
                    .field("timestamp", source.get("timestamp"))
                    .field("os_cpu_usage", os_cpu.get("usage"))
                    .field("os_mem_usage", os_mem.get("used_percent"))
                    .field("jvm_mem_usage", jvm_mem.get("heap_used_percent"))
                    .endObject();
        }
        builder.endArray();
        return builder;
    }

    private static class EmptyParams extends ToXContent.MapParams {
        public EmptyParams() {
            super(Collections.emptyMap());
        }
    }

}
