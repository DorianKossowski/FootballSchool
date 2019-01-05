package main;

import general.DatabaseHandler;
import general.User;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class HomeController {

    @FXML
    private Text opponentText, addressText, dateText;
    @FXML
    private TextField newMessageField;
    @FXML
    private VBox messagesVBox;

    private User loggedUser;
    private int currentTeamId;

    public void userInit(User currentUser) {
        this.loggedUser = currentUser;
        setNextMatch();
        readMessages();
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
            if (rs.next()) {
                currentTeamId = rs.getInt("id_d");
                rs = st.executeQuery("select * from szkolka.mecz where id_d=" + currentTeamId +
                        " and data>=current_date order by data;");
                if (rs.next()) {
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

    /**
     * being called after click on addButton
     * enter new coach's message in DB
     */
    @FXML
    private void addMessage() {
        if(newMessageField.getText().isEmpty()) {
            newMessageField.setPromptText("POLE NIE MOŻE BYĆ PUSTE");
            return;
        }
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement st = conn.createStatement();
            st.execute("insert into szkolka.wiadomosc(id_d, data, tresc) values(" + currentTeamId +
                    ", current_date, '" + newMessageField.getText() + "');");
            st.close();

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            TextArea msg = new TextArea(dateFormat.format(date) + "\n" + newMessageField.getText());
            msg.setMaxWidth(450);
            msg.setPrefRowCount(2);
            msg.setEditable(false);
            messagesVBox.getChildren().add(0, msg);

            newMessageField.setText("");
            } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * creates TextAreas with messages from DB
     */
    private void readMessages() {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement st = conn.createStatement();
            ResultSet rs =  st.executeQuery("select * from szkolka.wiadomosc where id_d=" + currentTeamId +
                    "order by id_w desc;");
            while (rs.next()) {
                TextArea msg = new TextArea(rs.getString("data") + "\n" + rs.getString("tresc"));
                msg.setMaxWidth(450);
                msg.setPrefRowCount(2);
                msg.setEditable(false);
                messagesVBox.getChildren().add(msg);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}