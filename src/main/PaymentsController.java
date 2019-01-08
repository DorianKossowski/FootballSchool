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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

public class PaymentsController  implements Initializable {

    @FXML
    private ComboBox<Integer> yearToAddBox, yearBox;
    @FXML
    private ComboBox<Map.Entry<Integer, String>> playerBox, monthsBox;
    @FXML
    private Text teamName, warningText, reportWarningText;
    @FXML
    private Button addButton, removeButton;
    @FXML
    private VBox wholeArea, addPaymentArea, bottomArea;
    @FXML
    private TableView<Payment> paymentsTable;
    @FXML
    private TableColumn<Payment, String> nameCol, monthCol;

    private User loggedUser;
    private boolean addPaymentFlag1, addPaymentFlag2;
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

        private final int id;

        Payment(String name, String month, int id) {
            this.id = id;
            this.name.set(name);
            this.month.set(month);
        }

        public String getName() {
            return name.get();
        }

        public String getMonth() {
            return month.get();
        }

        public int getId() {
            return id;
        }
    }

    /**
     * @param currentUser currently logged user
     * calls proper methods which prepare the scene
     */
    public void userInit(User currentUser) {
        loggedUser = currentUser;
        initBoxes();

        if (loggedUser.getUserType() == User.Type.COACH) {
            getTeam("select d.id_d, d.nazwa from szkolka.druzyna as d " +
                    "join szkolka.uzytkownik as u using(id_u) where id_u=" + loggedUser.getId() + ";");
            coachTable();
        } else {
            //PARENT
            getTeam("select d.* from szkolka.pilkarz as p join szkolka.uzytkownik as u using(id_u)" +
                    " join szkolka.druzyna as d using(id_d) where u.id_u=" + loggedUser.getId() + ";");
            parentTable();
            wholeArea.getChildren().remove(addPaymentArea); wholeArea.getChildren().remove(bottomArea);
        }
    }

    /**
     * read from DB correct team
     */
    private void getTeam(String getTeamQuery) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery(getTeamQuery)) {
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
     * calls setTable method for user of Coach type
     */
    private void coachTable() {
        setTable("select * from szkolka.wplata as w join szkolka.miesiac as m " +
                "using(id_m) join szkolka.pilkarz as p using(id_p) where w.rok=" + yearBox.getValue() + " and p.id_d=" +
                currentTeamId + " order by id_m;");
    }

    /**
     * calls setTable method for user of Parent type
     */
    private void parentTable() {
        setTable("select * from szkolka.wplata as w join szkolka.miesiac as m " +
                "using(id_m) join szkolka.pilkarz as p using(id_p) where w.rok=" + yearBox.getValue() + " and p.id_u=" +
                loggedUser.getId() + ";");
    }

    /**
     * fills the paymentsTable with values from DB and calls method which set proper table's height
     */
    private void setTable(String getPaymentsQuery) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery(getPaymentsQuery)) {
                    ObservableList<Payment> paymentsInDB = FXCollections.observableArrayList();
                    while(rs.next()) {
                        paymentsInDB.add(new Payment(rs.getString("imie") + " " + rs.getString("nazwisko"),
                                rs.getString("nazwa"), rs.getInt("id_w")));
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
        if(warningText.isVisible())
            warningText.setVisible(false);
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
        if(warningText.isVisible())
            warningText.setVisible(false);
    }

    /**
     * being called after action on yearBox
     */
    @FXML
    private void changeYear() {
        if (loggedUser.getUserType() == User.Type.COACH) {
            coachTable();
        } else {
            parentTable();
        }

        if(!removeButton.isDisable()) {
            removeButton.setDisable(true);
        }
    }

    /**
     * add available values of ComboBox to choose - players, months and years
     */
    private void initBoxes() {
        //printing only values(names)
        StringConverter<Map.Entry<Integer, String>> toComboBox = new StringConverter<>() {
            @Override
            public String toString(Map.Entry<Integer, String> object) {
                return object.getValue();
            }

            @Override
            public Map.Entry<Integer, String> fromString(String string) {
                return null;
            }
        };

        //PLAYERS
        ObservableList<Map.Entry<Integer, String>> availablePlayers = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select p.* from szkolka.pilkarz as p join szkolka.druzyna as d " +
                        "using(id_d) where d.id_u=" + loggedUser.getId() + ";")) {
                    while(rs.next()) {
                        int tempId = rs.getInt("id_p");
                        String tempName = rs.getString("imie") + " " + rs.getString("nazwisko");
                        availablePlayers.add(Map.entry(tempId, tempName));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        playerBox.setItems(availablePlayers);
        playerBox.setConverter(toComboBox);

        //MONTHS
        ObservableList<Map.Entry<Integer, String>> availableMonths = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("select * from szkolka.miesiac;")) {
                    while(rs.next()) {
                        int tempId = rs.getInt("id_m");
                        String tempName = rs.getString("nazwa");
                        availableMonths.add(Map.entry(tempId, tempName));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        monthsBox.setItems(availableMonths);
        monthsBox.setConverter(toComboBox);

        //YEARS
        ObservableList<Integer> availableYears = FXCollections.observableArrayList();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for(int year = 2018; year <= currentYear; ++year){
            availableYears.add(year);
        }
        yearToAddBox.setItems(availableYears); yearToAddBox.setValue(currentYear);
        yearBox.setItems(availableYears); yearBox.setValue(currentYear);
    }

    /**
     * check if new payment already exists, if not add new record to DB
     */
    @FXML
    private void setAddButton() {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                //check if record already exists id DB
                try (ResultSet rs = st.executeQuery("select 1 from szkolka.wplata where id_p=" +
                        playerBox.getValue().getKey() + " and id_m=" + monthsBox.getValue().getKey() +
                        " and rok=" + yearToAddBox.getValue() + ";")) {
                    if(rs.next()) {
                        warningText.setVisible(true);
                        return;
                    }
                }

                //add new record
                reportWarningText.setVisible(false);
                st.execute("insert into szkolka.wplata(id_p, id_m, rok, wplacono) values(" +
                        playerBox.getValue().getKey() + "," + monthsBox.getValue().getKey() + "," +
                        yearToAddBox.getValue() + ", true);");
                playerBox.getSelectionModel().clearSelection();
                monthsBox.getSelectionModel().clearSelection();
                addPaymentFlag1 = addPaymentFlag2 = false;
                addButton.setDisable(true);
                coachTable();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * being called after any action on table
     */
    @FXML
    private void rowSelected() {
        if (paymentsTable.getSelectionModel().getSelectedItem() != null) {
            removeButton.setDisable(false);
        }
    }

    /**
     * being called after on click removeButton
     * delete selected record from DB
     */
    @FXML
    private void setRemoveButton() {
        reportWarningText.setVisible(false);
        Payment selectedRow = paymentsTable.getSelectionModel().getSelectedItem();
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            try (Statement st = conn.createStatement()) {
                st.execute("delete from szkolka.wplata where id_w=" + selectedRow.getId() + ";");
                paymentsTable.getItems().remove(paymentsTable.getSelectionModel().getSelectedItem());
                removeButton.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param file absolute path when excel file will be saved
     * @throws IOException exceptions appear during file saving
     * creates excel file with summary of payments
     */
    private void createXls(String file) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("raport");

        //paid cell
        HSSFCellStyle gStyle = workbook.createCellStyle();
        gStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        gStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        gStyle.setBorderBottom(BorderStyle.THIN);
        gStyle.setBorderTop(BorderStyle.THIN);
        gStyle.setBorderRight(BorderStyle.THIN);
        gStyle.setBorderLeft(BorderStyle.THIN);

        //unpaid cell
        HSSFCellStyle rStyle = workbook.createCellStyle();
        rStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        rStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        rStyle.setBorderBottom(BorderStyle.THIN);
        rStyle.setBorderTop(BorderStyle.THIN);
        rStyle.setBorderRight(BorderStyle.THIN);
        rStyle.setBorderLeft(BorderStyle.THIN);

        int colCounter=0, rowCounter=0;
        Row row = spreadsheet.createRow(rowCounter++);
        spreadsheet.setColumnWidth(colCounter, 5000);

        row.createCell(colCounter++).setCellValue("Piłkarz");
        for (Map.Entry<Integer, String> month : monthsBox.getItems()) {
            row.createCell(colCounter++).setCellValue(month.getValue());
        }
        row.createCell(colCounter).setCellValue("Suma");

        for (Map.Entry<Integer, String> player : playerBox.getItems()) {
            row = spreadsheet.createRow(rowCounter++);
            colCounter = 0;
            row.createCell(colCounter++).setCellValue(player.getValue());
            try {
                Connection conn = DatabaseHandler.getInstance().getConnection();
                try (Statement st = conn.createStatement()) {
                    for (Map.Entry<Integer, String> month : monthsBox.getItems()) {
                        try (ResultSet rs = st.executeQuery("select 1 from szkolka.wplata where id_p=" +
                                player.getKey() + " and id_m=" + month.getKey() + ";")) {
                            if (rs.next()) {
                                row.createCell(colCounter++).setCellStyle(gStyle);
                            } else {
                                row.createCell(colCounter++).setCellStyle(rStyle);
                            }
                        }
                    }
                    try(ResultSet rs = st.executeQuery("select count(*) from szkolka.wplata where id_p=" +
                            player.getKey() + " and rok=" + yearBox.getValue() + ";")) {
                        if (rs.next()) {
                            row.createCell(colCounter).setCellValue(rs.getInt("count") + "/" +
                                    monthsBox.getItems().size());
                        }
                    }
                }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.close();
        reportWarningText.setText("Poprawnie zapisano raport");
        reportWarningText.setVisible(true);
    }

    /**
     * being called after on click reportButton
     * shows save dialog box and calls createXls method
     */
    @FXML
    private void makeReport() {
        reportWarningText.setVisible(false);
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel (*.xls)", "*.xls");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Zapisz raport");

        File file = fileChooser.showSaveDialog(removeButton.getScene().getWindow());
        if (file != null) {
            try {
                createXls(file.getAbsolutePath());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                reportWarningText.setText("Wystąpił problem podczas tworzenia raportu");
                reportWarningText.setVisible(true);
            }
        }
    }
}
