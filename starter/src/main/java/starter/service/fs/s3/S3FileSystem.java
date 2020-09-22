package starter.service.fs.s3;//package com.nikoyo.odata.file.s3;
//
//
//import com.amazonaws.ClientConfiguration;
//import com.amazonaws.Protocol;
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.model.ObjectMetadata;
//import com.amazonaws.services.s3.model.PutObjectResult;
//import com.amazonaws.services.s3.model.S3Object;
//import com.nikoyo.odata.file.FileSystem;
//import org.apache.commons.io.IOUtils;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.UUID;
//
//public class S3FileSystem implements FileSystem {
//    private final String accessKey;//= "X2PAC5FWSW3VVZD9CCH5";
//    private final String secretKey;//= "xxIlSMCg19XMBLRag1kabyidFwwhC0AiCtKHLOyr";
//    private final String bucketName;//= "xql";
//    private final AmazonS3 amazonS3;
//
//
//    public S3FileSystem(String accessKey,String secretKey,String bucketName,String endpoint) {
//
////        accessKey = Configuration.UC_PROPERTIES.getProperty("fs.s3.accessKey");
////        secretKey = Configuration.UC_PROPERTIES.getProperty("fs.s3.secretKey");
////        bucketName = Configuration.UC_PROPERTIES.getProperty("fs.s3.bucketName");
////        String endpoint = Configuration.UC_PROPERTIES.getProperty("fs.s3.endpoint");
//
//        this.accessKey = accessKey;
//        this.secretKey = secretKey;
//        this.bucketName = bucketName;
//        //String endpoint = Configuration.UC_PROPERTIES.getProperty("fs.s3.endpoint");
//
//        ClientConfiguration clientConfig = new ClientConfiguration();
//        clientConfig.setProtocol(Protocol.HTTP);
//        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
//        amazonS3 = new AmazonS3Client(credentials, clientConfig);
//        amazonS3.setEndpoint(endpoint);
//    }
//
//    public String write(byte[] buffer) {
//        InputStream inputStream = new ByteArrayInputStream(buffer);
//        try {
//            ObjectMetadata objectMetadata = new ObjectMetadata();
//            String key = UUID.randomUUID().toString();
//            PutObjectResult putObjectResult = amazonS3.putObject(bucketName, key, inputStream, objectMetadata);
//            return key;
//        } finally {
//            IOUtils.closeQuietly(inputStream);
//        }
//    }
//
//    public byte[] read(String key) {
//        try {
//            S3Object s3Object = amazonS3.getObject(bucketName, key);
//            byte[] buffer = IOUtils.toByteArray(s3Object.getObjectContent());
//            IOUtils.closeQuietly(s3Object);
//            return buffer;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
