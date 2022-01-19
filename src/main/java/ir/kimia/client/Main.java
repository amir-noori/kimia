package ir.kimia.client;

import com.google.common.eventbus.EventBus;
import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.common.UTF8Control;
import ir.kimia.client.common.UserSession;
import ir.kimia.client.controller.BaseController;
import ir.kimia.client.exception.ExceptionEventHandler;
import ir.kimia.client.service.di.DaggerServiceComponent;
import ir.kimia.client.service.di.FxAppComponent;
import ir.kimia.client.service.di.ServiceComponent;
import ir.kimia.client.util.FxUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InaccessibleObjectException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Amir
 */
public class Main extends Application {

    private static final Logger log = LogManager.getLogger(Main.class);

    // DaggerServiceComponent is auto generated from ServiceComponent by Dagger
    private static final ServiceComponent LAUNCHER = DaggerServiceComponent.create();

    private ProgressBar bar;

    private Scene createPreloaderScene() {
        bar = new ProgressBar();
        BorderPane p = new BorderPane();
        p.setCenter(bar);
        return new Scene(p, 300, 150);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        UserSession userSession = new UserSession();
        ApplicationContext.setUserSession(userSession);

        // event bus is created once and stored in application context for registering and sending messages between different components.
        EventBus eventBus = new EventBus();
        ApplicationContext.setEventBus(eventBus);

        ExceptionEventHandler exceptionEventHandler = new ExceptionEventHandler();
        eventBus.register(exceptionEventHandler);
        ApplicationContext.setExceptionEventHandler(exceptionEventHandler);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("bundles.messages", new Locale("fa"), new UTF8Control());
        ApplicationContext.setResourceBundle(resourceBundle);

        FxAppComponent fxApp = LAUNCHER.fxApp()
                .application(this)
                .mainWindow(primaryStage)
                .build();

        // handles exception globally
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof InaccessibleObjectException) {
                // @TODO: check why this happens on dialog loading.
            } else {
                FxUtil.exceptionOccurred(throwable);
            }
        });

        ApplicationContext.setFxApp(fxApp);
        ApplicationContext.setPrimaryStage(primaryStage);
        FXMLLoader loader = fxApp.loader(getClass().getResource("/fxml/entrance.fxml"));
        Parent root = loader.load();
        Object controller = loader.getController();
        root.requestFocus();
        primaryStage.setTitle(resourceBundle.getString("app.name"));
        root.getStylesheets().add("/css/main.css");
        root.getStylesheets().add("/css/popup-dialog.css");

        primaryStage.setScene(new Scene(root, 1200, 700));
        primaryStage.setMaximized(true);
        primaryStage.show();
        ((BaseController) controller).postLoad();

    }


    public static void main(String[] args) {
        launch(args);
    }
}