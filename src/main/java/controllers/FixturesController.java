package main.java.controllers;

import main.java.general.DatabaseHandler;
import main.java.general.User;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
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
    @FXML
    private Button removeButton;
    @FXML
    private VBox wholeArea, addMatchVBox;

    private User loggedUser;
    private int currentTeamId;

    /**
     * supplementary match class necessary to table creation
     */
    public static class Match {
        private final SimpleStringProperty date = new SimpleStringProperty();
        private final SimpleStringProperty opponent = new SimpleStringProperty();
        private final SimpleStringProperty address = new SimpleStringProperty();

        private final int id;

        private Match(String date, String opponent, String address, int id) {
            this.id = id;
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

        public int getId() {
            return id;
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

        if(loggedUser.getUserType() == User.Type.COACH) {
            getFixturesFromDB("select d.id_d, d.nazwa from szkolka.druzyna as d " +
                    "join szkolka.uzytkownik as u using(id_u) where id_u=" + loggedUser.getId() + ";");
        } else {
            //PARENT
            wholeArea.getChildren().remove(addMatchVBox); wholeArea.getChildren().remove(removeButton);
            getFixturesFromDB("select d.* from szkolka.pilkarz as p join szkolka.uzytkownik as u using(id_u)" +
                    " join szkolka.druzyna as d using(id_d) where u.id_u=" + loggedUser.getId() + ";");
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
    private void getFixturesFromDB(String getTeamQuery) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                ObservableList<Match> matchesInDB = FXCollections.observableArrayList();
                try (ResultSet rs = st.executeQuery(getTeamQuery)) {
                    if (rs.next()) {
                        currentTeamId = rs.getInt("id_d");
                        teamName.setText(rs.getString("nazwa"));
                        try (ResultSet rs2 = st.executeQuery("select m.* from szkolka.druzyna d join szkolka.mecz m using(id_d) where id_d=" +
                                currentTeamId + " order by m.data;")) {
                            while (rs2.next()) {
                                matchesInDB.add(new Match(rs2.getString("data"), rs2.getString("przeciwnik"),
                                        rs2.getString("adres"), rs2.getInt("id_m")));
                            }
                            matchesTable.setItems(matchesInDB);
                            setTableHeight();
                        }
                    }
                }
            }
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
            try (Statement st = conn.createStatement()) {
                String chosenDate = datePic.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
                st.execute("insert into szkolka.mecz(id_d, data, przeciwnik, adres) values(" +
                        currentTeamId + ", '" + chosenDate + "', '" + opponentTextField.getText() +
                        "', '" + addressTextField.getText() + "');");
            }
            opponentTextField.setText("");
            addressTextField.setText("");
            datePic.setValue(null);
            getFixturesFromDB("select d.id_d, d.nazwa from szkolka.druzyna as d " +
                    "join szkolka.uzytkownik as u using(id_u) where id_u=" + loggedUser.getId() + ";");
        } catch (NullPointerException | SQLException e) {
            warningText.setVisible(true);
        }
    }

    /**
     * being called after any action on table
     */
    @FXML
    private void rowSelected() {
        if (matchesTable.getSelectionModel().getSelectedItem() != null) {
            removeButton.setDisable(false);
        }
    }

    /**
     * being called after on click removeButton
     * delete selected record from DB
     */
    @FXML
    private void setRemoveButton() {
        Match selectedRow = matchesTable.getSelectionModel().getSelectedItem();
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                st.execute("delete from szkolka.mecz where id_m=" + selectedRow.getId() +";");
                matchesTable.getItems().remove(matchesTable.getSelectionModel().getSelectedItem());
                removeButton.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}