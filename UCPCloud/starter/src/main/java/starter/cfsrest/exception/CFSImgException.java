package starter.cfsrest.exception;


import org.springframework.http.HttpStatus;

public class CFSImgException extends RuntimeException {

    private int statusCode;
    public CFSImgException(String message) {
        super(message);
    }
	public CFSImgException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode.value();
    }

    public CFSImgException(String message, Throwable cause, HttpStatus statusCode) {
        super(message, cause);
        this.statusCode = statusCode.value();
    }

    public CFSImgException(Throwable cause, HttpStatus statusCode) {
        super(cause);
        this.statusCode = statusCode.value();
    }

    public int getStatusCode() {
        return statusCode;
    }
}
