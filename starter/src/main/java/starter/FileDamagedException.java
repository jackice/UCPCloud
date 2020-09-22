package starter;
/**
 * Modification History:
 * =============================================================================
 * Author         Date          Description
 * ------------ ---------- ---------------------------------------------------
 * JackIce   2017-09-04
 * =============================================================================
 */
public class FileDamagedException extends RuntimeException{

    public FileDamagedException(String message) {
        super(message);
    }
}
