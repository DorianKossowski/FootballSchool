package main;

import general.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import login.LoginController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    Text helloUser;
    @FXML
    Button logoutButton;
    @FXML
    BorderPane borderPane;

    private User currentUser;

    /**
     * @param u currently logged user
     */
    public MainController(User u) {
        currentUser = u;
    }

    /**
     * sets welcome text and assigns logout button
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        helloUser.setText("Witaj " + currentUser.getName());
        logoutButton.setOnAction(this::userLogout);

        if(currentUser.getUserType() == User.Type.ADMIN) {
            Parent root = null;
            try {
                root = FXMLLoader.load(getClass().getResource("admin.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            borderPane.setCenter(root);
        }
    }

    /**
     * @param event connected with LOGOUT
     * changes scene to login scene
     */
    private void userLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../login/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            LoginController lcontroller = loader.getController();
            lcontroller.setListeners(scene);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
