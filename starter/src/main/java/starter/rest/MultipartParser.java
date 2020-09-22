package starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import starter.FileSizeLimitConfig;
import starter.FileSizeLimit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.LimitExceededException;

public class MultipartParser {
	private MultipartHttpServletRequest request;
	private Json body;
	private List<MultipartFile> files;
	private ObjectMapper objectMapper = new ObjectMapper();
	private FileSizeLimit limit;
	private static Map<String, Long> typeSizes = new HashMap<String, Long>();

	public MultipartParser(MultipartHttpServletRequest request) {
		this.request = request;
	}

	public Json getBody() {
		return body;
	}

	public List<MultipartFile> getFiles() {
		return files;
	}

	public MultipartParser invoke(FileSizeLimit limit) throws Exception {
		this.limit = limit;
		ExtractMultipart();
		return this;
	}

	public MultipartParser invoke() throws Exception {
		ExtractMultipart();
		return this;
	}

	private void ExtractMultipart() throws LimitExceededException {
		body = new Json();
		files = new ArrayList<>();
		Map<String, MultipartFile> fileMap = request.getFileMap();
		for (String name : fileMap.keySet()) {
			for (MultipartFile file : request.getFiles(name)) {
				//检查文件大小是否超过设置值
				if (limit != null&&limit.getFslConfigs()!=null) {
					long limitsize = getSize(file.getOriginalFilename());
					if (file.getSize() > limitsize) {
						throw new LimitExceededException(String.format("%s size:%s>%s", file.getOriginalFilename(), file.getSize(), limitsize));
					}
				}
				if (!file.isEmpty()) {
					this.files.add(file);
				}
			}
		}
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (String key : parameterMap.keySet()) {
			if (key.equals("tag")) {
				body.put(key, parameterMap.get(key));
			} else {
				body.put(key, parameterMap.get(key)[0]);
			}
		}
	}

	private long getSize(String fileName) {
		String extension = getExtensionName(fileName);
		if (typeSizes.size() == 0&&limit.getFslConfigs()!=null) {
			for (FileSizeLimitConfig fileSize : limit.getFslConfigs()) {
				for (String type : fileSize.getTypes()) {
					if(type.trim().length()>0){
						typeSizes.put(type, fileSize.getSize());
					}
				}
			}
		}
		if (typeSizes.containsKey(extension)) {
			return typeSizes.get(extension);
		} else {
			return limit.getDefaultSize();
		}
	}

	public static String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return "";
	}
}