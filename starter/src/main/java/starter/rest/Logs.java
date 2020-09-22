package starter.rest;


import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starter.service.LogService;

import java.io.IOException;

@RestController
@RequestMapping(value = "svc/", produces = MediaType.APPLICATION_JSON_VALUE)
public class Logs {
    Logger logger = LoggerFactory.getLogger(Logs.class);

    @Autowired
    private LogService logService;

    //因故替换参数类型:query改为String，POST改为GET
    //@RequestMapping(value = "logs", method = {RequestMethod.POST}, headers = {"_method=QUERY"})
    //public String query(@RequestBody Json query,
    //                    @RequestParam(defaultValue = "0") int from,
    //                    @RequestParam(defaultValue = "10") int size,
    //                    @RequestParam(defaultValue = "[]") SortBuilder[] sort) {
    //    try {
    //        XContentBuilder result = logService.query(query, from, size, sort);
    //        return result.string();
    //    } catch (IOException e) {
    //        throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
    //    }
    //}

    @RequestMapping(value = "logs", method = {RequestMethod.GET})
    public String query(@RequestParam(defaultValue = "") String query,
                        @RequestParam(defaultValue = "0") int start,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam(defaultValue = "[]") SortBuilder[] sort) {
        try {
            XContentBuilder result = logService.query(query, start, limit, sort);
            return result.string();
        } catch (IOException e) {
            //因求将此处查询异常改为返回空值
            logger.error(String.format("log query failed: %s", e));
            //throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
            return "";
        }
    }
}