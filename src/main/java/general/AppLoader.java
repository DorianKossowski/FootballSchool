package main.java.general;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.controllers.LoginController;

public class AppLoader extends Application{
    private DatabaseHandler database;

    @Override
    public void init() {
        database = DatabaseHandler.getInstance();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../resources/view/login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Szkółka Piłkarska");
        Scene loginScene = new Scene(root);
        LoginController lController = loader.getController();
        lController.setListeners(loginScene);

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        database.closeConnection();
    }
}