package starter.rest.hrbb;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import starter.service.hrbb.HrbbService;
import starter.uContentException;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/2/18.
 */
@RestController
@RequestMapping(value = "/svc/hrbb", produces = MediaType.APPLICATION_JSON_VALUE)
public class HrbbRest {

    @Autowired
    private HrbbService imageService;

    @RequestMapping(value = "/{name}", method = RequestMethod.POST, consumes = "multipart/*")
    public String uploadToNas(@PathVariable String name, MultipartHttpServletRequest request) {
        List<MultipartFile> files = new ArrayList<>();
        Map<String, MultipartFile> fileMap = request.getFileMap();
        for (String key : fileMap.keySet()) {
            for(MultipartFile file : request.getFiles(key)){
                if (!file.isEmpty()) {
                    files.add(file);
                }
            }
        }
        try {
            return imageService.uploadToNas(name, files);
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{name}/{subName}", method = RequestMethod.GET, produces = MediaType.ALL_VALUE)
    public void downLoadFromNas(@PathVariable String name, @PathVariable String subName, HttpServletResponse response) {
        InputStream stream = null;
        try {
            byte[] bytes = imageService.downLoadFromNas(name, subName);
            response.setContentType("application/octet-stream");
            stream = new ByteArrayInputStream(bytes);
            IOUtils.copy(stream, response.getOutputStream());
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
    public String deleteFromNas(@PathVariable String name) {
        try {
            return imageService.deleteFromNas(name);
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
