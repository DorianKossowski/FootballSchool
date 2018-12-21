package main;

import general.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class AddPlayerController {

    @FXML
    private BorderPane borderPane;
    @FXML
    private Button backButton;

    private int teamId;
    private User loggedUser;

    void init(BorderPane borderPane, int teamId, User loggedUser) {
        this.borderPane = borderPane;
        this.teamId = teamId;
        this.loggedUser = loggedUser;
    }

    @FXML
    private void backToPreviousScene() {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("team.fxml"));
            root = loader.load();
            TeamController tController = loader.getController();
            tController.userInit(loggedUser, borderPane);

        } catch (IOException e) {
            e.printStackTrace();
        }
        borderPane.setCenter(root);
    }
}
