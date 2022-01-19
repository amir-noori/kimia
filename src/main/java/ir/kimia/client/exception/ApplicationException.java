package ir.kimia.client.exception;

public class ApplicationException extends Exception {

    protected String errorCode;

    public ApplicationException(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
