package starter.rest;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starter.service.MonitorService;
import starter.uContentException;

import java.io.IOException;

@RestController
@RequestMapping(value = "cat/", produces = MediaType.APPLICATION_JSON_VALUE)
public class Monitor {

    @Autowired
    private MonitorService monitorService;

    @RequestMapping(value = "state", method = RequestMethod.GET)
    public String state() {
        try {
            XContentBuilder result = monitorService.state();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "health", method = RequestMethod.GET)
    public String health() {
        try {
            XContentBuilder result = monitorService.health();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "stats", method = RequestMethod.GET)
    public String stats() {
        try {
            XContentBuilder result = monitorService.stats();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "nodes", method = RequestMethod.GET)
    public String nodes() {
        try {
            XContentBuilder result = monitorService.nodes();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "os", method = RequestMethod.GET)
    public String os(@RequestParam String node) {
        try {
            XContentBuilder result = monitorService.os(node.toLowerCase());
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
