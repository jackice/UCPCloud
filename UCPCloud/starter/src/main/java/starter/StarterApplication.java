package starter;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.commons.lang.StringUtils;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import starter.service.fs.FileSystem;
import starter.service.fs.FileSystemFactory;
import starter.service.fs.FsConfig;

import java.io.File;
import java.net.InetSocketAddress;
/**
 * Modification History:
 * =============================================================================
 * Author         Date          Description
 * ------------ ---------- ---------------------------------------------------
 * JackIce      2017-09-02      添加ssl配置
 * =============================================================================
 */

@SpringBootApplication
@EnableScheduling
public class StarterApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(StarterApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(StarterApplication.class, args);
    }

    @Bean
    public FileSystem fileSystem(Client client, FsConfig fsConfig) {
        FileSystemFactory fileSystemFactory = new FileSystemFactory(client);
        return fileSystemFactory.newFileSystem(fsConfig);
    }

    @Bean
    public Client client(EsConfig esConfig) {
        //Settings settings = Settings.builder().put("cluster.name", esConfig.getCluster()).build();
        Settings settings = Settings.builder().put("cluster.name", esConfig.getCluster()).build();
        TransportClient client = TransportClient.builder().settings(settings).build();
        for (String host : esConfig.getHosts()) {
            String[] parts = host.split(":");
            client.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(parts[0], Integer.parseInt(parts[1]))));
        }
        return client;

//        Settings settings = Settings.builder().put("cluster.name", esConfig.getCluster()).put("http.enabled", false).put("path.home", "").build();
//        Node node = NodeBuilder.nodeBuilder().clusterName(esConfig.getCluster()).client(true).node();
//        return node.client();
    }


    @Bean
    public MultipartResolver MultipartResolverInstance() {
        return new NkoMultipartResolver();
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer(ServerConfig config) {
        System.out.println(config.toString());
        System.out.println("设置内嵌tomcat");
        //http 强制跳转到https时需要
//        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
//            SecurityConstraint securityConstraint = new SecurityConstraint();
//        securityConstraint.setUserConstraint("CONFIDENTIAL");
//            SecurityCollection collection = new SecurityCollection();
//        collection.addPattern("/");
//        securityConstraint.addCollection(collection);
//        context.addConstraint(securityConstraint);
//        };
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        tomcat.setUriEncoding(config.getUriEncode());
//        if (!config.httpPortIsDefault()){
//            tomcat.addAdditionalTomcatConnectors(httpConnector(config));
//        }
        tomcat.setPort(config.getHttpPort());
//        tomcat.addAdditionalTomcatConnectors(httpConnector(config));
        tomcat.addAdditionalTomcatConnectors(createSslConnector(config));
//        tomcat.addConnectorCustomizers(new StarterApplication.MyTomcatConnectorCustomizer(config.getMaxConnections(),
//                config.getMaxThreads(),config.getConnectionTimeOut()));
        return tomcat;
    }
    class MyTomcatConnectorCustomizer implements TomcatConnectorCustomizer
    {
        private int maxConnections;
        private int maxThreads;
        private int connectionTimeOut;

        public MyTomcatConnectorCustomizer(int maxConnections, int maxThreads, int connectionTimeOut) {
            this.maxConnections = maxConnections;
            maxThreads = maxThreads;
            this.connectionTimeOut = connectionTimeOut;
        }

        public void customize(Connector connector)
        {
            Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
            //设置最大连接数
            System.out.println("setting maxconnection:"+this.maxConnections);
            protocol.setMaxConnections(this.maxConnections);
            //设置最大线程数
            protocol.setMaxThreads(this.maxThreads);
            protocol.setConnectionTimeout(this.connectionTimeOut);
        }
    }
    private Connector createSslConnector(ServerConfig config) {
        /**
        * method_name: createSslConnector
        * param: [config]
        * describe: TODO
        * creat_user: JackIce
        * creat_date: 2017/9/3
        * creat_time: 1:33
        **/
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        try {
            File keyStore = new File(config.getKeyStore());
            File trustStore = new File(config.getTrustStore());
            connector.setScheme("https");
            protocol.setSslProtocol("TLS");
            protocol.setClientAuth("true");
            protocol.setSSLEnabled(true);
            connector.setSecure(true);
            System.out.println("setting httpsport:"+config.getHttpsPort());
            connector.setPort(config.getHttpsPort());
            protocol.setTruststoreFile(trustStore.getAbsolutePath());
            protocol.setTruststorePass(config.getTrustStorePassword());
            protocol.setTruststoreType(config.getTrustStoreType());
            protocol.setKeystoreFile(keyStore.getAbsolutePath());
            protocol.setKeystorePass(config.getKeyStorePassword());
            protocol.setKeystoreType(config.getKeyStoreType());
            protocol.setKeyAlias(config.getSslKeyAlias());
            return connector;
        } catch (Exception ex) {
            throw new IllegalStateException("cant access keystore: [" + "keystore" + "]  ", ex);
        }
    }

    private Connector httpConnector(ServerConfig config){
        /**
        * method_name: httpConnector
        * param: [config]
        * describe: 转发端口
        * creat_user: JackIce
        * creat_date: 2017/9/3
        * creat_time: 1:34
        **/
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(config.getHttpPort());
        System.out.println("setting httpport:"+config.getHttpPort());
        connector.setRedirectPort(config.getHttpsPort());
        connector.setSecure(false);
        return connector;
    }

}
