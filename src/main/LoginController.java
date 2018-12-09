package main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginController{
    @FXML
    TextField login;
    @FXML
    TextField password;
    @FXML
    Text userValidError;
    @FXML
    Button loginButton;

    User currentUser;


    void enterListener(Scene currentScene) {
        currentScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                loginButton.fire();
            }
        });
    }

    @FXML
    public void userLogin(ActionEvent event) {
        userValidError.setVisible(false);
        if(userValidation(login.getText(), password.getText())) {
            try {
                MainController controller = new MainController(currentUser);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
                loader.setController(controller);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(loader.load());
                stage.setScene(scene);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        else {
            userValidError.setVisible(true);
        }
    }

    private boolean userValidation(String login, String password) {
        try {
            Connection conn = DatabaseHandler.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from szkolka.uzytkownik where login='" + login + "' and " +
                    "haslo='" + password + "';");
            if(rs.next()) {
                currentUser = new User(rs.getString("imie"), rs.getString("nazwisko"),
                        rs.getInt("id_tu"));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
