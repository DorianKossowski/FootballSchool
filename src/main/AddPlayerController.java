package main;

import general.DatabaseHandler;
import general.User;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

public class AddPlayerController {

    @FXML
    private BorderPane borderPane;
    @FXML
    private Button backButton, addButton;
    @FXML
    private ComboBox<Integer> yearBox;
    @FXML
    private TextField playerName, playerSurname, parentName, parentSurname;
    @FXML
    private Text warningText;

    private int teamId;
    private User loggedUser;
    private boolean yearIsValid;

    void init(BorderPane borderPane, int teamId, User loggedUser) {
        this.borderPane = borderPane;
        this.teamId = teamId;
        this.loggedUser = loggedUser;

        setYearBox();
        setListeners();
    }


    @FXML
    private void handleYearBox() {
        if(warningText.isVisible()) {
            warningText.setVisible(false);
        }
        yearIsValid = true;
    }

    private ChangeListener<String> textListener() {
        return (observable, oldValue, newValue) -> {
            if(warningText.isVisible()) {
                warningText.setVisible(false);
            }
        };
    }

    /**
     * listeners that handle new player validation
     */
    private void setListeners() {
        playerName.textProperty().addListener(textListener());
        playerSurname.textProperty().addListener(textListener());
        parentName.textProperty().addListener(textListener());
        parentSurname.textProperty().addListener(textListener());
    }

    private void setYearBox() {
        final int MINYEAR = 4;
        final int MAXYEAR = 18;
        ObservableList<Integer> availableYears = FXCollections.observableArrayList();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for(int year = currentYear - MAXYEAR; year <= currentYear - MINYEAR; ++year){
            availableYears.add(year);
        }
        yearBox.setItems(availableYears);
    }

    @FXML
    private void backToPreviousScene() {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("team.fxml"));
            root = loader.load();
            TeamController tController = loader.getController();
            tController.userInit(loggedUser, borderPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
        borderPane.setCenter(root);
    }

    @FXML
    private void addNewPlayer() {
        if(playerName.getText().isEmpty() || playerSurname.getText().isEmpty() || !yearIsValid ||
        parentName.getText().isEmpty() || parentSurname.getText().isEmpty()) {
            warningText.setText("Uzupełnij wszystkie pola");
            warningText.setVisible(true);
            return;
        }

        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();;
            Statement st = conn.createStatement();

            ResultSet rs = st.executeQuery("insert into szkolka.uzytkownik(imie, nazwisko, id_tu) values('" +
                    parentName.getText() + "', '" + parentSurname.getText() + "', 3) returning id_u;");
            if(rs.next()) {
                int id_u = rs.getInt("id_u");
                st.execute("insert into szkolka.pilkarz(id_u, id_d, imie, nazwisko, rocznik) values(" + id_u +
                ", " + teamId + ", '" + playerName.getText() + "', '" + playerSurname.getText() + "', " + yearBox.getValue() + ");");
            }
            st.close();

            playerName.setText(""); playerSurname.setText("");
            parentName.setText(""); parentSurname.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
            warningText.setText("Podano złe dane");
            warningText.setVisible(true);
        }
    }
}
