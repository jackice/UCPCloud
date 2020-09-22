package starter.util.sdk;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Modification History:
 * =============================================================================
 * Author         Date          Description
 * ------------ ---------- ---------------------------------------------------
 * JackIce      2017-09-02      证书工具类
 * =============================================================================
 */
@Component
@ConfigurationProperties(prefix = "cer")
public class CerConfig {
    /** 签名证书路径. */
    private String signCertPath;
    /** 签名证书密码. */
    private String signCertPwd;
    /** 签名证书类型. */
    private String signCertType;
    /** 加密公钥证书路径. */
    private String encryptCertPath;
    /** 验证签名公钥证书目录. */
    private String validateCertDir;
    private boolean singleMode;

    public boolean isSingleMode() {
        return singleMode;
    }

    public void setSingleMode(boolean singleMode) {
        this.singleMode = singleMode;
    }

    public String getEncryptCertPath() {
        return encryptCertPath;
    }

    public String getSignCertPath() {
        return signCertPath;
    }

    public void setSignCertPath(String signCertPath) {
        this.signCertPath = signCertPath;
    }

    public String getSignCertPwd() {
        return signCertPwd;
    }

    public void setSignCertPwd(String signCertPwd) {
        this.signCertPwd = signCertPwd;
    }

    public String getSignCertType() {
        return signCertType;
    }

    public void setSignCertType(String signCertType) {
        this.signCertType = signCertType;
    }

    public void setEncryptCertPath(String encryptCertPath) {
        this.encryptCertPath = encryptCertPath;
    }

    public String getValidateCertDir() {
        return validateCertDir;
    }

    public void setValidateCertDir(String validateCertDir) {
        this.validateCertDir = validateCertDir;
    }
}
