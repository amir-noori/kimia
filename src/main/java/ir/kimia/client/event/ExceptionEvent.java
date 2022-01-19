package ir.kimia.client.event;


/**
 * Event for notifiying that an exception has ocured which is handled by @{@link ir.kimia.client.exception.ExceptionEventHandler}
 *
 * @author Amir
 */
public class ExceptionEvent {

    private Throwable exception;

    public ExceptionEvent(Throwable exception) {
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
