package starter.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;

/**
 * Created by admin on 2017/8/31.
 */
public class Certifacate {
    private static final String STORE_PASS = "1234567890";
    private static final String ALIAS = "server";
    private static final String KEYSTORE_PATH = "D:\\public\\server.jks";
    private static final String CERT_PATH = "D:\\public\\server.cer";
    private static final String PLAIN_TEXT = "MANUTD is the most greatest club in the world.";
    /** JDK6只支持X.509标准的证书 */
    private static final String CERT_TYPE = "X.509";

    public static void main(String[] args) throws IOException {
        /**
         * 假设现在有这样一个场景 。A机器上的数据，需要加密导出，然后将导出文件放到B机器上导入。 在这个场景中，A相当于服务器，B相当于客户端
         */

        /** A */
//        KeyStore keyStore = getKeyStore(STORE_PASS, KEYSTORE_PATH);
//        PrivateKey privateKey = getPrivateKey(keyStore, ALIAS, STORE_PASS);
//        X509Certificate certificate = getCertificateByKeystore(keyStore, ALIAS);
//        /** 加密和签名 */
//        byte[] encodedText = encode(PLAIN_TEXT.getBytes(), privateKey);
//        System.out.println(encodedText.toString());
//        System.out.println(PLAIN_TEXT);
//        byte[] signature = sign(certificate, privateKey, PLAIN_TEXT.getBytes());
//        System.out.println("signature:"+signature.toString());
//        /** 现在B收到了A的密文和签名，以及A的可信任证书 */
//        X509Certificate receivedCertificate = getCertificateByCertPath(
//                CERT_PATH, CERT_TYPE);
//        PublicKey publicKey = getPublicKey(receivedCertificate);
//        byte[] decodedText = decode(encodedText, publicKey);
//        System.out.println("decodedText:"+decodedText.toString());
//        System.out.println("Signature is : "
//                + verify(receivedCertificate, decodedText, signature));
    }

    /**
     * 加载密钥库，与Properties文件的加载类似，都是使用load方法
     *
     * @throws IOException
     */
    public static KeyStore getKeyStore(String storepass, String keystorePath)
            throws IOException {
        InputStream inputStream = null;
        try {
            System.out.println(KeyStore.getDefaultType());
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            inputStream = new FileInputStream(keystorePath);
            keyStore.load(inputStream, storepass.toCharArray());
            return keyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException
                | CertificateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        return null;
    }

    /**
     * 获取私钥
     *
     * @param keyStore
     * @param alias
     * @param password
     * @return
     */
    public static PrivateKey getPrivateKey(KeyStore keyStore, String alias,
                                           String password) {
        try {
            return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        } catch (UnrecoverableKeyException | KeyStoreException
                | NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取公钥
     *
     * @param certificate
     * @return
     */
    public static PublicKey getPublicKey(Certificate certificate) {
        return certificate.getPublicKey();
    }

    /**
     * 通过密钥库获取数字证书，不需要密码，因为获取到Keystore实例
     *
     * @param keyStore
     * @param alias
     * @return
     */
    public static X509Certificate getCertificateByKeystore(KeyStore keyStore,
                                                           String alias) {
        try {
            return (X509Certificate) keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过证书路径生成证书，与加载密钥库差不多，都要用到流。
     *
     * @param path
     * @param certType
     * @return
     * @throws IOException
     */
    public static X509Certificate getCertificateByCertPath(String path,
                                                           String certType) throws IOException {
        InputStream inputStream = null;
        try {
            // 实例化证书工厂
            CertificateFactory factory = CertificateFactory
                    .getInstance(certType);
            // 取得证书文件流
            inputStream = new FileInputStream(path);
            // 生成证书
            Certificate certificate = factory.generateCertificate(inputStream);

            return (X509Certificate) certificate;
        } catch (CertificateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        return null;

    }

    /**
     * 从证书中获取加密算法，进行签名
     *
     * @param certificate
     * @param privateKey
     * @param plainText
     * @return
     */
    public static byte[] sign(X509Certificate certificate,
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
     * 验签，公钥包含在证书里面
     *
     * @param certificate
     * @param decodedText
     * @param receivedignature
     * @return
     */
    public static boolean verify(X509Certificate certificate,
                                 byte[] decodedText, final byte[] receivedignature) {
        try {
            Signature signature = Signature.getInstance(certificate
                    .getSigAlgName());
            /** 注意这里用到的是证书，实际上用到的也是证书里面的公钥 */
            signature.initVerify(certificate);
            signature.update(decodedText);
            return signature.verify(receivedignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException
                | SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 加密。注意密钥是可以获取到它适用的算法的。
     *
     * @param plainText
     * @param privateKey
     * @return
     */
    public static byte[] encode(byte[] plainText, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return cipher.doFinal(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

    }
    /**
     * 解密，注意密钥是可以获取它适用的算法的。
     *
     * @param encodedText
     * @param publicKey
     * @return
     */
    public static byte[] decode(byte[] encodedText, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return cipher.doFinal(encodedText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
}
