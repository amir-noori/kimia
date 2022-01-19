package ir.kimia.client.exception;

import com.google.common.eventbus.Subscribe;
import ir.kimia.client.event.ExceptionEvent;
import ir.kimia.client.util.FxUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

/**
    global exception handler based on exception event.
    @author: Amir
 */
public class ExceptionEventHandler {

    private static final Logger log = LogManager.getLogger(ExceptionEventHandler.class);

    @Subscribe
    public void handleException(ExceptionEvent event) {
        Throwable exception = event.getException();
        if (exception != null) {
            String stackTrace = ExceptionUtils.getStackTrace(exception);
            if (exception instanceof SQLException) {
                log.error(stackTrace);
                FxUtil.showException("database.operation.error", (SQLException) exception);
            } else if (exception instanceof ApplicationException) {
                String errorCode = ((ApplicationException) exception).getErrorCode();
                FxUtil.error(Integer.parseInt(errorCode));
            } else if (exception instanceof Exception) {
                log.error(stackTrace);
                FxUtil.showException("unknown.error", (Exception) exception);
            }
        }

    }
}
