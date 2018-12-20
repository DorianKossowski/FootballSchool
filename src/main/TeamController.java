package main;

import general.DatabaseHandler;
import general.User;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TeamController {
    @FXML
    private Text mainText;
    @FXML
    private VBox wholeArea, teamCreationArea, playersArea;

    private int currentTeamId;

    void userInit(User currentUser) {
        if(currentUser.getUserType() == User.Type.COACH) {
            if(coachHasTeam(currentUser.getId())) {
                wholeArea.getChildren().remove(teamCreationArea);
                playersArea.setVisible(true);
            }
            else {

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
            Connection conn = DatabaseHandler.getConnection();
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
}
