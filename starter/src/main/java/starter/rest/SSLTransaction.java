package starter.rest;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import starter.FileSizeLimit;
import starter.service.DocumentService;
import starter.service.SSLDocuentService;
import starter.uContentException;

import java.io.IOException;

/**
 * Modification History:
 * =============================================================================
 * Author         Date          Description
 * ------------ ---------- ---------------------------------------------------
 * JackIce   2017-09-04        基于ssl安全传输接口定义
 * =============================================================================
 */
@RestController
@RequestMapping(value = "/ssl", produces = MediaType.APPLICATION_JSON_VALUE)
public class SSLTransaction {
    @Autowired

    private SSLDocuentService sslDocumentService;
    @Autowired
    private FileSizeLimit fileSizeLimit;
    @RequestMapping(value = "/{type}", method = RequestMethod.POST, consumes = "multipart/*", produces = MediaType.ALL_VALUE)
    public String create(@PathVariable String type, MultipartHttpServletRequest request) {
        try {
            SSLMultipartParser parser = new SSLMultipartParser(request).invoke(fileSizeLimit);
            XContentBuilder result = sslDocumentService.sslCreate(type, parser.getBody(), parser.getFiles());
            return result.string();
        } catch (Exception e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
