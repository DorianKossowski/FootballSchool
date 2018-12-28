package main;

import general.DatabaseHandler;
import general.User;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HomeController {

    @FXML
    private Text opponentText, addressText, dateText;

    private User loggedUser;

    public void userInit(User currentUser) {
        this.loggedUser = currentUser;
        setNextMatch();
    }

    /**
     * sets proper values to texts about upcoming match
     */
    private void setNextMatch() {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select d.id_d, d.nazwa from szkolka.druzyna as d " +
                    "join szkolka.uzytkownik as u using(id_u) where id_u=" + loggedUser.getId() + ";");
            if(rs.next()) {
                int currentTeamId = rs.getInt("id_d");
                rs = st.executeQuery("select * from szkolka.mecz where id_d=" + currentTeamId +
                        " and data>=current_date order by data;");
                if(rs.next()) {
                    opponentText.setText(rs.getString("przeciwnik"));
                    addressText.setText(rs.getString("adres"));
                    dateText.setText(rs.getString("data"));
                } else {
                    opponentText.setText("Brak nadchodzących meczy");
                    addressText.setVisible(false);
                    dateText.setVisible(false);
                }
                st.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
