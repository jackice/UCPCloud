package starter.service.hrbb;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Created by Administrator on 2016/2/18.
 */
@Service
public class HrbbService {

    private Logger logger = Logger.getLogger(HrbbService.class);

    @Autowired
    private HrbbConfig hrbbConfig;


    public String uploadToNas(String name, List<MultipartFile> files) throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder().startObject();
        if (files.isEmpty()) {
            logger.warn(String.format("the files of %s is empty", name));
            builder.field("result", false)
                    .field("reason", String.format("the files of %s is empty", name))
                    .endObject();
            return builder.string();
        }
        String subName = "";
        String directory = "";
        try {
            directory = getDirectory(name);
            for(MultipartFile file : files){
                subName = Base64.getEncoder().encodeToString(file.getOriginalFilename().getBytes("UTF-8"));
                File dest = new File(FilenameUtils.concat(directory, subName));
                if (dest.exists() && dest.isFile()) {
                    dest.delete();
                }
                FileUtils.writeByteArrayToFile(dest, file.getBytes());
                logger.info(String.format("subFile %s of %s upload to Nas successful. path is %s", subName, name, dest));
            }
            builder.field("result", true)
                    .field("reason", "")
                    .endObject();
            return builder.string();
        } catch (IOException e) {
            logger.error(e.getMessage());
            FileUtils.cleanDirectory(new File(directory));
            builder.field("result", false)
                    .field("reason", e.getMessage())
                    .endObject();
            return builder.string();
        }
    }

    public byte[] downLoadFromNas(String name, String subName){
        if (StringUtils.isBlank(name) || StringUtils.isBlank(subName)) {
            logger.warn("both args can not be null");
            throw new IllegalArgumentException("both args can not be null");
        }
        try {
            String directory = getDirectory(name);
            subName = Base64.getEncoder().encodeToString(subName.getBytes("UTF-8"));
            return FileUtils.readFileToByteArray(new File(FilenameUtils.concat(directory, subName)));
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String deleteFromNas(String name) throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder().startObject();
        if (StringUtils.isBlank(name)) {
            logger.warn(String.format("args: %s can not be null", name));
            builder.field("result", false)
                    .field("reason", String.format("args: %s can not be null", name))
                    .endObject();
            return builder.string();
        }
        try {
            File directory = new File(getDirectory(name));
            FileUtils.cleanDirectory(directory);
            builder.field("result", true)
                    .field("reason", "")
                    .endObject();
            return builder.string();
        } catch (Throwable e) {
            logger.error(e.getMessage());
            builder.field("result", false)
                    .field("reason", e.getMessage())
                    .endObject();
            return builder.string();
        }
    }


    private String getDirectory(String name) throws IOException {
        String crc32 = checksumCRC32(name.getBytes());
        String first2 = crc32.substring(0, 2);
        String first4 = crc32.substring(2, 4);
        return FilenameUtils.concat(FilenameUtils.concat(hrbbConfig.getTempPath(), first2), first4);
    }

    public static String checksumCRC32(byte[] buffer) throws IOException {
        CRC32 crc = new CRC32();
        crc.update(buffer);
        return String.format("%08X", crc.getValue());
    }


}
