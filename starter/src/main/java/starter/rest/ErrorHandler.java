package starter.rest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import starter.uContentException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice(annotations = RestController.class)
public class ErrorHandler {

    private Logger logger = Logger.getLogger(ErrorHandler.class);

    @ExceptionHandler(uContentException.class)
    @ResponseBody
    public String handle(uContentException ex, HttpServletResponse response) throws IOException {
        logger.error(ex.getMessage(),ex);
        XContentBuilder result = JsonXContent.contentBuilder();
        result.startObject().
                field("reason", ex.getMessage()).
                field("status", ex.getStatusCode()).
                endObject();
        response.setStatus(500);
        response.setContentType("application/json;charset=UTF-8");
        return result.string();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String allException(Exception ex, HttpServletResponse response) throws IOException {
        logger.error(ex.getMessage(),ex);
        XContentBuilder result = JsonXContent.contentBuilder();
        result.startObject().
                field("reason", ex.getMessage()).
                field("status", 500).
                field("stack", ExceptionUtils.getStackTrace(ex)).
        endObject();
        response.setStatus(500);
        response.setContentType("application/json;charset=UTF-8");
        return result.string();
    }
    
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public String allThrowable(Throwable ex, HttpServletResponse response) throws IOException {
        logger.error(ex.getMessage(),ex);
        XContentBuilder result = JsonXContent.contentBuilder();
        result.startObject().
                field("reason", ex.getMessage()).
                field("status", 500).
                field("stack", ExceptionUtils.getStackTrace(ex)).
        endObject();
        response.setStatus(500);
        response.setContentType("application/json;charset=UTF-8");
        return result.string();
    }

}
