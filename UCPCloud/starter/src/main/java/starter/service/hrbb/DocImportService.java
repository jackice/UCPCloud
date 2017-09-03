package starter.service.hrbb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.exists.ExistsResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import starter.rest.Json;
import starter.service.Constant;
import starter.service.DocumentService;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by wbpanwei on 2016/3/10.
 */
@Service
public class DocImportService {

    @Autowired
    private HrbbConfig hrbbConfig;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private Client client;

    @Autowired
    private Cache cache;

    private Logger logger = Logger.getLogger(DocImportService.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    public XContentBuilder uploadZip(List<MultipartFile> zips) throws IOException {
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startArray();
        for (MultipartFile zip : zips) {
            String root = getRoot();
            xContentBuilder.startObject();
            try{
                unZip(zip, root);//解压ZIP
            } catch (IOException e){
                logger.error(e.getMessage());
                xContentBuilder.field("zipId", zip.getName())
                        .field("success", false)
                        .field("msg", String.format("unzip %s failed" + zip.getName()))
                        .endObject();
                continue;
            }

            File file = new File(FilenameUtils.concat(root, "metadata.txt"));
            String jsonString = null;
            try {
                jsonString = FileUtils.readFileToString(file, "UTf-8");
            } catch (IOException e) {
                logger.error(e.getMessage());
                xContentBuilder.field("zipId", zip.getName())
                        .field("success", false)
                        .field("msg", e.getMessage())
                        .endObject();
                continue;
            }
            logger.info("upload json is:\r\n" + jsonString);
            try {
                List<Map<String, Object>> json = objectMapper.readValue(jsonString, List.class);
                if (json == null || json.isEmpty()) {
                    logger.error("the metadata is null");
                    xContentBuilder.field("zipId", zip.getName())
                            .field("success", false)
                            .field("msg", "the metadata is null")
                            .endObject();
                    continue;
                }

                List<Map<String, Object>> docs = new ArrayList<>();
                for (Map<String, Object> body : json) {
                    Json doc = null;
                    // type必须指定
                    String type = (String) body.get("_type");
                    if (StringUtils.isBlank(type)) {
                        if (doc == null) {
                            doc = Json.parse(body);
                        }
                        xContentBuilder.field("zipId", zip.getName())
                                .field("success", false)
                                .field("msg", "_type is required")
                                .field("doc", doc.toXContentBuilder().string())
                                .endObject();
                        docs.clear();
                        break;
                    }

                    //检查文档类型是否存在
                    Arrays.asList(cache.typeIds());

                    // 引用的图片文件必须存在
                    String streams = (String) body.get("_streams");
                    if (StringUtils.isNotBlank(streams)) {
                        File f = new File(FilenameUtils.concat(root, streams));
                        if (!f.exists()) {
                            if (doc == null) {
                                doc = Json.parse(body);
                            }
                            xContentBuilder.field("zipId", zip.getName())
                                    .field("success", false)
                                    .field("msg", String.format("file: %s is not exist", streams))
                                    .field("doc", doc.toXContentBuilder().string())
                                    .endObject();
                            docs.clear();
                            break;
                        }
                    }
                    String id = (String) body.get("_id");
                    String opType = (String) body.get("_opType");
                    if (!(StringUtils.isBlank(opType) || "create".equals(opType) || "update".equals(opType) || "delete".equals(opType))) {
                        if (doc == null) {
                            doc = Json.parse(body);
                        }
                        xContentBuilder.field("zipId", zip.getName())
                                .field("success", false)
                                .field("msg", String.format("the opType %s is invalid", opType))
                                .field("doc", doc.toXContentBuilder().string())
                                .endObject();
                        docs.clear();
                        break;
                    }

                    if ("update".equals(opType) || "delete".equals(opType)) {
                        if (StringUtils.isBlank(id)) {
                            if (doc == null) {
                                doc = Json.parse(body);
                            }
                            xContentBuilder.field("zipId", zip.getName())
                                    .field("success", false)
                                    .field("msg", String.format("the _id is required when opType is %s ", opType))
                                    .field("doc", doc.toXContentBuilder().string())
                                    .endObject();
                            docs.clear();
                            break;
                        }
                        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery(type).ids(id);
                        ExistsResponse existsResponse = client.prepareExists(type).setQuery(idsQueryBuilder).get();
                        if (!existsResponse.exists()) {
                            if (doc == null) {
                                doc = Json.parse(body);
                            }
                            xContentBuilder.field("zipId", zip.getName())
                                    .field("success", false)
                                    .field("msg", String.format("the doc %s is not exist", id))
                                    .field("doc", doc.toXContentBuilder().string())
                                    .endObject();
                            docs.clear();
                            break;
                        }
                    }

                    try {// 校验
                        documentService.formatAcl(body);
                        documentService.validate(body, type);
                    } catch (Throwable e) {
                        xContentBuilder.field("zipId", zip.getName())
                                .field("success", false)
                                .field("msg", e.getMessage())
                                .endObject();
                        docs.clear();
                        logger.error(e.getMessage());
                        break;
                    }
                    body.put("_type", type);
                    body.put("_opType", opType);
                    body.put("_id", id);
                    docs.add(body);
                }


                if (!docs.isEmpty()) {
                    //do bulk insert
                    BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
                    for (Map<String, Object> doc : docs) {
                        Object _streams = doc.get("_streams");
                        String type = (String) doc.get("_type");
                        if (_streams != null && StringUtils.isNotBlank(_streams.toString())) {
                            List<Map<String, Object>> streams = new ArrayList<>();
                            String s = _streams.toString();
                            Map<String, Object> stream = documentService.uploadStream(type,new HrbbFile(new File(FilenameUtils.concat(root, s))));
                            streams.add(stream);
                            doc.put(Constant.FieldName.STREAMS, streams);
                        }
                     
                        doc.remove("_type");
                        String id = (String) doc.get("_id");
                        doc.remove("_id");
                        String opType = (String) doc.get("_opType");
                        doc.remove("_opType");
                        if (StringUtils.isBlank(opType) || "create".equals(opType)) {
                            documentService.beforeCreate(doc);
                            IndexRequest indexRequest = new IndexRequest(type, type).source(doc);
                            if (StringUtils.isNotBlank(id)) {
                                indexRequest.id(id.toString());
                            }
                            logger.info("create:\r\n" + Json.parse(doc).toXContentBuilder().string());
                            bulkRequestBuilder.add(indexRequest);
                        }else {
                            if ("delete".equals(opType)) {
                                doc.remove("_acl");
                                documentService.addAdminGrouptoDoc(doc);
                            }
                            documentService.beforeUpdate(doc);
                            logger.info(opType + ":\r\n" + Json.parse(doc).toXContentBuilder().string());
                            UpdateRequest updateRequest = new UpdateRequest(type, type, id).doc(doc);
                            bulkRequestBuilder.add(updateRequest);
                        }
                    }

                    BulkResponse response = bulkRequestBuilder.get();
                    xContentBuilder.field("zipId", zip.getName())
                            .field("success", !response.hasFailures())
                            .field("msg", response.hasFailures() == true ? response.buildFailureMessage() : "")
                            .endObject();
                }
            } catch (Throwable e) {
                logger.error(e);
                xContentBuilder.field("zipId", zip.getName())
                        .field("success", false)
                        .field("msg", e.getMessage())
                        .endObject();
            } finally {
                clean(root);
            }
        }
        xContentBuilder.endArray();
        logger.info(xContentBuilder.string());
        return xContentBuilder;
    }


    private String getRoot() {
        return FilenameUtils.concat(hrbbConfig.getUploadTempPath(), UUID.randomUUID().toString());
    }

    private void unZip(MultipartFile zip, String path) throws IOException{
        InputStream is = null;
        logger.info("start unzip " + zip.getName());
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        is = zip.getInputStream();
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            FileOutputStream fos = new FileOutputStream(new File(FilenameUtils.concat(directory.getAbsolutePath(), zipEntry.getName())));
            IOUtils.copy(zipInputStream, fos);
            IOUtils.closeQuietly(fos);
        }
        logger.info("unzip success");
        IOUtils.closeQuietly(is);
    }


    private void clean(String directoryPath) {
        File directory = new File(directoryPath);
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

}
