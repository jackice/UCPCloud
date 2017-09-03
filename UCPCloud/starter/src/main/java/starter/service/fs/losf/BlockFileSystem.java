package starter.service.fs.losf;


import org.apache.commons.io.FilenameUtils;

import starter.service.fs.FileSystem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlockFileSystem implements FileSystem {

    private final String root;
    private final long MAX_SIZE;
    private BlockFileWriter writer;
    private Map<String, BlockFileReader> readers = new HashMap<String, BlockFileReader>();
    private final String nodeName;

    public BlockFileSystem(String name, String root, long blocksize) {
        nodeName = name;
        this.root = root;
        MAX_SIZE = blocksize;
        File dir = new File(this.root);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (File file : dir.listFiles()) {
            if (file.getName().startsWith("LOSF_" + nodeName + "_") && file.length() < MAX_SIZE) {
                writer = new BlockFileWriter(file.getAbsolutePath());
            }
        }
    }

    public String write(String type,byte[] bytes) {
        try {
            allocateBlockWriter();
            return writer.append(bytes).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] read(String fileId) {
        try {
            BlockFile blockFile = BlockFile.valueOf(fileId);
            BlockFileReader reader = allocateBlockReader(blockFile);
            return reader.read(blockFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void allocateBlockWriter() throws IOException {
        if (writer == null || writer.position() > MAX_SIZE) {
            writer = new BlockFileWriter(nameBlock());
            return;
        }

    }

    private BlockFileReader allocateBlockReader(BlockFile blockFile) throws IOException {
        if (!readers.containsKey(blockFile.getName())) {
            BlockFileReader reader = new BlockFileReader(nameBlock(blockFile.getName()));
            readers.put(blockFile.getName(), reader);
        }
        return readers.get(blockFile.getName());
    }


    private String nameBlock() {
        return FilenameUtils.concat(root, "LOSF_" + nodeName + "_" + System.currentTimeMillis());
    }

    private String nameBlock(String name) {
        return FilenameUtils.concat(root, name);
    }



}
