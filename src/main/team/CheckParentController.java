package main.team;

import general.DatabaseHandler;
import general.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckParentController {

    @FXML
    private BorderPane borderPane;
    @FXML
    private Text playerName, parentName, parentPhone, loginText, passwordText;

    private User loggedUser;

    /**
     * @param borderPane previous whole team scene
     * @param loggedUser currently logged user
     * @param selectedPlayer selected player from table
     * sets proper values for texts placed on the scene
     */
    void init(BorderPane borderPane, User loggedUser, TeamController.Player selectedPlayer) {
        this.borderPane = borderPane;
        this.loggedUser = loggedUser;

        playerName.setText(selectedPlayer.getName() + " " + selectedPlayer.getSurname());
        parentPhone.setText("Telefon: " + selectedPlayer.getPhone());
        setParent(selectedPlayer.getId_p());
    }

    /**
     * @param id_p id of parent assigned to selected player
     * gets name and surname of parent and assigns this values to text on scene
     */
    private void setParent(int id_p) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement st = conn.createStatement();

            ResultSet rs = st.executeQuery("select u.* from szkolka.uzytkownik as u join szkolka.pilkarz as p " +
                    "using(id_u) where p.id_p = " + id_p + ";");
            if(rs.next()) {
                parentName.setText(rs.getString("imie") + " " + rs.getString("nazwisko"));
                loginText.setText("login: " + rs.getString("login"));
                passwordText.setText("hasło: " + rs.getString("haslo"));
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * being called after on click backButton
     * sets previous scene
     */
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
