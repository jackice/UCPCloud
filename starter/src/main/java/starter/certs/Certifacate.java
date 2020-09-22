package starter.certs;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import starter.util.sdk.CerConfig;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modification History:
 * =============================================================================
 * Author         Date          Description
 * ------------ ---------- ---------------------------------------------------
 * JackIce   2017-09-06
 * =============================================================================
 */
public class Certifacate {
    private static CerConfig config;
    private static Logger logger = Logger.getLogger(Certifacate.class);
    /** 证书容器. */
    private static KeyStore keyStore = null;
    /** 密码加密证书 */
    private static X509Certificate encryptCert = null;
    /** 验证签名证书. */
    private static X509Certificate validateCert = null;
    /** 验签证书存储Map. */
    private static Map<String, X509Certificate> certMap = new HashMap<String, X509Certificate>();
    /** 根据传入证书文件路径和密码读取指定的证书容器.(一种线程安全的实现方式) */
    private static final  ThreadLocal<KeyStore> certKeyStoreLocal = new ThreadLocal<KeyStore>();
    /** 基于Map存储多商户RSA私钥 */
    private static final  Map<String, KeyStore> certKeyStoreMap = new ConcurrentHashMap<String, KeyStore>();

    public static  KeyStore getKeyStore() {
        return keyStore;
    }

    public static X509Certificate getEncryptCert() {
        return encryptCert;
    }

    public static X509Certificate getValidateCert() {
        return validateCert;
    }

    public Certifacate(CerConfig config) {
        this.config = config;
    }

    /**
     * 初始化所有证书.
     */
    public void init() {
        initProvider();
        logger.info(String.format("InitCertifacate starting........"));
        if (config.isSingleMode()) {
            // 单证书模式,初始化配置文件中的签名证书
            initSignCert();
        }
        initEncryptCert();// 初始化加密公钥证书
        initValidateCertFromDir();// 初始化所有的验签证书
    }
    private void initProvider(){
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 加载签名证书
     */
    private  void initSignCert() {
        if (null != keyStore) {
            keyStore = null;
        }
        try {
            keyStore = getKeyStore(config.getSignCertPwd(),config.getSignCertPath(),config.getSignCertType());
            logger.info(String.format("InitSignCert Successful. CertId=[%s]",getSignCertId()));
        } catch (IOException e) {
            logger.error(String.format("initSignCert has Error:[%s]--%s",e.getMessage(),e));
        }
    }
    /**
     * 加载密钥库，与Properties文件的加载类似，都是使用load方法
     *
     * @throws IOException
     */
    private static KeyStore getKeyStore(String storepass, String keystorePath,String keystoreType)
            throws IOException {
        logger.info(String.format("storepass:%s-keystorepath:%s-keystoretype:%s",storepass,keystorePath,keystoreType));
        InputStream inputStream = null;
        try {
            KeyStore keyStore = null;
            if (StringUtils.isBlank(keystoreType)) {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            }else{
                keyStore = KeyStore.getInstance(keystoreType);
            }
            inputStream = new FileInputStream(keystorePath);
            keyStore.load(inputStream, storepass.toCharArray());
            return keyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException
                | CertificateException | IOException e) {
           logger.error(String.format("getKeystore fail:%s",e));
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        return null;
    }
    /**
     * 根据传入的证书文件路径和证书密码加载指定的签名证书
     * @deprecated
     */
    public static void initSignCert(String certFilePath, String certPwd) {
        logger.info(String.format("加载证书文件[%s]和证书密码[%s]的签名证书开始.",certFilePath,certPwd));
        certKeyStoreLocal.remove();
        File files = new File(certFilePath);
        if (!files.exists()) {
            logger.error("证书文件不存在,初始化签名证书失败.");
            return;
        }
        try {
            certKeyStoreLocal.set(getKeyInfo(certFilePath, certPwd, "PKCS12"));
        } catch (IOException e) {
            logger.error(String.format("加载签名证书识别:[%s]--%s",e.getMessage(),e));
        }
        logger.info(String.format("加载证书文件[%s]和证书密码[%s]的签名证书结束.",certFilePath,certPwd));
    }


    /**
     * 加载RSA签名证书
     *
     * @param certFilePath
     * @param certPwd
     */
    public static void loadRsaCert(String certFilePath, String certPwd) {
        KeyStore keyStore = null;
        try {
            keyStore = getKeyInfo(certFilePath, certPwd, "PKCS12");
            certKeyStoreMap.put(certFilePath, keyStore);
            logger.info("LoadRsaCert Successful");
        } catch (IOException e) {
            logger.error(String.format("LoadRsaCert Error:[%s]--%s",e.getMessage(),e));
        }
    }
    /**
     * 从证书中获取加密算法，进行签名
     *
     * @param certificate
     * @param privateKey
     * @param plainText
     * @return
     */
    public static  byte[] sign(X509Certificate certificate,
                              PrivateKey privateKey, byte[] plainText) {
        /** 如果要从密钥库获取签名算法的名称，只能将其强制转换成X509标准，JDK 6只支持X.509类型的证书 */
        try {
            Signature signature = Signature.getInstance(certificate
                    .getSigAlgName());
            signature.initSign(privateKey);
            signature.update(plainText);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException
                | SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
    /**
     * 加载密码加密证书 目前支持有两种加密
     */
    private  void initEncryptCert() {
        logger.info(String.format("加载敏感信息加密证书==>%s.",config.getEncryptCertPath()));
        if (!StringUtils.isEmpty(config.getEncryptCertPath())) {
            encryptCert = initCert(config.getEncryptCertPath());
            logger.info("LoadEncryptCert Successful");
        } else {
            logger.info("WARN: acpsdk.encryptCert.path is empty");
        }
//		if (!isEmpty(config.getEncryptTrackCertPath())) {
//			encryptTrackCert = initCert(config
//					.getEncryptTrackCertPath());
//			logger.writeLog("LoadEncryptTrackCert Successful");
//		} else {
//			logger.writeLog("WARN: acpsdk.encryptTrackCert.path is empty");
//		}
    }

    /**
     *
     * @param path
     * @return
     */
    private static X509Certificate initCert(String path) {
        X509Certificate encryptCertTemp = null;
        CertificateFactory cf = null;
        FileInputStream in = null;
        try {
            cf = CertificateFactory.getInstance("X.509", "BC");
            in = new FileInputStream(path);
            encryptCertTemp = (X509Certificate) cf.generateCertificate(in);
            // 打印证书加载信息,供测试阶段调试
            logger.info(String.format("[%s][CertId=%s]",path,encryptCertTemp.getSerialNumber().toString()));
        } catch (CertificateException e) {
            logger.error(String.format("InitCert Error:[%s]--%s",e.getMessage(),e));
        } catch (FileNotFoundException e) {
            logger.error(String.format("InitCert Error File Not Found:[%s]--%s",e.getMessage(),e));
        } catch (NoSuchProviderException e) {
            logger.error(String.format("LoadVerifyCert Error No BC Provider:[%s]--%s",e.getMessage(),e));
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error(String.format("Close io Error:[%s]--%s",e.getMessage(),e));
                }
            }
        }
        return encryptCertTemp;
    }

    /**
     * 从指定目录下加载验证签名证书
     *
     */
    private static void initValidateCertFromDir() {
        certMap.clear();
        String dir = config.getValidateCertDir();
        logger.info(String.format("加载验证签名证书目录==>%s",dir));
        if (StringUtils.isEmpty(dir)) {
            logger.error("ERROR: acpsdk.validateCert.dir is empty");
            return;
        }
        CertificateFactory cf = null;
        FileInputStream in = null;
        try {
            cf = CertificateFactory.getInstance("X.509", "BC");
            File fileDir = new File(dir);
            File[] files = fileDir.listFiles(new CerFilter());
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                in = new FileInputStream(file.getAbsolutePath());
                validateCert = (X509Certificate) cf.generateCertificate(in);
                certMap.put(validateCert.getSerialNumber().toString(),
                        validateCert);
                // 打印证书加载信息,供测试阶段调试
                logger.debug(String.format("[%s][CertId=%s]",file.getAbsolutePath(),validateCert.getSerialNumber().toString()));
            }
            logger.info("LoadVerifyCert Successful");
        } catch (CertificateException e) {
            logger.error("LoadVerifyCert Error", e);
        } catch (FileNotFoundException e) {
            logger.error("LoadVerifyCert Error File Not Found", e);
        } catch (NoSuchProviderException e) {
            logger.error("LoadVerifyCert Error No BC Provider", e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error(e.toString());
                }
            }
        }
    }


    /**
     * 获取签名证书私钥（单证书模式）
     *
     * @return
     */
    public static PrivateKey getSignCertPrivateKey() {
        try {
            Enumeration<String> aliasenum = keyStore.aliases();
            String keyAlias = null;
            if (aliasenum.hasMoreElements()) {
                keyAlias = aliasenum.nextElement();
            }
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias,
                    config.getSignCertPwd().toCharArray());
            return privateKey;
        } catch (KeyStoreException e) {
            logger.error("getSignCertPrivateKey Error", e);
            return null;
        } catch (UnrecoverableKeyException e) {
            logger.error("getSignCertPrivateKey Error", e);
            return null;
        } catch (NoSuchAlgorithmException e) {
            logger.error("getSignCertPrivateKey Error", e);
            return null;
        }
    }


    /**
     * 通过传入证书绝对路径和证书密码获取所对应的签名证书私钥
     *
     * @param certPath
     *            证书绝对路径
     * @param certPwd
     *            证书密码
     * @return 证书私钥
     *
     * @deprecated
     */
    public static PrivateKey getSignCertPrivateKeyByThreadLocal(
            String certPath, String certPwd) {
        if (null == certKeyStoreLocal.get()) {
            // 初始化指定certPath和certPwd的签名证书容器
            initSignCert(certPath, certPwd);
        }
        try {
            Enumeration<String> aliasenum = certKeyStoreLocal.get().aliases();
            String keyAlias = null;
            if (aliasenum.hasMoreElements()) {
                keyAlias = aliasenum.nextElement();
            }
            PrivateKey privateKey = (PrivateKey) certKeyStoreLocal.get()
                    .getKey(keyAlias, certPwd.toCharArray());
            return privateKey;
        } catch (Exception e) {
            logger.error("获取[" + certPath + "]的签名证书的私钥失败", e);
            return null;
        }
    }

    public  PrivateKey getSignCertPrivateKeyByStoreMap(String certPath,
                                                             String certPwd) {
        if (!certKeyStoreMap.containsKey(certPath)) {
            loadRsaCert(certPath, certPwd);
        }
        try {
            Enumeration<String> aliasenum = certKeyStoreMap.get(certPath)
                    .aliases();
            String keyAlias = null;
            if (aliasenum.hasMoreElements()) {
                keyAlias = aliasenum.nextElement();
            }
            PrivateKey privateKey = (PrivateKey) certKeyStoreMap.get(certPath)
                    .getKey(keyAlias, certPwd.toCharArray());
            return privateKey;
        } catch (KeyStoreException e) {
            logger.error("getSignCertPrivateKeyByStoreMap Error", e);
            return null;
        } catch (UnrecoverableKeyException e) {
            logger.error("getSignCertPrivateKeyByStoreMap Error", e);
            return null;
        } catch (NoSuchAlgorithmException e) {
            logger.error("getSignCertPrivateKeyByStoreMap Error", e);
            return null;
        }
    }

    /**
     * 获取加密证书公钥.密码加密时需要
     *
     * @return
     */
    public static PublicKey getEncryptCertPublicKey() {
        if (null == encryptCert) {
            String path = config.getEncryptCertPath();
            if (!StringUtils.isEmpty(path)) {
                encryptCert = initCert(path);
                return encryptCert.getPublicKey();
            } else {
                logger.error("ERROR: acpsdk.encryptCert.path is empty");
                return null;
            }
        } else {
            return encryptCert.getPublicKey();
        }
    }

    /**
     * 获取加密证书公钥.密码加密时需要
     * 加密磁道信息证书
     *
     * @return
     */
    public  PublicKey getEncryptTrackPublicKey() {
//		if (null == encryptTrackCert) {
//			String path = config.getEncryptTrackCertPath();
//			if (!isEmpty(path)) {
//				encryptTrackCert = initCert(path);
//				return encryptTrackCert.getPublicKey();
//			} else {
//				logger.writeLog("ERROR: acpsdk.encryptTrackCert.path is empty");
//				return null;
//			}
//		} else {
//			return encryptTrackCert.getPublicKey();
//		}
//        if (null == encryptTrackKey) {
//            initTrackKey();
//        }
//        return encryptTrackKey;
        return null;
    }

    /**
     * 验证签名证书
     *
     * @return 验证签名证书的公钥
     */
    public  PublicKey getValidateKey() {
        if (null == validateCert) {
            return null;
        }
        return validateCert.getPublicKey();
    }
    public static X509Certificate getValidateCertificate(String certId) {
        X509Certificate cf = null;
        if (certMap.containsKey(certId)) {
            // 存在certId对应的证书对象
            cf = certMap.get(certId);
            return cf;
        } else {
            // 不存在则重新Load证书文件目录
            initValidateCertFromDir();
            if (certMap.containsKey(certId)) {
                // 存在certId对应的证书对象
                cf = certMap.get(certId);
                return cf;
            } else {
                logger.error("缺少certId=[" + certId + "]对应的验签证书.");
                return null;
            }
        }
    }

    /**
     * 通过certId获取证书Map中对应证书的公钥
     *
     * @param certId
     *            证书物理序号
     * @return 通过证书编号获取到的公钥
     */
    public  PublicKey getValidateKey(String certId) {
        X509Certificate cf = null;
        if (certMap.containsKey(certId)) {
            // 存在certId对应的证书对象
            cf = certMap.get(certId);
            return cf.getPublicKey();
        } else {
            // 不存在则重新Load证书文件目录
            initValidateCertFromDir();
            if (certMap.containsKey(certId)) {
                // 存在certId对应的证书对象
                cf = certMap.get(certId);
                return cf.getPublicKey();
            } else {
                logger.error("缺少certId=[" + certId + "]对应的验签证书.");
                return null;
            }
        }
    }



    /**
     * 获取签名证书中的证书序列号（单证书）
     *
     * @return 证书的物理编号
     */
    public  String getSignCertId() {
        try {
            System.out.println(null == keyStore);
            Enumeration<String> aliasenum = keyStore.aliases();
            String keyAlias = null;
            if (aliasenum.hasMoreElements()) {
                keyAlias = aliasenum.nextElement();
                logger.info(String.format("keyStore aliase:%s",keyAlias));
            }
            X509Certificate cert = (X509Certificate) keyStore
                    .getCertificate(keyAlias);
            return cert.getSerialNumber().toString();
        } catch (Exception e) {
            logger.error("getSignCertId Error", e);
            return null;
        }
    }

    /**
     * 获取加密证书的证书序列号
     *
     * @return
     */
    public  String getEncryptCertId() {
        if (null == encryptCert) {
            String path = config.getEncryptCertPath();
            if (!StringUtils.isEmpty(path)) {
                encryptCert = initCert(path);
                return encryptCert.getSerialNumber().toString();
            } else {
                logger.error("ERROR: acpsdk.encryptCert.path is empty");
                return null;
            }
        } else {
            return encryptCert.getSerialNumber().toString();
        }
    }

    /**
     * 获取磁道加密证书的证书序列号
     * @deprecated 磁道根本没给证书啊啊啊啊啊啊啊
     * @return
     */
    public  String getEncryptTrackCertId() {
//		if (null == encryptTrackCert) {
//			String path = config.getEncryptTrackCertPath();
//			if (!isEmpty(path)) {
//				encryptTrackCert = initCert(path);
//				return encryptTrackCert.getSerialNumber().toString();
//			} else {
//				logger.writeLog("ERROR: acpsdk.encryptTrackCert.path is empty");
//				return null;
//			}
//		} else {
//			return encryptTrackCert.getSerialNumber().toString();
//		}
        return "";
    }

    /**
     * 获取签名证书公钥对象
     *
     * @return
     */
    public  PublicKey getSignPublicKey() {
        try {
            Enumeration<String> aliasenum = keyStore.aliases();
            String keyAlias = null;
            if (aliasenum.hasMoreElements()) // we are readin just one
            // certificate.
            {
                keyAlias = (String) aliasenum.nextElement();
            }
            Certificate cert = (Certificate) keyStore.getCertificate(keyAlias);
            PublicKey pubkey = cert.getPublicKey();
            return pubkey;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }


    /**
     * 将证书文件读取为证书存储对象
     *
     * @param pfxkeyfile
     *            证书文件名
     * @param keypwd
     *            证书密码
     * @param type
     *            证书类型
     * @return 证书对象
     * @throws IOException
     */
    public static KeyStore getKeyInfo(String pfxkeyfile, String keypwd,
                                      String type) throws IOException {
        logger.info("加载签名证书==>" + pfxkeyfile);
        FileInputStream fis = null;
        try {
            if (Security.getProvider("BC") == null) {
                logger.info("add BC provider");
                Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            } else {
                Security.removeProvider("BC"); //解决eclipse调试时tomcat自动重新加载时，BC存在不明原因异常的问题。
                Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                logger.info("re-add BC provider");
            }
            KeyStore ks = null;
            if ("JKS".equals(type)) {
                ks = KeyStore.getInstance(type, "BC");
            } else if ("PKCS12".equals(type)) {
                String jdkVendor = System.getProperty("java.vm.vendor");
                String javaVersion = System.getProperty("java.version");
                logger.info("java.vm.vendor=[" + jdkVendor + "]");
                logger.info("java.version=[" + javaVersion + "]");
                printSysInfo();
                ks = KeyStore.getInstance(type, "BC");
            }
            logger.info("Load RSA CertPath=[" + pfxkeyfile + "],Pwd=["
                    + keypwd + "]");
            fis = new FileInputStream(pfxkeyfile);
            char[] nPassword = null;
            nPassword = null == keypwd || "".equals(keypwd.trim()) ? null
                    : keypwd.toCharArray();
            if (null != ks) {
                ks.load(fis, nPassword);
            }
            return ks;
        } catch (Exception e) {
            if (Security.getProvider("BC") == null) {
                logger.error("BC Provider not installed.");
            }
            logger.error("getKeyInfo Error", e);
            if ((e instanceof KeyStoreException) && "PKCS12".equals(type)) {
                Security.removeProvider("BC");
            }
            return null;
        } finally {
            if(null!=fis)
                fis.close();
        }
    }

    // 打印系统环境信息
    public static void printSysInfo() {
        logger.info("================= SYS INFO begin====================");
        logger.info("os_name:" + System.getProperty("os.name"));
        logger.info("os_arch:" + System.getProperty("os.arch"));
        logger.info("os_version:" + System.getProperty("os.version"));
        logger.info("java_vm_specification_version:"
                + System.getProperty("java.vm.specification.version"));
        logger.info("java_vm_specification_vendor:"
                + System.getProperty("java.vm.specification.vendor"));
        logger.info("java_vm_specification_name:"
                + System.getProperty("java.vm.specification.name"));
        logger.info("java_vm_version:"
                + System.getProperty("java.vm.version"));
        logger.info("java_vm_name:" + System.getProperty("java.vm.name"));
        logger.info("java.version:" + System.getProperty("java.version"));
        printProviders();
        logger.info("================= SYS INFO end=====================");
    }

    public static void printProviders() {
        logger.info("Providers List:");
        Provider[] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            logger.info(String.format("%s1.%s",i,providers[i].getName()));
        }
    }

    /**
     * 证书文件过滤器
     *
     */
    static class CerFilter implements FilenameFilter {
        public boolean isCer(String name) {
            if (name.toLowerCase().endsWith(".cer")) {
                return true;
            } else {
                return false;
            }
        }
        public boolean accept(File dir, String name) {
            return isCer(name);
        }
    }

    /**
     * <pre>
     * 从一个ThreadLocal中获取当前KeyStore中的CertId,
     * 如果获取失败则重新初始化这个KeyStore并存入ThreadLocal
     * </pre>>
     * @deprecated
     * @param certPath
     * @param certPwd
     * @return
     */
    public static String getCertIdByThreadLocal(String certPath, String certPwd) {
        // 初始化指定certPath和certPwd的签名证书容器
        initSignCert(certPath, certPwd);
        try {
            Enumeration<String> aliasenum = certKeyStoreLocal.get().aliases();
            String keyAlias = null;
            if (aliasenum.hasMoreElements()) {
                keyAlias = aliasenum.nextElement();
            }
            X509Certificate cert = (X509Certificate) certKeyStoreLocal.get()
                    .getCertificate(keyAlias);
            System.out.println("========"+cert.getSerialNumber().toString());
            logger.debug(String.format("==============%s",cert.getSerialNumber().toString()));
        } catch (Exception e) {
            logger.error(String.format("获取签名证书的序列号失败:[%s]--%s",e.getMessage(),e));
            return "";
        }
        return "";
    }


    public static String getCertIdByKeyStoreMap(String certPath, String certPwd) {
        if (!certKeyStoreMap.containsKey(certPath)) {
            // 缓存中未查询到,则加载RSA证书
            loadRsaCert(certPath, certPwd);
        }
        return getCertIdIdByStore(certKeyStoreMap.get(certPath));
    }

    private static String getCertIdIdByStore(KeyStore keyStore) {
        Enumeration<String> aliasenum = null;
        try {
            aliasenum = keyStore.aliases();
            String keyAlias = null;
            if (aliasenum.hasMoreElements()) {
                keyAlias = aliasenum.nextElement();
            }
            X509Certificate cert = (X509Certificate) keyStore
                    .getCertificate(keyAlias);
            return cert.getSerialNumber().toString();
        } catch (KeyStoreException e) {
            logger.error(String.format("getCertIdIdByStore Error:[%s]--%s",e.getMessage(),e));
            return null;
        }
    }


    /**
     * 获取证书容器
     *
     * @return
     */
    public static Map<String, X509Certificate> getCertMap() {
        return certMap;
    }

    /**
     * 设置证书容器
     *
     * @param certMap
     */
    public  void setCertMap(Map<String, X509Certificate> certMap) {
        this.certMap = certMap;
    }

    /**
     * 使用模和指数生成RSA公钥 注意：此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同
     *
     * @param modulus
     *            模
     * @param exponent
     *            指数
     * @return
     */
    public static PublicKey getPublicKey(String modulus, String exponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            logger.error(String.format("构造RSA公钥失败:[%s]--%s",e.getMessage(),e));
            return null;
        }
    }

    /**
     * 使用模和指数的方式获取公钥对象
     *
     * @return
     */
    public static PublicKey getEncryptTrackCertPublicKey(String modulus,
                                                         String exponent) {
        if (StringUtils.isEmpty(modulus) || StringUtils.isEmpty(exponent)) {
            logger.error("[modulus] OR [exponent] invalid");
            return null;
        }
        return getPublicKey(modulus, exponent);
    }

    public static boolean verify(X509Certificate certificate,
                                 byte[] decodedText, final byte[] receivedignature) {
        /**
         * method_name: verify
         * param: [certificate, decodedText, receivedignature]
         * describe: TODO
         * creat_user: JackIce
         * creat_date: 2017/9/4
         * creat_time: 17:26
         **/
        try {
            Signature signature = Signature.getInstance(certificate
                    .getSigAlgName());
            /** 注意这里用到的是证书，实际上用到的也是证书里面的公钥 */
            signature.initVerify(certificate);
            signature.update(decodedText);
            boolean result = signature.verify(receivedignature);
            logger.info(String.format("signature verify result:%s",result));
            return result;
        } catch (NoSuchAlgorithmException | InvalidKeyException
                | SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
