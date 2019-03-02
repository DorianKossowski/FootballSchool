package main.java.general;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AppPreloaderTest extends ApplicationTest {

    private static AppPreloader appPreloader = new AppPreloader();

    @Test
    public void start() {
        Platform.runLater(() -> {
            try {
                appPreloader.start(new Stage());
                assertTrue(appPreloader.preloaderStage.isShowing());
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void handleStateChangeNotification() {
        Platform.runLater(() -> {
            try {
                appPreloader.start(new Stage());
                appPreloader.handleStateChangeNotification(
                        new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
                assertFalse(appPreloader.preloaderStage.isShowing());
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    @AfterClass
    public static void windowClosed() {
        assertFalse(appPreloader.preloaderStage.isShowing());
    }
}