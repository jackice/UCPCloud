package starter.rest.converts;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import starter.uContentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SortConverter implements Converter<String, SortBuilder[]> {

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public SortBuilder[] convert(String source) {
        try {
            ArrayList<SortBuilder> sorts = new ArrayList<>();
            List<Map<String, String>> list = mapper.readValue(source, List.class);
            for (Map<String, String> map : list) {
                FieldSortBuilder sortBuilder = new FieldSortBuilder(map.get("property"));
                if (map.get("direction").equalsIgnoreCase("desc")) {
                    sortBuilder.order(SortOrder.DESC);
                } else if (map.get("direction").equalsIgnoreCase("asc")) {
                    sortBuilder.order(SortOrder.ASC);
                }
                sorts.add(sortBuilder);
            }
            return sorts.toArray(new SortBuilder[]{});
        } catch (IOException e) {
            throw new uContentException(String.format("can not convert [%s] to SortBuilder Array", source), HttpStatus.BAD_REQUEST);
        }
    }
}