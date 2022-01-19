package ir.kimia.client.exception;

public class InvoiceValidationException extends Exception {

    private String messageKey;

    public InvoiceValidationException(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
}
