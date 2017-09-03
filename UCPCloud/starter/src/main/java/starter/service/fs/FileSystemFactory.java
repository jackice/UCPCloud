package starter.service.fs;


import org.elasticsearch.client.Client;

import starter.service.fs.local.LocalFileSystem;
import starter.service.fs.losf.BlockFileSystem;
import starter.service.fs.lotsf.TypedBlockFileSystem;

public class FileSystemFactory {

    private final Client client;

    public FileSystemFactory(Client client) {
        this.client = client;
    }

    public FileSystem newFileSystem(FsConfig config) {
        // String fs = Configuration.UC_PROPERTIES.getProperty("fs", "local");
        String fs = config.getType();
        if ("local".equalsIgnoreCase(fs)) {
            return new LocalFileSystem(config.getRoot());
        } else if ("losf".equalsIgnoreCase(fs)) {
            return new BlockFileSystem(client.settings().get("name"), config.getRoot(), config.getBlocksize());
        }else if ("lotsf".equalsIgnoreCase(fs)) {
            return new TypedBlockFileSystem(client.settings().get("name"), config.getRoot(), config.getBlocksize());
        } else if ("s3".equalsIgnoreCase(fs)) {
            //return new S3FileSystem(config.getAccessKey(), config.getSecretKey(), config.getBucketName(), config.getEndpoint());
        }
        return null;
    }

}
