package starter.cfsrest.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import starter.uContentException;
import starter.cfsrest.service.CFSStreamService;
import starter.service.Constant;

@RestController
@RequestMapping(value = "cfs/", produces = MediaType.APPLICATION_JSON_VALUE)
public class CFSStreams {
    @Autowired
    private CFSStreamService streamService;
   /*
    * 根据类型type和图片id进行文件流的查询，并在页面获取图片
    */
    @RequestMapping(value = "{type}/{id}/_stream/first", method = RequestMethod.GET, produces = MediaType.ALL_VALUE)
    public void getfirstStream(@PathVariable String type, @PathVariable String id, HttpServletResponse response) {
        InputStream stream = null;
        try {
            Map<String, Object> result = streamService.getStream(type, id, null);
            response.setContentType(result.get(Constant.FieldName.CONTENTTYPE).toString());
            response.setContentLength(Integer.valueOf(result.get(Constant.FieldName.LENGTH).toString()));
            stream = new ByteArrayInputStream((byte[]) result.get("bytes"));
            IOUtils.copy(stream, response.getOutputStream());
        } catch (IOException e) {
            throw new uContentException("获取图片失败", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(Exception e){
        	throw new uContentException("获取图片失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }finally {
            IOUtils.closeQuietly(stream);

        }
    }
}
