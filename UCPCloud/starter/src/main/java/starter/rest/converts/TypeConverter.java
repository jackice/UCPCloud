package starter.rest.converts;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
public class TypeConverter implements Converter<String, String[]> {

    @Override
    public String[] convert(String source) {
        if (StringUtils.isNotBlank(source)) {
            return source.split(",");
        }
        return new String[0];
    }
}
