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

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class AMonthsController implements Initializable {

    @FXML
    private TableView<Month> monthsTable;
    @FXML
    private TableColumn<Month, String> nameCol;
    @FXML
    private TextField monthName;
    @FXML
    private Button removeButton;


    /**
     * supplementary month class necessary to table creation
     */
    public static class Month {
        private final SimpleStringProperty name = new SimpleStringProperty();

        private final int id;

        public Month(int id, String name) {
            this.id = id;
            this.name.set(name);
        }

        public String getName() {
            return name.get();
        }

        public int getId() {
            return id;
        }

    }

    /**
     * assigns table column and fill table with months from DB
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
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
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement st = conn.createStatement();

            ObservableList<Month> monthsInDB = FXCollections.observableArrayList();

            ResultSet rs = st.executeQuery("select * from szkolka.miesiac;");
            while(rs.next()) {
                monthsInDB.add(new Month(rs.getInt("id_m"), rs.getString("nazwa")));
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
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement st = conn.createStatement();

            st.execute("insert into szkolka.miesiac(nazwa) values('" + monthName.getText() + "');");
            st.close();
            monthName.setText("");
            setMonthsTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * being called after any action on table
     */
    @FXML
    private void rowSelected() {
        if (monthsTable.getSelectionModel().getSelectedItem() != null) {
            removeButton.setDisable(false);
        }
    }

    /**
     * being called after on click removeButton
     * delete selected record from DB
     */
    @FXML
    private void setRemoveButton() {
        Month selectedRow = monthsTable.getSelectionModel().getSelectedItem();
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                st.execute("delete from szkolka.miesiac where id_m=" + selectedRow.getId() + ";");
                monthsTable.getItems().remove(monthsTable.getSelectionModel().getSelectedItem());
                removeButton.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
