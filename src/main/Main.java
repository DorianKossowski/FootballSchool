package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    DatabaseHandler database;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        database = DatabaseHandler.getInstance();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Szkółka Piłkarska");
        Scene loginScene = new Scene(root);
        LoginController lController = loader.getController();
        lController.enterListener(loginScene);

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        database.closeConnection();
    }
}
