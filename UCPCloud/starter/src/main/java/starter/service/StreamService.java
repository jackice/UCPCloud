package starter.service;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import starter.RequestContext;
import starter.service.fs.FileSystem;
import starter.uContentException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.io.IOException;
import java.util.*;

@Service
public class StreamService {

    @Autowired
    private RequestContext context;

    @Autowired
    private FileSystem fs;

    @Autowired
    private DocumentService documentService;

    private Logger logger = Logger.getLogger(StreamService.class);

    public XContentBuilder get(String type, String id) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.read);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startArray();
        Object streams = getResponse.getSource().get(Constant.FieldName.STREAMS);
        if (streams != null) {
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            for (Map<String, Object> map : _streams) {
                xContentBuilder.startObject();
                Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> entry = it.next();
                    xContentBuilder.field(entry.getKey(), entry.getValue());
                }
                xContentBuilder.endObject();
            }
        }
        xContentBuilder.endArray();
        return xContentBuilder;
    }


    public XContentBuilder get(String type, String id, String streamId) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.read);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        Object streams = getResponse.getSource().get(Constant.FieldName.STREAMS);
        if (streams != null) {
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            for (Map<String, Object> map : _streams) {
                if (map.get(Constant.FieldName.STREAMID).toString().equals(streamId)) {
                    Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, Object> entry = it.next();
                        xContentBuilder.field(entry.getKey(), entry.getValue());
                    }
                    break;
                }
            }
        }
        xContentBuilder.endObject();
        return xContentBuilder;
    }


    public Map<String, Object> getStream(String type, String id, String streamId) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.read);
        Object streams = getResponse.getSource().get(Constant.FieldName.STREAMS);
        if (streams != null) {
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            if (StringUtils.isBlank(streamId)) {
                Map<String, Object> map = _streams.get(0);
                byte[] bytes = fs.read(map.get(Constant.FieldName.STREAMID).toString());
                if (bytes == null) {
                    logger.info(String.format("retrieve stream %s from FS failed", streamId));
                    throw new uContentException(String.format(context.m("Stream.StreamRetrieveFailed"), streamId), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                map.put("bytes", bytes);
                return map;
            }else{
                for (Map<String, Object> map : _streams) {
                    if (map.get(Constant.FieldName.STREAMID).toString().equals(streamId)) {
                        byte[] bytes = fs.read(map.get(Constant.FieldName.STREAMID).toString());
                        if (bytes == null) {
                            logger.info(String.format("retrieve stream %s from FS failed", streamId));
                            throw new uContentException(String.format(context.m("Stream.StreamRetrieveFailed"), streamId), HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                        map.put("bytes", bytes);
                        return map;
                    }
                }
            }
        }
        logger.error(String.format("stream %s is not exist", streamId));
        throw new uContentException(String.format(context.m("Stream.StreamNotExist"), streamId), HttpStatus.NOT_FOUND);
    }


    public XContentBuilder delete(String type, String id, List<String> streamIds) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.write);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startObject()
                .field("_index", type.toLowerCase())
                .field("_type", type)
                .field("_id", id);
        Object streams = getResponse.getSource().get(Constant.FieldName.STREAMS);
        long version = getResponse.getVersion();
        if (streams != null) {
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            Iterator<Map<String, Object>> it = _streams.iterator();
            boolean found = false;
            while (it.hasNext()) {
                Map<String, Object> entry = it.next();
                if (streamIds.contains(entry.get(Constant.FieldName.STREAMID).toString())) {
                    it.remove();
                    found = true;
                }
            }
            if (found) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(Constant.FieldName.STREAMS, _streams);
                UpdateResponse updateResponse = context.getClient().prepareUpdate(type.toLowerCase(), type, id).setDoc(map).execute().actionGet();
                version = updateResponse.getVersion();
            }
        }
        xContentBuilder.field("_version", version).endObject();
        return xContentBuilder;
    }


    public XContentBuilder add(String type, String id, Integer order, List<MultipartFile> files) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.write);
        List<Map<String, Object>> newStreams = new ArrayList<Map<String, Object>>();
        for (MultipartFile file : files) {
            Map<String, Object> stream = documentService.uploadStream(type,file);
            newStreams.add(stream);
        }
        Object streams = getResponse.getSource().get(Constant.FieldName.STREAMS);
        List<Map<String, Object>> _streams = null;
        if (streams != null) {
            _streams = (List<Map<String, Object>>) streams;
            if (_streams.size() < order) {
                order = _streams.size();
            }
            _streams.addAll(order, newStreams);
        } else {
            _streams = newStreams;
        }
        Map<String, Object> streamsMap = new HashMap<String, Object>();
        streamsMap.put(Constant.FieldName.STREAMS, _streams);
        UpdateResponse updateResponse = context.getClient().prepareUpdate(type.toLowerCase(), type, id).setDoc(streamsMap).execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        xContentBuilder.field("_index", type.toLowerCase())
                .field("_type", type)
                .field("_id", id)
                .field("_version", updateResponse.getVersion());
        xContentBuilder.endObject();
        return xContentBuilder;
    }

}
