package starter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import starter.service.ReIndexConfig;
import starter.service.TikaConfig;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {

    @Autowired
    private Client client;

    @Autowired
    private EsConfig esConfig;

    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private TikaConfig tikaConfig;

    @Autowired
    private ReIndexConfig reIndexConfig;
    
    @Autowired
	private DefaultUserBindingConfig defaultUserBindingConfig;

    /**
     * 所有业务类型的别名
     *
     * @return
     */
    public String getAlias() {
        return esConfig.getAlias();
    }

    public Client getClient() {
        return client;
    }

    public HttpServletRequest getRequest() {
        return request;
    }
    
    public HttpServletResponse getResponse() {
        return response;
    }

    public String getUserName() {
        return this.request.getUserPrincipal().getName();
    }
    
    public String getAdaptUserName() {
    	if (this.request.getUserPrincipal()==null) {
    		ArrayList<DefaultUserMappingConfig> defaultUserMappingConfigs = defaultUserBindingConfig.getDefaultUserMappingConfig();
    		for (DefaultUserMappingConfig defaultUserMappingConfig : defaultUserMappingConfigs) {
				List<String> interfaces = defaultUserMappingConfig.getInterfaces();
			 return interfaces.contains(this.request.getRequestURI())?defaultUserMappingConfig.getUserName():null;
			}
    	} 
        return this.request.getUserPrincipal().getName();
    }

    public String[] getIndices() {
        return client.admin().indices().prepareGetIndex().setIndices(getAlias()).execute().actionGet().indices();
    }

    public String m(String key) {
        return messageSource.getMessage(key, null, request.getLocale());
    }

    public TikaConfig getTikaConfig() {
        return tikaConfig;
    }

    public ReIndexConfig getReIndexConfig() {
        return reIndexConfig;
    }
}
