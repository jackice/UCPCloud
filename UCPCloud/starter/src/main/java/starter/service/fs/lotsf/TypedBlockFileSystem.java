package starter.service.fs.lotsf;

import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.asn1.dvcs.Data;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import starter.service.fs.FileSystem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TypedBlockFileSystem implements FileSystem {

	private final String root;
	private final long MAX_SIZE;
	private Map<String, TypedBlockFileReader> readers = new HashMap<String, TypedBlockFileReader>();
	private Map<String, TypedBlockFileWriter> writers = new HashMap<String, TypedBlockFileWriter>();
	private final String nodeName;

	public TypedBlockFileSystem(String name, String root, long blocksize) {
		nodeName = name;
		this.root = root;
		MAX_SIZE = blocksize;
		File dir = new File(this.root);
		if (!dir.exists()) {
			dir.mkdirs();
		}

	}

	public String write(String type, byte[] bytes) {
		try {
			TypedBlockFileWriter writer = getBlockWriter(type);
			TypedBlockFile blockFile=writer.append(bytes);
			return blockFile.getFileId();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] read(String fileId) {
		try {
			TypedBlockFile blockFile = TypedBlockFile.valueOf(fileId);
			TypedBlockFileReader reader = allocateBlockReader(blockFile);
			return reader.read(blockFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private synchronized TypedBlockFileWriter getBlockWriter(String type) throws IOException {
		TypedBlockFileWriter writer=null;
		Date date=new Date();
		String year=new SimpleDateFormat("YYYY").format(date);
		String month=new SimpleDateFormat("MM").format(date);
		String relativePath = String.format("/%s/%s/%s/", type, year, month);
		String absolutePath = String.format("/%s/%s/", root, relativePath);
		type=type+year+month;
		
		if (writers.containsKey(type)&&writers.get(type)!=null&&writers.get(type).position() <MAX_SIZE) {
			writer = writers.get(type);
		}else {
			File dir = new File(absolutePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			for (File file : dir.listFiles()) {
				if (file.getName().startsWith("LOSF_" + nodeName + "_") && file.length() < MAX_SIZE) {
					writer = new TypedBlockFileWriter(relativePath, file.getAbsolutePath());
					writers.put(type, writer);
					break;
				}
			}
		}
		if (writer == null) {
			
			String filename = FilenameUtils.concat(absolutePath, "LOSF_" + nodeName + "_" + System.currentTimeMillis());
			writer = new TypedBlockFileWriter(relativePath, filename);
			writers.put(type, writer);
		} 
		return writer;
	}

	private TypedBlockFileReader allocateBlockReader(TypedBlockFile blockFile) throws IOException {
		if (!readers.containsKey(blockFile.getName())) {
			TypedBlockFileReader reader = new TypedBlockFileReader(nameBlock(blockFile.getName()));
			readers.put(blockFile.getName(), reader);
		}
		return readers.get(blockFile.getName());
	}

	private String nameBlock(String name) {
		return String.format("%s/%s", root, name);
	}

}
