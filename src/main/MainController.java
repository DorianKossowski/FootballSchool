package main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    Text helloUser;
    private User currentUser;

    MainController(User u) {
        currentUser = u;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        helloUser.setText("Witaj " + currentUser.name);
    }
}
