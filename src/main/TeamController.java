package main;

import general.DatabaseHandler;
import general.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.scene.control.TextField;

public class TeamController {
    @FXML
    private Text mainText;
    @FXML
    private VBox wholeArea, teamCreationArea, playersArea;
    @FXML
    private TextField newTeamName;
    @FXML
    private Button createTeamButton, addPlayerButton;
    @FXML
    private BorderPane borderPane;

    private User loggedUser;
    private int currentTeamId;

    void userInit(User currentUser, BorderPane pane) {
        loggedUser = currentUser;
        borderPane = pane;
        if(loggedUser.getUserType() == User.Type.COACH) {
            if(coachHasTeam(loggedUser.getId())) {
                wholeArea.getChildren().remove(teamCreationArea);
                playersArea.setVisible(true);
            }
        } else {

        }

    }

    /**
     * @param coachId id of logged user->coach
     * @return if currently logged coach has team
     */
    private boolean coachHasTeam(int coachId) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select d.id_d, d.nazwa from szkolka.druzyna as d " +
                    "join szkolka.uzytkownik as u using(id_u) where id_u=" + coachId + ";");
            if(rs.next()) {
                mainText.setText("Nazwa drużyny: " + rs.getString("nazwa"));
                currentTeamId = rs.getInt("id_d");
                st.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mainText.setText("Stwórz drużynę:");
        return false;
    }

    /**
     *  being called on click createTeamButton
     *  add to DB new team with currently logged user as coach
     */
    @FXML
    private void createTeam() {
        if(!newTeamName.getText().isEmpty())
        {
            try {
                Connection conn = DatabaseHandler.getInstance().getConnection();
                Statement st = conn.createStatement();
                st.execute("insert into szkolka.druzyna(id_u, nazwa) values(" + loggedUser.getId() +
                        ", '" + newTeamName.getText() + "');");
                st.close();

                wholeArea.getChildren().remove(teamCreationArea);
                playersArea.setVisible(true);
                coachHasTeam(loggedUser.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            newTeamName.setPromptText("POLE NIE MOŻE BYĆ PUSTE");
        }
    }

    @FXML
    private void addPlayer() {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addPlayer.fxml"));
            root = loader.load();
            AddPlayerController apController = loader.getController();
            apController.init(borderPane, currentTeamId, loggedUser);

        } catch (IOException e) {
            e.printStackTrace();
        }
        borderPane.setCenter(root);
    }
}
