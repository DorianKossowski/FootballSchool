package main.admin;

import general.DatabaseHandler;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class ACoachesController implements Initializable {

    @FXML
    private TableView<Coach> coachesTable;
    @FXML
    private TableColumn<Coach, String> nameCol, surnameCol, loginCol, passwordCol;

    @FXML
    private TextField coachName, coachSurname;
    @FXML
    private Text warningText;
    @FXML
    private Button removeButton;

    /**
     * supplementary coach class necessary to table creation
     */
    public static class Coach {
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty surname = new SimpleStringProperty();
        private final SimpleStringProperty login = new SimpleStringProperty();
        private final SimpleStringProperty password = new SimpleStringProperty();

        private final int id;

        Coach(String name, String surname, String login, String password, int id) {
            this.id = id;
            this.name.set(name);
            this.surname.set(surname);
            this.login.set(login);
            this.password.set(password);
        }

        public String getName() {
            return name.get();
        }

        public String getSurname() {
            return surname.get();
        }

        public String getLogin() {
            return login.get();
        }

        public String getPassword() {
            return password.get();
        }

        public int getId() {
            return id;
        }
    }

    /**
     * listeners that handle new coach validation
     */
    public void setListeners() {
        coachName.textProperty().addListener( observable -> {
            if(warningText.isVisible()) {
                warningText.setVisible(false);
            }
        });
        coachSurname.textProperty().addListener( observable -> {
            if(warningText.isVisible()) {
                warningText.setVisible(false);
            }
        });
    }

    /**
     * assigns table columns and fill table with coaches from DB
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));

        setCoachesTable();
        setTableHeight();

    }

    /**
     * adjusts table height towards number of rows
     */
    private void setTableHeight() {
        coachesTable.prefHeightProperty().bind(coachesTable.fixedCellSizeProperty().
                multiply(Bindings.size(coachesTable.getItems())).add(20).add(15));  //margin + header height
    }

    /**
     * loads coaches stored in DB
     */
    private void setCoachesTable() {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();;
            try (Statement st = conn.createStatement()) {
                ObservableList<Coach> coachesInDB = FXCollections.observableArrayList();
                try (ResultSet rs = st.executeQuery("select * from szkolka.uzytkownik where id_tu=2;")) {
                    while (rs.next()) {
                        coachesInDB.add(new Coach(rs.getString("imie"), rs.getString("nazwisko"),
                                rs.getString("login"), rs.getString("haslo"), rs.getInt("id_u")));
                    }
                    coachesTable.setItems(coachesInDB);
                    setTableHeight();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create and save to DB new coach
     * update table presents on scene
     */
    @FXML
    private void addNewCoach() {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                st.execute("insert into szkolka.uzytkownik(imie, nazwisko, id_tu) values('" +
                        coachName.getText() + "', '" + coachSurname.getText() + "', 2);");
                st.close();
                coachName.setText("");
                coachSurname.setText("");
                setCoachesTable();
            }
        } catch (SQLException e) {
            warningText.setVisible(true);
        }
    }

    /**
     * being called after any action on table
     */
    @FXML
    private void rowSelected() {
        if (coachesTable.getSelectionModel().getSelectedItem() != null) {
            removeButton.setDisable(false);
        }
    }

    /**
     * being called after on click removeButton
     * delete selected record from DB
     */
    @FXML
    private void setRemoveButton() {
        Coach selectedRow = coachesTable.getSelectionModel().getSelectedItem();
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                st.execute("delete from szkolka.uzytkownik where id_u=" + selectedRow.getId() + ";");
                coachesTable.getItems().remove(coachesTable.getSelectionModel().getSelectedItem());
                removeButton.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
