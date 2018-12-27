package main;

import general.DatabaseHandler;
import general.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import login.LoginController;
import main.admin.ACoachesController;
import main.team.TeamController;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Text helloUser;
    @FXML
    private Button homeButton, fixturesButton, teamButton, paymentsButton, coachesButton,
            monthsButton, changePassButton, logoutButton;
    @FXML
    private BorderPane borderPane;

    private User currentUser;
    private Button activeTab;

    /**
     * @param u currently logged user
     */
    public MainController(User u) {
        currentUser = u;
    }

    /**
     * @param newActiveTab currently active tab in menu
     * sets properties to active tab button in menu
     */
    private void setSelectedFont(Button newActiveTab) {
        if(newActiveTab != activeTab) {
            activeTab.setFont(Font.font("System", 18));
            activeTab.setMouseTransparent(false);
            activeTab = newActiveTab;
        }
        activeTab.setFont(Font.font("System", FontWeight.BOLD, 18));
        activeTab.setMouseTransparent(true);
    }

    /**
     * sets welcome text and assigns logout button
     * loads proper content to scene
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activeTab = homeButton;
        setSelectedFont(homeButton);
        helloUser.setText("Witaj " + currentUser.getName());
        logoutButton.setOnAction(this::userLogout);

        Parent root = null;

        if(currentUser.getUserType() == User.Type.ADMIN) {
            setSelectedFont(coachesButton);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("admin/adminCoaches.fxml"));
                root = loader.load();
                ACoachesController aController = loader.getController();
                aController.setListeners();
                adminMenuBar();
            } catch (IOException e) {
                e.printStackTrace();
            }
            borderPane.setCenter(root);
        } else if(currentUser.getUserType() == User.Type.COACH && !coachHasTeam(currentUser.getId())) {
            fixturesButton.setDisable(true);
            paymentsButton.setDisable(true);
        }
    }

    /**
     * sets buttons properties for admin in menu bar
     */
    private void adminMenuBar() {
        homeButton.setDisable(true); fixturesButton.setDisable(true);
        teamButton.setDisable(true); paymentsButton.setDisable(true);
        coachesButton.setVisible(true); monthsButton.setVisible(true);
    }

    /**
     * being called on click coachesButton
     */
    @FXML
    private void coachesOnClick() {
        setSelectedFont(coachesButton);
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin/adminCoaches.fxml"));
            root = loader.load();
            ACoachesController aController = loader.getController();
            aController.setListeners();
            adminMenuBar();
        } catch (IOException e) {
            e.printStackTrace();
        }
        borderPane.setCenter(root);
    }

    /**
     * being called on click monthsButton
     */
    @FXML
    private void monthsOnClick() {
        setSelectedFont(monthsButton);
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin/adminMonths.fxml"));
            root = loader.load();
            adminMenuBar();
        } catch (IOException e) {
            e.printStackTrace();
        }
        borderPane.setCenter(root);
    }

    /**
     * being called on click changePassButton
     */
    @FXML
    private void changePassOnClick() {
        setSelectedFont(changePassButton);
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("changePassword.fxml"));
            root = loader.load();
            ChangePassController cpController = loader.getController();
            cpController.userInit(currentUser);
            if(currentUser.getUserType() == User.Type.ADMIN)
                adminMenuBar();
        } catch (IOException e) {
            e.printStackTrace();
        }
        borderPane.setCenter(root);
    }

    /**
     * being called on click teamButton
     */
    @FXML
    private void teamOnClick() {
        setSelectedFont(teamButton);
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("team/team.fxml"));
            root = loader.load();
            TeamController tController = loader.getController();
            tController.userInit(currentUser, borderPane);

        } catch (IOException e) {
            e.printStackTrace();
        }
        borderPane.setCenter(root);
    }

    /**
     * being called on click fixturesButton
     */
    @FXML
    private void fixturesOnClick() {
        setSelectedFont(fixturesButton);
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fixtures.fxml"));
            root = loader.load();
            FixturesController fController = loader.getController();
            fController.userInit(currentUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
        borderPane.setCenter(root);
    }

    /**
     * @param event connected with LOGOUT
     * changes scene to login scene
     */
    private void userLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../login/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            LoginController lcontroller = loader.getController();
            lcontroller.setListeners(scene);
        } catch (IOException io) {
            io.printStackTrace();
        }
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
                st.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
