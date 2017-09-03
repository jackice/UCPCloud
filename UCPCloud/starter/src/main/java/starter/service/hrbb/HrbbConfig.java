package starter.service.hrbb;


import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@ConfigurationProperties(prefix = "hrb")
@Service
public class HrbbConfig {

    private String tempPath;

    private String uploadTempPath;

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
        //this method takes up space and time,so not used;update 20161012 by yangbin
//        for (int i = 0; i < 256; i++) {
//            String level1 = FilenameUtils.concat(tempPath, String.format("%02X", i));
//            for (int j = 0; j < 256; j++) {
//                File level2 = new File(FilenameUtils.concat(level1, String.format("%02X", j)));
//                boolean res = level2.mkdirs();
//                if (!res) return;
//            }
//        }
    }

    public String getTempPath() {
        return tempPath;
    }

    public String getUploadTempPath() {
        return uploadTempPath;
    }

    public void setUploadTempPath(String uploadTempPath) {
        this.uploadTempPath = uploadTempPath;
        File dir = new File(uploadTempPath);
        if (dir.exists() && dir.isFile()) {
            throw new RuntimeException(String.format("the path: %s is not a directory", dir));
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
