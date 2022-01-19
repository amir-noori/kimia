package ir.kimia.client.util;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.common.Severity;
import ir.kimia.client.event.ExceptionEvent;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.robot.Robot;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utilities for FX application
 *
 * @author Amir
 */
public class FxUtil {

    private static final Logger log = LogManager.getLogger(FxUtil.class);
    private static final Robot robot = new Robot();


    public static void runInBackground(ProgressBar progressBar, Function<Void, Object> backgroundTask, Function<Object, Void> callback) {
        progressBar.setVisible(true);
        Service progressBarService = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() {
                        for (int i = 0; i < 100; i++) {
                            updateProgress(i, 100);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignored) {
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void cancelled() {
                        super.cancelled();
                    }
                };
            }
        };
        progressBar.progressProperty().bind(progressBarService.progressProperty());
        progressBarService.start();

        new Thread(() -> {
            Object result = null;
            if (backgroundTask != null) {
                result = backgroundTask.apply(null);
            }
            Object finalResult = result;
            Platform.runLater(() -> {
                if (callback != null) {
                    progressBarService.cancel();
                    callback.apply(finalResult);
                    progressBar.progressProperty().unbind();
                    progressBar.setProgress(0);
                    progressBar.setVisible(false);
                }
            });
        }).start();

    }

    public static void pressEnter() {
        robot.keyPress(KeyCode.ENTER);
        robot.keyRelease(KeyCode.ENTER);
    }

    public static void showTooltip() {
        final Stage currentStage;
        currentStage = ApplicationContext.getPrimaryStage();
        Screen screen = Screen.getPrimary();
        Rectangle2D visualBounds = screen.getVisualBounds();
        if (currentStage != null) {
            Tooltip originalTooltip = new Tooltip("LOADING...");
            Tooltip newTooltip = new Tooltip(originalTooltip.getText());
            newTooltip.setStyle("-fx-font-size: 20;");
            newTooltip.show(currentStage, visualBounds.getWidth() - 400, 100);
        }
    }

    public static void autoResizeColumns(TableView<?> table) {
        //Set the right policy
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().stream().forEach((column) ->
        {
            //Minimal width = column header
            Text text = new Text(column.getText());
            double max = text.getLayoutBounds().getWidth();
            column.setPrefWidth(max + 40.0d);
        });
    }

    public static void exceptionOccurred(Throwable exception) {
        ApplicationContext.getEventBus().post(new ExceptionEvent(exception));
    }

    public static void showTooltip(Stage owner, Control control, String tooltipText, ImageView tooltipGraphic) {
        if (owner == null) {
            owner = ApplicationContext.getPrimaryStage();
        }

        Point2D p = control.localToScene(0.0, 0.0);

        final Tooltip customTooltip = new Tooltip();
        customTooltip.setText(tooltipText);

        control.setTooltip(customTooltip);
        customTooltip.setAutoHide(true);

        customTooltip.show(owner, p.getX()
                + control.getScene().getX() + control.getScene().getWindow().getX(), p.getY()
                + control.getScene().getY() + control.getScene().getWindow().getY());

    }

    public static boolean showAlert(String title, String messageKey, Alert.AlertType alertType, Exception exception) {
        Alert alert = null;
        switch (alertType) {
            case CONFIRMATION -> alert = new Alert(Alert.AlertType.CONFIRMATION);
            case INFORMATION -> alert = new Alert(Alert.AlertType.INFORMATION);
            case ERROR -> alert = new Alert(Alert.AlertType.ERROR);
            case WARNING -> alert = new Alert(Alert.AlertType.WARNING);
        }
        if (alert != null) {
            alert.setTitle(message(title));
            alert.setHeaderText(message(title));
            alert.setContentText(message(messageKey));

            if (exception != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                exception.printStackTrace(pw);
                String exceptionText = sw.toString();
                Label label = new Label("The exception stacktrace was:");
                TextArea textArea = new TextArea(exceptionText);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);
                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(label, 0, 0);
                expContent.add(textArea, 0, 1);
                alert.getDialogPane().setExpandableContent(expContent);
            }
            alert.getDialogPane().getStylesheets().add(FxUtil.class.getResource("/css/popup-dialog.css").toExternalForm());
            alert.getDialogPane().getStylesheets().add("/css/popup-dialog.css");

            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        }
        return false;
    }

    public static String message(String messageKey) {
        try {
            return ApplicationContext.getResourceBundle().getString(messageKey);
        } catch (Exception e) {
            log.error("there is an exception with message key: " + messageKey + ". It may not exists.");
            return messageKey;
        }
    }

    public static boolean showException(String message, Exception exception) {
        return showAlert("confirm", message, Alert.AlertType.ERROR, exception);
    }

    public static Stage messageBox(String title, String message, Severity severity) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(ApplicationContext.getPrimaryStage());
        FXMLLoader loader = ApplicationContext.getFxApp().loader(FxUtil.class.getResource("/fxml/messageBox.fxml"));
        try {
            VBox vbox = loader.<VBox>load();
            Scene dialogScene = new Scene(vbox, 400, 150);
            ImageView severityImage = (ImageView) dialogScene.lookup("#severity-image");
            Label messageTitle = (Label) dialogScene.lookup("#message-title");
            Label messageContent = (Label) dialogScene.lookup("#message-content");
            Image image = null;
            switch (severity) {
                case INFO -> image = new Image(FxUtil.class.getResourceAsStream("/images/success.png"));
                case WARNING -> image = new Image(FxUtil.class.getResourceAsStream("/images/warning.png"));
                case ERROR, FATAL -> image = new Image(FxUtil.class.getResourceAsStream("/images/error.png"));
            }
            messageTitle.setText(title);
            messageContent.setText(message);
            severityImage.setImage(image);
            dialog.setScene(dialogScene);
            dialog.setAlwaysOnTop(false);
            dialog.show();
        } catch (IOException ioException) {
            exceptionOccurred(ioException);
        }
        return dialog;
    }

    public static boolean confirm(String message) {
        return showAlert("confirm", message, Alert.AlertType.CONFIRMATION, null);
    }

    public static boolean info(String message) {
        return showAlert("info", message, Alert.AlertType.INFORMATION, null);
    }

    public static boolean warning(String message) {
        return showAlert("warning", message, Alert.AlertType.WARNING, null);
    }

    public static boolean error(String message) {
        return showAlert("error", message, Alert.AlertType.ERROR, null);
    }

    public static boolean error(int errorCode) {
        return showAlert(errorCode + ".title", errorCode + ".desc", Alert.AlertType.ERROR, null);
    }

    /**
     * This util function is added to fix the issue regarding the problem with RIGHT_TO_LEFT orientation and
     *
     * @param textFields
     */
    public static void handleCaretPosition(TextField... textFields) {
        for (TextField textField : textFields) {
            textField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent keyEvent) {
                    KeyCode keyCode = keyEvent.getCode();
                    int caretPosition = textField.getCaretPosition();

                    if (keyCode.equals(KeyCode.LEFT)) {
                        textField.positionCaret(caretPosition - 1);
                    } else if (keyCode.equals(KeyCode.RIGHT)) {
                        textField.positionCaret(caretPosition + 1);
                    }
                }
            });
        }
    }

}