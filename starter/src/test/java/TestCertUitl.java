import starter.util.sdk.CerUtil;

/**
 * Modification History:
 * =============================================================================
 * Author         Date          Description
 * ------------ ---------- ---------------------------------------------------
 * JackIce   2017-09-01         测试工具类
 * =============================================================================
 */
public class TestCertUitl {
    public static void main(String[] args) {
        String encryptCertId = CerUtil.getEncryptCertId();
        System.out.println(encryptCertId);
    }
}
