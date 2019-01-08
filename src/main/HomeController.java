package main;

import general.DatabaseHandler;
import general.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeController {

    @FXML
    private Text opponentText, addressText, dateText;
    @FXML
    private TextField newMessageField;
    @FXML
    private VBox messagesVBox, wholeArea;
    @FXML
    private HBox addHBox;
    @FXML
    private Button addButton;

    private User loggedUser;
    private int currentTeamId;

    public void userInit(User currentUser) {
        this.loggedUser = currentUser;

        if (loggedUser.getUserType() == User.Type.COACH) {
            setNextMatch("select d.id_d, d.nazwa from szkolka.druzyna as d " +
                    "join szkolka.uzytkownik as u using(id_u) where id_u=" + loggedUser.getId() + ";");
        } else {
            //PARENT
            setNextMatch("select d.* from szkolka.pilkarz as p join szkolka.uzytkownik as u using(id_u)" +
                    " join szkolka.druzyna as d using(id_d) where u.id_u=" + loggedUser.getId() + ";");
            addHBox.getChildren().remove(newMessageField); addHBox.getChildren().remove(addButton);
            Text parentText = new Text("Wiadomości od trenera:");
            parentText.setStyle("-fx-font: 22 system;");
            addHBox.getChildren().add(parentText);
        }
        readMessages();
    }

    /**
     * sets proper values to texts about upcoming match
     */
    private void setNextMatch(String getTeamQuery) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery(getTeamQuery)) {
                    if (rs.next()) {
                        currentTeamId = rs.getInt("id_d");
                        try (ResultSet rs2 = st.executeQuery("select * from szkolka.mecz where id_d=" + currentTeamId +
                                " and data>=current_date order by data;")) {
                            if (rs2.next()) {
                                opponentText.setText(rs2.getString("przeciwnik"));
                                addressText.setText(rs2.getString("adres"));
                                dateText.setText(rs2.getString("data"));
                            } else {
                                opponentText.setText("Brak nadchodzących meczy");
                                addressText.setVisible(false);
                                dateText.setVisible(false);
                            }
                        }
                    }
                }
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
            try (Statement st = conn.createStatement()) {
                st.execute("insert into szkolka.wiadomosc(id_d, data, tresc) values(" + currentTeamId +
                        ", current_date, '" + newMessageField.getText() + "');");
            }
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
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select * from szkolka.wiadomosc where id_d=" + currentTeamId +
                        "order by id_w desc;")) {
                    while (rs.next()) {
                        TextArea msg = new TextArea(rs.getString("data") + "\n" + rs.getString("tresc"));
                        msg.setMaxWidth(450);
                        msg.setPrefRowCount(2);
                        msg.setEditable(false);
                        messagesVBox.getChildren().add(msg);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
