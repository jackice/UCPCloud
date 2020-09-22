package starter.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2016/1/12.
 */
@Component
@ConfigurationProperties(prefix = "tika")
public class TikaConfig {

    private String mediaType;

    private int maxStringLength;

    private Set<String> set = new HashSet<>();

    public Set<String> getMediaTypes() {
        return set;
    }

    public void setMediaType(String mediaType) {
        if (StringUtils.isNotBlank(mediaType)) {
            String[] split = mediaType.split(",");
            for(String s : split){
                if (StringUtils.isNotBlank(s)) {
                    set.add(s);
                }
            }
        }
    }

    public int getMaxStringLength() {
        return maxStringLength;
    }

    public void setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
    }
}
