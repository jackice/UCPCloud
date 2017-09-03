package starter.service.fs.lotsf;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TypedBlockFileReader implements Closeable {

    private final String path;
    private final FileInputStream fs;
    private final FileChannel fc;

    public TypedBlockFileReader(String path) {
        this.path = path;
        try {
            fs = FileUtils.openInputStream(new File(path));
            fc = fs.getChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] read(TypedBlockFile innerFile) throws IOException {
        ByteBuffer dst = ByteBuffer.allocate((int) innerFile.getLength());
        fc.read(dst, innerFile.getPosition());
        fc.position();
        return dst.array();
    }


    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(fc);
        IOUtils.closeQuietly(fs);
    }
}
