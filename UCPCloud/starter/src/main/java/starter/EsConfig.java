package starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "es")
public class EsConfig {
    private List<String> hosts = new ArrayList<>();

    private String cluster;

    private String alias;

    private int shards;

    private int replicas;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public List<String> getHosts() {
        System.out.print(hosts);
        return hosts;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String index) {
        this.alias = index;
    }

    public int getShards() {
        return shards;
    }

    public void setShards(int shards) {
        this.shards = shards;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }
}
