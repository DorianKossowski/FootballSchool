package main.team;

import general.DatabaseHandler;
import general.User;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

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
    private Button createTeamButton, addPlayerButton, parentButton;
    @FXML
    private BorderPane borderPane;
    @FXML
    private TableView<Player> playersTable;
    @FXML
    private TableColumn<Player, String> nameCol, surnameCol;
    @FXML
    private TableColumn<Player, Integer> yearCol;

    private User loggedUser;
    private int currentTeamId;
    private Button homeButton, fixturesButton, paymentsButton;


    /**
     * supplementary player class necessary to table creation
     */
    public static class Player {
        private final SimpleIntegerProperty id_p = new SimpleIntegerProperty();
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty surname = new SimpleStringProperty();
        private final SimpleIntegerProperty year = new SimpleIntegerProperty();

        private final String phone;

        Player(int id_p, String name, String surname, Integer year, String phone) {
            this.id_p.set(id_p);
            this.name.set(name);
            this.surname.set(surname);
            this.year.set(year);
            this.phone = phone;
        }

        public int getId_p() {
            return id_p.get();
        }

        public String getName() {
            return name.get();
        }

        public String getSurname() {
            return surname.get();
        }

        public int getYear() {
            return year.get();
        }

        public String getPhone() {
            return this.phone;
        }
    }

    public void userInit(User currentUser, BorderPane pane) {
        loggedUser = currentUser;
        borderPane = pane;
        if(loggedUser.getUserType() == User.Type.COACH) {
            if(coachHasTeam(loggedUser.getId())) {
                wholeArea.getChildren().remove(teamCreationArea);

                nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
                surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
                yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
                setPlayersTable();
                playersArea.setVisible(true);
            }
        } else {

        }
    }

    /**
     * @param homeButton connected with home tab
     * @param fixturesButton connected with fixtures tab
     * @param paymentsButton connected with payments tab
     * buttons objects necessary to unlock features after team creation
     */
    public void buttonsInit(Button homeButton, Button fixturesButton, Button paymentsButton) {
        this.homeButton = homeButton;
        this.fixturesButton = fixturesButton;
        this.paymentsButton = paymentsButton;
    }

    /**
     * being called after any action on table
     */
    @FXML
    private void rowSelected() {
        if (playersTable.getItems().size() > 0) {
            parentButton.setDisable(false);
        }
    }

    /**
     * available after row selection
     * change scene to parent info scene
     */
    @FXML
    private void checkParent() {
        Player selectedPlayer = playersTable.getSelectionModel().getSelectedItem();

        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("checkParent.fxml"));
            root = loader.load();
            CheckParentController cpController = loader.getController();
            cpController.init(borderPane, loggedUser ,selectedPlayer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        borderPane.setCenter(root);
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
                mainText.setText(rs.getString("nazwa"));
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
                homeButton.setDisable(false);
                fixturesButton.setDisable(false);
                paymentsButton.setDisable(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            newTeamName.setPromptText("POLE NIE MOŻE BYĆ PUSTE");
        }
    }

    /**
     * being called on click addPlayerButton
     * changes scene to player creation scene
     */
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

    /**
     * adjusts table height towards number of rows
     */
    private void setTableHeight() {
        playersTable.prefHeightProperty().bind(playersTable.fixedCellSizeProperty().
                multiply(Bindings.size(playersTable.getItems())).add(20).add(15));  //margin + header height
    }

    /**
     * loads players from current team stored in DB
     */
    private void setPlayersTable() {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement st = conn.createStatement();

            ObservableList<TeamController.Player> playersInDB = FXCollections.observableArrayList();

            ResultSet rs = st.executeQuery("select * from szkolka.pilkarz where id_d=" + currentTeamId + ";");
            while(rs.next()) {
                playersInDB.add(new Player(rs.getInt("id_p"), rs.getString("imie"), rs.getString("nazwisko"),
                        rs.getInt("rocznik"), rs.getString("telefon")));
            }
            playersTable.setItems(playersInDB);
            setTableHeight();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
