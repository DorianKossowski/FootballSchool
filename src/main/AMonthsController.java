package main;

import general.DatabaseHandler;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class AMonthsController implements Initializable {

    @FXML
    private TableView<String> monthsTable;
    @FXML
    private TableColumn<String, String> nameCol;
    @FXML
    private TextField monthName;

    /**
     * assigns table column and fill table with months from DB
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        setMonthsTable();
    }

    /**
     * adjusts table height towards number of rows
     */
    private void setTableHeight() {
        monthsTable.prefHeightProperty().bind(monthsTable.fixedCellSizeProperty().
                multiply(Bindings.size(monthsTable.getItems())).add(20).add(15));  //margin + header height
    }

    /**
     * loads from months stored in DB
     */
    private void setMonthsTable() {
        try {
            Connection conn = DatabaseHandler.getConnection();
            Statement st = conn.createStatement();

            ObservableList<String> monthsInDB = FXCollections.observableArrayList();

            ResultSet rs = st.executeQuery("select nazwa from szkolka.miesiac;");
            while(rs.next()) {
                monthsInDB.add(rs.getString("nazwa"));
            }
            monthsTable.setItems(monthsInDB);
            setTableHeight();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create and save to DB new month
     * update table presents on scene
     */
    @FXML
    private void addNewMonth() {
        try {
            Connection conn = DatabaseHandler.getConnection();
            Statement st = conn.createStatement();

            st.execute("insert into szkolka.miesiac(nazwa) values('" + monthName.getText() + "');");
            st.close();
            monthName.setText("");
            setMonthsTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
