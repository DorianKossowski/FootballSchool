package main;

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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import login.LoginController;

import java.io.IOException;
import java.net.URL;
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

    /**
     * @param u currently logged user
     */
    public MainController(User u) {
        currentUser = u;
    }

    /**
     * sets welcome text and assigns logout button
     * loads proper content to scene
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        helloUser.setText("Witaj " + currentUser.getName());
        logoutButton.setOnAction(this::userLogout);

        Parent root = null;

        if(currentUser.getUserType() == User.Type.ADMIN) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("adminCoaches.fxml"));
                root = loader.load();
                ACoachesController aController = loader.getController();
                aController.setListeners();
                adminMenuBar();
            } catch (IOException e) {
                e.printStackTrace();
            }
            borderPane.setCenter(root);
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
     * call on click coachesButton
     */
    @FXML
    private void coachesOnClick() {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("adminCoaches.fxml"));
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
     * call on click monthsButton
     */
    @FXML
    private void monthsOnClick() {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("adminMonths.fxml"));
            root = loader.load();
            AMonthsController aController = loader.getController();
//            aController.setListeners();
            adminMenuBar();
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
}
