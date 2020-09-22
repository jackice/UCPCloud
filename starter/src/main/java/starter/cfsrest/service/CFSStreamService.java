package starter.cfsrest.service;


import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import starter.RequestContext;
import starter.uContentException;
import starter.service.Constant;
import starter.service.fs.FileSystem;

@Service
public class CFSStreamService {

    @Autowired
    private RequestContext context;

    @Autowired
    private FileSystem fs;

    @Autowired
    private CFSCommonService documentService;

    private Logger logger = Logger.getLogger(CFSStreamService.class);
   /*
    * 获取文件输出流
    * type 类型
    * id 文件id
    * streamid 流id
    */
    public Map<String, Object> getStream(String type, String id, String streamId) throws IOException {
    	String userName = null;
    	try {
    		userName = context.getUserName();
		} catch (Exception e) {
			logger.info("userName is null");
		}
    	GetResponse getResponse = documentService.checkPermission(type, id, userName, Constant.Permission.read);
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
/*
 * 获取流id
 * type 类型
 * id 文件id
 */
    public XContentBuilder get(String type, String id) throws IOException {
    	
    	String userName = null;
    	try {
    		userName = context.getUserName();
		} catch (Exception e) {
			logger.info("userName is null");
		}
        GetResponse getResponse = documentService.checkPermission(type, id, userName, Constant.Permission.read);
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
    
}
