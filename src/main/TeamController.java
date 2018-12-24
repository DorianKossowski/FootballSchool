package main;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.scene.control.TextField;
import main.admin.ACoachesController;

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



    /**
     * supplementary player class necessary to table creation
     */
    public static class Player {
        private final SimpleIntegerProperty id_p = new SimpleIntegerProperty();
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty surname = new SimpleStringProperty();
        private final SimpleIntegerProperty year = new SimpleIntegerProperty();

        Player(int id_p, String name, String surname, Integer year) {
            this.id_p.set(id_p);
            this.name.set(name);
            this.surname.set(surname);
            this.year.set(year);
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
    }

    void userInit(User currentUser, BorderPane pane) {
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

    @FXML
    private void rowSelected() {
        parentButton.setDisable(false);
    }

    @FXML
    private void checkParent() {
        Player selectedPlayer = playersTable.getSelectionModel().getSelectedItem();
        System.out.println(selectedPlayer.name);
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
                        rs.getInt("rocznik")));
            }
            playersTable.setItems(playersInDB);
            setTableHeight();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
