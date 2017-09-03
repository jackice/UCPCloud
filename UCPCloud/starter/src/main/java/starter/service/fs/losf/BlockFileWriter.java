package starter.service.fs.losf;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BlockFileWriter implements Closeable {

    private final String path;
    private final String blockName;
    private final FileOutputStream fs;
    private final FileChannel fc;
    private static final BlockFile NullFile = new BlockFile();


    public BlockFileWriter(String path) {
        this.path = path;
        blockName = FilenameUtils.getName(this.path);
        try {
            fs = FileUtils.openOutputStream(new File(path), true);
            fc = fs.getChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public long position() throws IOException {
        return fc.position();
    }

    public synchronized BlockFile append(byte[] bytes) throws IOException {

//        FileLock lock = fc.tryLock();
//        if (lock == null) return NullFile;
//        try {
            BlockFile innerFile = new BlockFile(blockName, fc.position(), bytes.length);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            fc.write(buffer);
            return innerFile;
//        } finally {
//            lock.release();
//        }
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(fc);
        IOUtils.closeQuietly(fs);
    }


}
