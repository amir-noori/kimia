package ir.kimia.client.common;

import com.google.common.eventbus.EventBus;
import ir.kimia.client.controller.MainController;
import ir.kimia.client.exception.ExceptionEventHandler;
import ir.kimia.client.service.di.FxAppComponent;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.util.ResourceBundle;

/**
 * Holding data on application level
 *
 * @author Amir
 */
public class ApplicationContext {

    private static Stage primaryStage;
    private static FxAppComponent fxApp;
    private static EventBus eventBus;
    private static ResourceBundle resourceBundle;
    private static UserSession userSession;
    private static MainController mainController;
    private static Double goldQuote = 750.00;
    private static Double dollarQuote = 100.00;
    private static Double ounceQuote = 100.00;
    private static String goldOrSilver = "gold";
    private static Double officeCarat = 750.0;
    private static Double referenceCarat = 705.0;
    private static Double weightUnitForFee = 4.6083;

    private static ExceptionEventHandler exceptionEventHandler;

    public static ExceptionEventHandler getExceptionEventHandler() {
        return exceptionEventHandler;
    }

    public static void setExceptionEventHandler(ExceptionEventHandler exceptionEventHandler) {
        exceptionEventHandler = exceptionEventHandler;
    }

    public static Double getOfficeCarat() {
        return officeCarat;
    }

    public static void setOfficeCarat(Double officeCarat) {
        ApplicationContext.officeCarat = officeCarat;
    }

    public static Double getReferenceCarat() {
        return referenceCarat;
    }

    public static void setReferenceCarat(Double referenceCarat) {
        ApplicationContext.referenceCarat = referenceCarat;
    }

    public static Double getWeightUnitForFee() {
        return weightUnitForFee;
    }

    public static void setWeightUnitForFee(Double weightUnitForFee) {
        ApplicationContext.weightUnitForFee = weightUnitForFee;
    }

    public static String getGoldOrSilver() {
        return goldOrSilver;
    }

    public static void setGoldOrSilver(String goldOrSilver) {
        goldOrSilver = goldOrSilver;
    }

    public static Double getGoldQuote() {
        return goldQuote;
    }

    public static void setGoldQuote(Double goldQuote) {
        goldQuote = goldQuote;
    }

    public static Double getDollarQuote() {
        return dollarQuote;
    }

    public static void setDollarQuote(Double dollarQuote) {
        dollarQuote = dollarQuote;
    }

    public static Double getOunceQuote() {
        return ounceQuote;
    }

    public static void setOunceQuote(Double ounceQuote) {
        ounceQuote = ounceQuote;
    }

    public static MainController getMainController() {
        return mainController;
    }

    public static void setMainController(MainController mainController) {
        ApplicationContext.mainController = mainController;
    }

    public static UserSession getUserSession() {
        return userSession;
    }

    public static void setUserSession(UserSession userSession) {
        ApplicationContext.userSession = userSession;
    }

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static void setResourceBundle(ResourceBundle resourceBundle) {
        ApplicationContext.resourceBundle = resourceBundle;
    }

    public static EventBus getEventBus() {
        return eventBus;
    }

    public static void setEventBus(EventBus eventBus) {
        ApplicationContext.eventBus = eventBus;
    }

    public static FxAppComponent getFxApp() {
        return fxApp;
    }

    public static void setFxApp(FxAppComponent fxApp) {
        ApplicationContext.fxApp = fxApp;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setPrimaryStage(Stage primaryStage) {
        ApplicationContext.primaryStage = primaryStage;
    }

    //    private static HashMap<Class, BaseController> controllers;
//
//    public static BaseController getController(Class clazz) {
//        BaseController controller = null;
//
//        for (Class c : controllers.keySet()) {
//            if(c.equals(clazz)) {
//                controller = controllers.get(c);
//                break;
//            }
//        }
//        return controller;
//    }
//
//    public static void addController(BaseController controller) {
//        if(controllers == null) {
//            controllers = new HashMap<Class, BaseController>();
//        }
//        controllers.put(controller.getClass(), controller);
//    }
//
//
//    public static void removeController(Class clazz) {
//        controllers.remove(clazz);
//    }
}
