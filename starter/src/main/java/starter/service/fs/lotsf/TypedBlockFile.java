package starter.service.fs.lotsf;

import java.util.Base64;

public class TypedBlockFile {
	//相对于fs.root的目录
    private String name;
    private long position;
    private long length;


    public TypedBlockFile() {
    }


    public TypedBlockFile(String name, long position, long length) {
        this.name = name;
        this.position = position;
        this.length = length;
    }

    public static TypedBlockFile valueOf(String s) {
        String fileId = new String(Base64.getDecoder().decode(s));
        String[] parts = fileId.split(",");
        return new TypedBlockFile(parts[0], Long.valueOf(parts[1]), Long.valueOf(parts[2]));
    }
    
    public String getFileId(){
        String fileId = String.format("%s,%d,%d", name, position, length);
        return new String(Base64.getEncoder().encode(fileId.getBytes()));
    }

    @Override
    public String toString() {
        return getFileId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getPosition() {
        return position;
    }

    public long getLength() {
        return length;
    }
}
