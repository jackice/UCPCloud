package starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import starter.FileDamagedException;
import starter.FileSizeLimit;
import starter.FileSizeLimitConfig;
import starter.certs.Certifacate;
import starter.service.Constant;
import starter.util.Base64;
import starter.util.sdk.CerConfig;

import javax.naming.LimitExceededException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SSLMultipartParser {
	private MultipartHttpServletRequest request;
	private Json body;
	private List<MultipartFile> files;
	private ObjectMapper objectMapper = new ObjectMapper();
	private FileSizeLimit limit;
	private static Map<String, Long> typeSizes = new HashMap<String, Long>();
	private static Logger logger = Logger.getLogger(SSLMultipartParser.class);
	public SSLMultipartParser(MultipartHttpServletRequest request) {
		this.request = request;
	}

	public Json getBody() {
		return body;
	}

	public List<MultipartFile> getFiles() {
		return files;
	}

	public SSLMultipartParser invoke(FileSizeLimit limit) throws Exception {
		this.limit = limit;
		ExtractMultipart();
		return this;
	}

	public SSLMultipartParser invoke() throws Exception {
		ExtractMultipart();
		return this;
	}

	private void ExtractMultipart() throws LimitExceededException {
		body = new Json();
		files = new ArrayList<>();
		Map<String, String[]> parameterMap = request.getParameterMap();
		String signature = null;
		for (String key : parameterMap.keySet()) {
			if (key.equals("tag")) {
				body.put(key, parameterMap.get(key));
			}else if (key.equals(Constant.SUGNATURE)){
				signature = parameterMap.get(key)[0];
			}
			else {
				body.put(key, parameterMap.get(key)[0]);
			}
		}
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
				//验证文件签名
				try {
					X509Certificate validateCert = Certifacate.getValidateCert();
					logger.info(String.format("CID:%--sclient signature is:%s",validateCert.getSerialNumber(),signature));
					if (!Certifacate.verify(validateCert,file.getBytes(), Base64.decodeToBytes(signature))){
						logger.error(String.format("%s digital signature verify fail,%s may have been damaged!%s signature is:%s",
								file.getName(),file.getName(),file.getName(),signature));
						throw new FileDamagedException(String.format("%s digital signature verify fail,%s may have been damaged!",file.getName(),file.getName()));
					}else{
						//对文件进行签名
						X509Certificate encryptCert = Certifacate.getEncryptCert();
						KeyStore keyStore = Certifacate.getKeyStore();
						PrivateKey signCertPrivateKey = Certifacate.getSignCertPrivateKey();
						byte[] fileSign= Certifacate.sign(encryptCert, signCertPrivateKey, file.getBytes());
						logger.info(String.format("Server signature is :%s",fileSign));
						body.put(Constant.SUGNATURE,fileSign);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (!file.isEmpty()) {
					this.files.add(file);
				}
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