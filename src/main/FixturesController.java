package main;

import general.DatabaseHandler;
import general.User;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;

public class FixturesController {

    @FXML
    private Text teamName, warningText;
    @FXML
    private TableView<Match> matchesTable;
    @FXML
    private TableColumn<Match, String> dateCol, opponentCol, addressCol;
    @FXML
    private TextField opponentTextField, addressTextField;
    @FXML
    private DatePicker datePic;

    private User loggedUser;
    private int currentTeamId;

    /**
     * supplementary match class necessary to table creation
     */
    public static class Match {
        private final SimpleStringProperty date = new SimpleStringProperty();
        private final SimpleStringProperty opponent = new SimpleStringProperty();
        private final SimpleStringProperty address = new SimpleStringProperty();

        private Match(String date, String opponent, String address) {
            this.date.set(date);
            this.opponent.set(opponent);
            this.address.set(address);
        }


        public String getDate() {
            return date.get();
        }

        public String getOpponent() {
            return opponent.get();
        }

        public String getAddress() {
            return address.get();
        }
    }

    /**
     * @param currentUser currently logged user
     * assigns table columns and get records from DB
     */
    void userInit(User currentUser) {
        this.loggedUser = currentUser;

        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        opponentCol.setCellValueFactory(new PropertyValueFactory<>("opponent"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        getFixturesFromDB();
        if(loggedUser.getUserType() == User.Type.COACH) {

        } else {

        }
    }

    /**
     * checks if value has been changed in editable fields
     */
    @FXML
    private void setListener() {
        if(warningText.isVisible()) {
            warningText.setVisible(false);
        }
    }

    /**
     * adjusts table height towards number of rows
     */
    private void setTableHeight() {
        matchesTable.prefHeightProperty().bind(matchesTable.fixedCellSizeProperty().
                multiply(Bindings.size(matchesTable.getItems())).add(20).add(15));  //margin + header height
    }

    /**
     * read stored matches in database and fill table
     */
    private void getFixturesFromDB() {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement st = conn.createStatement();

            ObservableList<Match> matchesInDB = FXCollections.observableArrayList();

            ResultSet rs = st.executeQuery("select d.id_d, d.nazwa from szkolka.druzyna as d " +
                    "join szkolka.uzytkownik as u using(id_u) where id_u=" + loggedUser.getId() + ";");
            if(rs.next()) {
                currentTeamId = rs.getInt("id_d");
                teamName.setText(rs.getString("nazwa"));
            }
            rs = st.executeQuery("select m.* from szkolka.druzyna d join szkolka.mecz m using(id_d) where id_d=" +
                    currentTeamId + " order by m.data;");
            while(rs.next()) {
                matchesInDB.add(new Match(rs.getString("data"), rs.getString("przeciwnik"),
                        rs.getString("adres")));
            }
            matchesTable.setItems(matchesInDB);
            setTableHeight();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * insert into DB new match record
     */
    @FXML
    private void addNewMatch() {
        try {
            if(opponentTextField.getText().isEmpty() || addressTextField.getText().isEmpty()) {
                throw new NullPointerException();
            }
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement st = conn.createStatement();

            String chosenDate = datePic.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
            st.execute("insert into szkolka.mecz(id_d, data, przeciwnik, adres) values(" +
                    currentTeamId + ", '" + chosenDate + "', '" + opponentTextField.getText() +
                    "', '" + addressTextField.getText() + "');");
            st.close();
            opponentTextField.setText("");
            addressTextField.setText("");
            datePic.setValue(null);
            getFixturesFromDB();
        } catch (NullPointerException | SQLException e) {
            warningText.setVisible(true);
        }
    }
}