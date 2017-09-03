package starter.rest;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import starter.service.DocumentService;
import starter.service.hrbb.DocImportService;
import starter.FileSizeLimit;
import starter.uContentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;

@RestController
@RequestMapping(value = "/svc", produces = MediaType.APPLICATION_JSON_VALUE)
public class Documents {
	
	@Autowired
	private ServletContext context;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocImportService docImportService;
    
    @Autowired
    private FileSizeLimit fileSizeLimit;
    
    @RequestMapping(value = "/{type}", method = {RequestMethod.GET})
    public String query(@PathVariable String type,
                        @RequestParam(defaultValue = "") String query,
                        @RequestParam(defaultValue = "false") boolean fulltext,
                        @RequestParam(defaultValue = "0") int start,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam(defaultValue = "[]") SortBuilder[] sort,
                        @RequestParam(defaultValue = "false") boolean allowableActions,
                        @RequestParam(defaultValue = "true") boolean containsType
    ) {
        try {
            query = java.net.URLDecoder.decode(query, "UTF-8");
            String[] types = {type};
            XContentBuilder xDocument = documentService.query(types, query, start, limit, sort, allowableActions, fulltext, containsType);
            return xDocument.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "", method = {RequestMethod.GET})
    public String query(@RequestParam(defaultValue = "") String[] types,
                        @RequestParam(defaultValue = "") String query,
                        @RequestParam(defaultValue = "false") boolean highlight,
                        @RequestParam(defaultValue = "0") int start,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam(defaultValue = "[]") SortBuilder[] sort,
                        @RequestParam(defaultValue = "false") boolean allowableActions,
                        @RequestParam(defaultValue = "true") boolean containsType
    ) {
        try {
            query = java.net.URLDecoder.decode(query, "UTF-8");
            XContentBuilder xContentBuilder = documentService.query(types, query, start, limit, sort, allowableActions, highlight, containsType);
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.POST, consumes = "application/json")
    public String create(@PathVariable String type, @RequestBody Json body) {
        try {
            XContentBuilder result = documentService.create(type, body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @RequestMapping(value = "/{type}", method = RequestMethod.POST, consumes = "multipart/*", produces = MediaType.ALL_VALUE)
    public String create(@PathVariable String type, MultipartHttpServletRequest request) {
        try {
            MultipartParser parser = new MultipartParser(request).invoke(fileSizeLimit);
            XContentBuilder result = documentService.create(type, parser.getBody(), parser.getFiles());
            return result.string();
        } catch (Exception e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.GET)
    public String get(@PathVariable String type,
                      @PathVariable String id,
                      @RequestParam(defaultValue = "false") boolean head,
                      @RequestParam(defaultValue = "false") boolean allowableActions,
                      @RequestParam(defaultValue = "true") boolean containsType) {
        try {
            Json json = documentService.get(type, id, head, allowableActions, containsType);
            return json.toXContentBuilder().string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{id}/_id", method = RequestMethod.GET)
    public String get(@PathVariable String id,
                      @RequestParam(defaultValue = "false") boolean allowableActions,
                      @RequestParam(defaultValue = "true") boolean containsType) {
        try {
            Json json = documentService.get(id, allowableActions,containsType);
            return json.toXContentBuilder().string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @RequestMapping(value = "/{type}/{id}", method = {RequestMethod.POST,RequestMethod.PUT}, consumes = "multipart/*")
    public String update(@PathVariable String type, @PathVariable String id, MultipartHttpServletRequest request) {
        try {
            MultipartParser parser = new MultipartParser(request).invoke(fileSizeLimit);
            XContentBuilder result = documentService.update(type, id, parser.getBody(), parser.getFiles());
            return result.string();
        } catch (Exception e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.PUT, consumes = "application/json")
	public String update(@PathVariable String type, @PathVariable String id, @RequestBody Json body) {
	    try {
	        XContentBuilder xContentBuilder = documentService.update(type, id, body, null);
	        return xContentBuilder.string();
	    } catch (IOException e) {
	        throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	@RequestMapping(value = "/{type}/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable String type, @PathVariable String id) {
        try {
            XContentBuilder xContentBuilder = documentService.delete(type, id);
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public String delete(@RequestBody List<Map> body) {
        XContentBuilder xContentBuilder = documentService.delete(body);
        try {
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/{type}/_count", method = RequestMethod.GET)
    public String count(@PathVariable String type) {
        try {
            XContentBuilder xContentBuilder = documentService.count(type);
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = "multipart/*")
    public String uploadZip(MultipartHttpServletRequest request) {
        List<MultipartFile> zips = new ArrayList<>();
        Map<String, MultipartFile> fileMap = request.getFileMap();
        for (String name : fileMap.keySet()) {
            for(MultipartFile file : request.getFiles(name)){
                if (!file.isEmpty()) {
                    zips.add(file);
                }
            }
        }
        try {
            XContentBuilder xContentBuilder = docImportService.uploadZip(zips);
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
