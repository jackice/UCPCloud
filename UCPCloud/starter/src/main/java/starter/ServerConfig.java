package starter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Modification History:
 * =============================================================================
 * Author         Date          Description
 * ------------ ---------- ---------------------------------------------------
 * JackIce       2017/9/2
 * =============================================================================
 */
@Component
@ConfigurationProperties(prefix = "udserver")
public class ServerConfig {
    private final int DEFAULT_HTTP_PORT=8080;
    @Value("8080")
    private int httpPort;
    @Value("443")
    private int httpsPort;
    private String sslKeyAlias;
    private String keyStorePassword;
    private String keyStore;
    private String keyStoreType;
    private String uriEncode;
    @Value("2000")
    private int maxConnections;
    @Value("2000")
    private int maxThreads;
    @Value("30000")
    private int connectionTimeOut;

    public boolean httpPortIsDefault() {
        if (this.DEFAULT_HTTP_PORT==this.httpPort){
            return true;
        }
        return false;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getSslKeyAlias() {
        return sslKeyAlias;
    }

    public void setSslKeyAlias(String sslKeyAlias) {
        this.sslKeyAlias = sslKeyAlias;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getUriEncode() {
        return uriEncode;
    }

    public void setUriEncode(String uriEncode) {
        this.uriEncode = uriEncode;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "httpPort=" + httpPort +
                ", httpsPort=" + httpsPort +
                ", sslKeyAlias='" + sslKeyAlias + '\'' +
                ", keyStorePassword='" + keyStorePassword + '\'' +
                ", keyStore='" + keyStore + '\'' +
                ", keyStoreType='" + keyStoreType + '\'' +
                ", uriEncode='" + uriEncode + '\'' +
                ", maxConnections=" + maxConnections +
                ", maxThreads=" + maxThreads +
                ", connectionTimeOut=" + connectionTimeOut +
                '}';
    }
}
