package ir.kimia.client;


import com.sun.javafx.application.LauncherImpl;

/**
 * This class is created to prevent "Runtime Components Missing" issue
 * To see details check: https://edencoding.com/runtime-components-error/
 *
 * @author Amir
 */
public class App {
    public static void main(String[] args) {
//        Main.main(args);
        LauncherImpl.launchApplication(Main.class, AppPreLoader.class, args);
    }
}
