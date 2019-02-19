package main.java.controllers;

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
import main.java.general.DatabaseHandler;
import main.java.general.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginController {
    @FXML
    TextField login;
    @FXML
    TextField password;
    @FXML
    Text userValidError;
    @FXML
    Button loginButton;

    private User currentUser;


    /**
     * @param currentScene where listeners are staying awake
     */
    public void setListeners(Scene currentScene) {
        currentScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                loginButton.fire();
            }
        });

        login.textProperty().addListener( observable -> setErrorInvisible());
        password.textProperty().addListener( observable -> setErrorInvisible());
    }

    /**
     * disable error text after changing value of textfield
     */
    @FXML
    private void setErrorInvisible() {
        if(userValidError.isVisible()) {
            userValidError.setVisible(false);
        }
    }
    /**
     * @param event connected with LOGIN button
     * after correct user validation, changes to main scene
     */
    @FXML
    private void userLogin(ActionEvent event) {
        if(userValidation(login.getText(), password.getText())) {
            try {
                MainController controller = new MainController(currentUser);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
                loader.setController(controller);
                Scene currentScene = ((Node) event.getSource()).getScene();
                Stage stage = (Stage) currentScene.getWindow();
                Scene scene = new Scene(loader.load(), currentScene.getWidth(), currentScene.getHeight());
                stage.setScene(scene);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        else {
            userValidError.setVisible(true);
        }
    }

    /**
     * @param login entered user login
     * @param password entered user password
     * @return  is that user in database
     * checks if user exists in database
     */
    private boolean userValidation(String login, String password) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select * from szkolka.uzytkownik where login='" + login + "' and " +
                        "haslo='" + password + "';")) {
                    if (rs.next()) {
                        currentUser = User.getInstance();
                        currentUser.init(rs.getString("imie"), rs.getString("nazwisko"),
                                rs.getInt("id_tu"), login, rs.getInt("id_u"));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
