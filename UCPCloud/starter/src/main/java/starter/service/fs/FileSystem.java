package starter.service.fs;

import java.io.IOException;

public interface FileSystem {

    //String write(byte[] bytes) throws IOException;
    
    String write(String type, byte[] bytes) throws IOException;

    byte[] read(String fileId);


}
