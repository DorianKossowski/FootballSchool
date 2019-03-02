package main.java.general;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.controllers.LoginController;

public class AppLoader extends Application {
    private DatabaseHandler database;

    public static void main(String[] args) {
        LauncherImpl.launchApplication(AppLoader.class, AppPreloader.class, args);
    }

    @Override
    public void init() {
        database = DatabaseHandler.getInstance();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Szkółka Piłkarska");
        Scene loginScene = new Scene(root);
        LoginController lController = loader.getController();
        lController.setListeners(loginScene);

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        database.closeConnection();
    }
}