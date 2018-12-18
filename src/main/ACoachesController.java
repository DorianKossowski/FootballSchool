package main;

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
    private Button addCoachButton;
    @FXML
    private Text warningText;

    public static class Coach {
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty surname = new SimpleStringProperty();
        private final SimpleStringProperty login = new SimpleStringProperty();
        private final SimpleStringProperty password = new SimpleStringProperty();

        Coach(String name, String surname, String login, String password) {
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

    private void setTableHeight() {
        coachesTable.prefHeightProperty().bind(coachesTable.fixedCellSizeProperty().
                multiply(Bindings.size(coachesTable.getItems())).add(20).add(15));  //margin + header height
    }

    /**
     * loads from coaches stored in DB
     */
    private void setCoachesTable() {
        try {
            Connection conn = DatabaseHandler.getConnection();
            Statement st = conn.createStatement();

            ObservableList<Coach> coachesInDB = FXCollections.observableArrayList();

            ResultSet rs = st.executeQuery("select * from szkolka.uzytkownik where id_tu=2;");
            while(rs.next()) {
                coachesInDB.add(new Coach(rs.getString("imie"), rs.getString("nazwisko"),
                        rs.getString("login"), rs.getString("haslo")));
            }
            coachesTable.setItems(coachesInDB);
            setTableHeight();
            st.close();
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
            Connection conn = DatabaseHandler.getConnection();
            Statement st = conn.createStatement();

            st.execute("insert into szkolka.uzytkownik(imie, nazwisko, id_tu) values('" +
                    coachName.getText() + "', '" + coachSurname.getText() + "', 2);");
            st.close();
            coachName.setText("");
            coachSurname.setText("");
            setCoachesTable();
        } catch (SQLException e) {
            warningText.setVisible(true);
        }
    }
}
