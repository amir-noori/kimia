package ir.kimia.client.controller;

import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.di.FxAppScoped;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@FxAppScoped
public class StockController extends BaseController {

    @FXML
    private TabPane stockTabPane;

    @FXML
    private AccountBalanceManagementController stockViewController;

    @FXML
    private ChequeStockManagementController chequeStockViewController;

    @FXML
    private ProductStockManagementController productStockViewController;

    @FXML
    private OverallStockManagementController overallStockViewController;

    @Inject
    public StockController() {
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException, ApplicationException {
        stockTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

        });
    }

    @Override
    public List<BaseController> getIncludedControllers() {
        return new ArrayList<>() {{
            add(stockViewController);
            add(chequeStockViewController);
            add(productStockViewController);
            add(overallStockViewController);
        }};
    }
}
