package starter.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.http.HttpStatus;
import starter.uContentException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Json extends HashMap<String, Object> {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public Json() {
    }

    public static Json parse(Map map) {
        Json json = new Json();
        json.putAll(map);
        return json;
    }

    public static Json parse(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Json.class);
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static Map parseToMap(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, Map.class);
    }


    public XContentBuilder toXContentBuilder() throws IOException {
        if (this == null) {
            return null;
        }
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject();
        Iterator<Entry<String, Object>> it = this.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Object> entry = it.next();
            builder.field(entry.getKey()).value(entry.getValue());
        }
        builder.endObject();
        return builder;
    }

    //将Json集合转换为大Json
    public static XContentBuilder parse(List<Json> list, String arrayName) throws IOException {
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject();
        if (list == null||list.size()==0) {
            builder.field("total", 0);
            builder.startArray(arrayName);
        }else{
            builder.field("total", list.size());
            builder.startArray(arrayName);
            for(Json json:list){
                builder.startObject();
                Iterator<Entry<String, Object>> it = json.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, Object> entry = it.next();
                    builder.field(entry.getKey()).value(entry.getValue());
                }
                builder.endObject();
            }
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

}
