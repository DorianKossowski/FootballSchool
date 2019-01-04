package main;

import general.DatabaseHandler;
import general.User;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class PaymentsController  implements Initializable {

    @FXML
    private ComboBox<Integer> yearToAddBox, yearBox;
    @FXML
    private ComboBox<String> playerBox, monthsBox;
    @FXML
    private Text teamName;
    @FXML
    private Button addButton;

    @FXML
    private TableView<Payment> paymentsTable;
    @FXML
    private TableColumn<Payment, String> nameCol, monthCol;

    private User loggedUser;
    private boolean addPaymentFlag1, addPaymentFlag2;
    private List<Map.Entry<Integer, String>> players = new ArrayList<>();
    private int currentTeamId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        monthCol.setCellValueFactory(new PropertyValueFactory<>("month"));
    }

    /**
     * supplementary coach class necessary to table creation
     */
    public static class Payment {
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty month = new SimpleStringProperty();

        Payment(String name, String month) {
            this.name.set(name);
            this.month.set(month);
        }

        public String getName() {
            return name.get();
        }

        public String getMonth() {
            return month.get();
        }
    }

    /**
     * @param currentUser currently logged user
     * calls proper methods which prepare the scene
     */
    public void userInit(User currentUser) {
        loggedUser = currentUser;

        getTeam();
        initBoxes();
        setTable();
    }

    /**
     * read from DB correct team
     */
    private void getTeam() {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select d.id_d, d.nazwa from szkolka.druzyna as d " +
                        "join szkolka.uzytkownik as u using(id_u) where id_u=" + loggedUser.getId() + ";")) {
                    if (rs.next()) {
                        teamName.setText(rs.getString("nazwa"));
                        currentTeamId = rs.getInt("id_d");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * adjusts table height towards number of rows
     */
    private void setTableHeight() {
        paymentsTable.prefHeightProperty().bind(paymentsTable.fixedCellSizeProperty().
                multiply(Bindings.size(paymentsTable.getItems())).add(20).add(15));  //margin + header height
    }

    /**
     * fills the paymentsTable with values from DB and calls method which set proper table's height
     */
    private void setTable() {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select * from szkolka.wplata as w join szkolka.miesiac as m " +
                        "using(id_m) join szkolka.pilkarz as p using(id_p) where w.rok=" + yearBox.getValue() + " and p.id_d=" +
                        currentTeamId + ";")) {

                    ObservableList<Payment> paymentsInDB = FXCollections.observableArrayList();
                    while(rs.next()) {
                        paymentsInDB.add(new Payment(rs.getString("imie") + " " + rs.getString("nazwisko"),
                                rs.getString("nazwa")));
                    }
                    paymentsTable.setItems(paymentsInDB);
                    setTableHeight();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * being called after any action on monthsBox
     */
    @FXML
    private void setAddPaymentFlag1() {
        addPaymentFlag1 = true;
        if(addPaymentFlag2) {
            addButton.setDisable(false);
        }
    }

    /**
     * being called after any action on playerBox
     */
    @FXML
    private void setAddPaymentFlag2() {
        addPaymentFlag2 = true;
        if(addPaymentFlag1) {
            addButton.setDisable(false);
        }
    }

    /**
     * being called after action on yearBox
     */
    @FXML
    private void changeYear() {
        setTable();
    }

    /**
     * add available values of ComboBox to choose - players, months and years
     */
    private void initBoxes() {
        ObservableList<String> availablePlayers = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select p.* from szkolka.pilkarz as p join szkolka.druzyna as d " +
                        "using(id_d) where d.id_u=" + loggedUser.getId() + ";")) {
                    while(rs.next()) {
                        int tempId = rs.getInt("id_p");
                        String tempName = rs.getString("imie") + " " + rs.getString("nazwisko");
                        players.add(Map.entry(tempId, tempName));
                        availablePlayers.add(tempName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        playerBox.setItems(availablePlayers);

        ObservableList<String> availableMonths = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select * from szkolka.miesiac;")) {
                    while(rs.next()) {
                        availableMonths.add(rs.getString("nazwa"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        monthsBox.setItems(availableMonths);

        ObservableList<Integer> availableYears = FXCollections.observableArrayList();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for(int year = 2018; year <= currentYear; ++year){
            availableYears.add(year);
        }
        yearToAddBox.setItems(availableYears);
        yearToAddBox.setValue(currentYear);
        yearBox.setItems(availableYears);
        yearBox.setValue(currentYear);
    }
}
