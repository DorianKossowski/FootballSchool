package main.java.controllers;

import main.java.general.DatabaseHandler;
import main.java.general.User;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ChangePassController {

    @FXML
    private Text nameText, typeText, warningText;
    @FXML
    private PasswordField oldPassword, newPassword, newPassword2;

    private String currentLogin;

    /**
     * @param currentUser currently logged user
     * sets properly texts values on the scene and
     * calls listeners
     */
    void userInit(User currentUser) {
        nameText.setText(currentUser.getName() + " " + currentUser.getSurname());
        switch (currentUser.getUserType()) {
            case ADMIN:
                typeText.setText("Administrator");
                break;
            case COACH:
                typeText.setText("Trener");
                break;
            case PARENT:
                typeText.setText("Rodzic");
                break;
        }
        currentLogin = currentUser.getLogin();
        setListeners();
    }

    /**
     * sets listeners on text fields
     */
    private void setListeners() {
        oldPassword.textProperty().addListener( observable -> {
            if(warningText.isVisible()) {
                warningText.setVisible(false);
            }
        });
        newPassword.textProperty().addListener( observable -> {
            if(warningText.isVisible()) {
                warningText.setVisible(false);
            }
        });
        newPassword2.textProperty().addListener( observable -> {
            if(warningText.isVisible()) {
                warningText.setVisible(false);
            }
        });
    }

    /**
     * @return if entered values (passwords) are correct
     */
    private boolean changeValidation() {
        return newPassword.getText().equals(newPassword2.getText()) && newPassword.getLength() >= 5
                && !oldPassword.getText().equals(newPassword.getText());
    }

    /**
     * inserts password update to DB
     */
    @FXML
    private void changePassword() {
        try {
            warningText.setVisible(true);
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select 1 from szkolka.uzytkownik where login='" + currentLogin + "' " +
                        "and haslo='" + oldPassword.getText() + "';")) {

                    if (rs.next() && changeValidation()) {
                        st.execute("update szkolka.uzytkownik set haslo='" + newPassword.getText() + "' where login='" +
                                currentLogin + "';");
                        oldPassword.setText("");
                        newPassword.setText("");
                        newPassword2.setText("");
                        warningText.setText("Poprawnie zmieniono hasło");
                        warningText.setVisible(true);
                    } else {
                        warningText.setText("Podano złe wartości");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
