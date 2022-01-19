package ir.kimia.client.controller;


import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.common.BasicProductCode;
import ir.kimia.client.common.Constants;
import ir.kimia.client.data.model.*;
import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.api.AccountBalanceService;
import ir.kimia.client.service.api.ChequeService;
import ir.kimia.client.service.api.StockService;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.EditCell;
import ir.kimia.client.util.FxUtil;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@FxAppScoped
public class OverallStockManagementController extends BaseController {

    private static final Logger log = LogManager.getLogger(OverallStockManagementController.class);

    private final StockService stockService;
    private final AccountBalanceService accountBalanceService;
    private final ChequeService chequeService;

    @FXML
    private TableView<Pair> cashBasedStockTable;
    @FXML
    private TableView<Pair> goldBasedStockTable;
    @FXML
    private TableView<Product> productFeeTable;
    @FXML
    private TextField stockBasedOnDollarInput;
    @FXML
    private TextField stockBasedOnGoldInput;
    @FXML
    private TextField stockBasedOnRialInput;
    @FXML
    private TextField stockBasedOnEuroInput;
    @FXML
    private TextField stockBasedOnEmamiCoinInput;
    @FXML
    private TextField stockBasedOnDirhamInput;

    private DecimalFormat decimalFormat;

    private Callback<TableColumn.CellDataFeatures<Pair, String>, ObservableValue<String>> getPairValueFactory(String cellName) {
        return param -> {
            Pair value = param.getValue();
            String tableId = param.getTableView().getId();
            return new ObjectBinding<>() {
                @Override
                protected String computeValue() {
                    try {
                        switch (cellName) {
                            case "title" -> {
                                if (value != null) {
                                    return message((String) value.getLeft());
                                } else {
                                    return "";
                                }
                            }
                            case "value" -> {
                                if (value != null) {
                                    String key = (String) value.getLeft();
                                    List<Stock> stocks = new ArrayList<>();
                                    List<AccountBalance> accountBalanceList = new ArrayList<>();
                                    boolean isCountable = true;
                                    if (tableId.equals("cashBasedStockTable")) {
                                        switch (key) {
                                            case "cashDebtors" -> {
                                                accountBalanceList = accountBalanceService.getCashDebtorsStocks();
                                            }
                                            case "cashCreditors" -> {
                                                accountBalanceList = accountBalanceService.getCashCreditorsStocks();
                                            }
                                            case "cash" -> {
                                                stocks = stockService.getCashStocks();
                                            }
                                            case "inventoryCheques", "officeCheques" -> {
                                                List<Cheque> cheques = null;
                                                if (key.equals("inventoryCheques")) {
                                                    cheques = chequeService.getInOfficeInventoryChequeStocks();
                                                }
                                                if (key.equals("officeCheques")) {
                                                    cheques = chequeService.getHoldByPartyChequeStocks();
                                                }

                                                Double result = 0.0;
                                                if (cheques != null) {
                                                    for (Cheque cheque : cheques) {
                                                        Double amount = cheque.getAmount();
                                                        if (amount != null) {
                                                            result += amount;
                                                        }
                                                    }
                                                }
                                                value.setValue(result);
                                                return decimalFormat.format(result);
                                            }
                                            case "manufacturedWages" -> {
                                                stocks = stockService.getOfficeManufacturedStocks();
                                                Double result = 0.0;
                                                if (stocks != null) {
                                                    for (Stock stock : stocks) {
                                                        Double stockCarat = stock.getCarat();
                                                        Double stockWeight = stock.getWeight();
                                                        if (stockCarat == null) {
                                                            stockCarat = Constants.GOLD_QUOTE;
                                                        }

                                                        if (stockWeight != null) {
                                                            result += stockWeight * stockCarat / Constants.GOLD_QUOTE;
                                                        }
                                                    }
                                                }
                                                value.setValue(result);
                                                return decimalFormat.format(result);
                                            }
                                            case "coinsWorth" -> {
                                                stocks = stockService.getOfficeCoinStocks();
                                            }
                                            case "currencyWorth" -> {
                                                stocks = stockService.getOfficeCurrencyStocks();
                                            }
                                            case "coinDebtors" -> {
                                                accountBalanceList = accountBalanceService.getOfficeCoinDebtorsStocks();
                                            }
                                            case "coinCreditors" -> {
                                                accountBalanceList = accountBalanceService.getOfficeCoinCreditorsStocks();
                                            }
                                            case "currencyDebtors" -> {
                                                accountBalanceList = accountBalanceService.getOfficeCurrencyDebtorsStocks();
                                            }
                                            case "currencyCreditors" -> {
                                                accountBalanceList = accountBalanceService.getOfficeCurrencyCreditorsStocks();
                                            }
                                            case "stonesWorth" -> {
                                                stocks = stockService.getOfficeStoneStocks();
                                            }
                                            case "totalCashWorth" -> {
                                                final double calculateTotalCashWorth = calculateTotalCashWorth();
                                                value.setValue(calculateTotalCashWorth);
                                                return decimalFormat.format(calculateTotalCashWorth);
                                            }
                                        }
                                    }

                                    if (tableId.equals("goldBasedStockTable")) {
                                        switch (key) {
                                            case "goldDebtors" -> {
                                                final Double goldDebtorsValue = accountBalanceService.getGoldDebtorsValue();
                                                value.setValue(goldDebtorsValue);
                                                return decimalFormat.format(goldDebtorsValue);
                                            }
                                            case "goldCreditors" -> {
                                                final Double goldCreditorsValue = accountBalanceService.getGoldCreditorsValue();
                                                value.setValue(goldCreditorsValue);
                                                return decimalFormat.format(goldCreditorsValue);
                                            }
                                            case "meltedWeightByScale" -> {
                                                final Double meltedWeightByScaleValue = stockService.getMeltedWeightByScaleValue();
                                                value.setValue(meltedWeightByScaleValue);
                                                return decimalFormat.format(meltedWeightByScaleValue);
                                            }
                                            case "meltedWeightBasedOnCarat" -> {
                                                final Double meltedWeightByCaratValue = stockService.getMeltedWeightByCaratValue();
                                                value.setValue(meltedWeightByCaratValue);
                                                return decimalFormat.format(meltedWeightByCaratValue);
                                            }
                                            case "mscWeightByScale" -> {
                                                final Double mscWeightByScaleValue = stockService.getMscWeightByScaleValue();
                                                value.setValue(mscWeightByScaleValue);
                                                return decimalFormat.format(mscWeightByScaleValue);
                                            }
                                            case "mscWeightBasedOnCarat" -> {
                                                final Double mscWeightByCaratValue = stockService.getMscWeightByCaratValue();
                                                value.setValue(mscWeightByCaratValue);
                                                return decimalFormat.format(mscWeightByCaratValue);
                                            }
                                            case "manufacturedInventory" -> {
                                                final Double manufacturedWeightsValue = stockService.getManufacturedWeightsValue();
                                                value.setValue(manufacturedWeightsValue);
                                                return decimalFormat.format(manufacturedWeightsValue);
                                            }
                                            case "wagedInventoryPercentage" -> {
                                                final Double manufacturedWagesPercentageValue = stockService.getManufacturedWagesPercentageValue();
                                                value.setValue(manufacturedWagesPercentageValue);
                                                return decimalFormat.format(manufacturedWagesPercentageValue);
                                            }
                                            case "totalGoldWorth" -> {
                                                return decimalFormat.format(calculateTotalGoldWorth());
                                            }
                                        }
                                    }


                                    Double result = 0.0;
                                    if (stocks != null && stocks.size() > 0) {
                                        for (Stock stock : stocks) {
                                            Double count = stock.getCount();
                                            Double weight = stock.getWeight();
                                            final Product stockProduct = stock.getProduct();
                                            final String categoryCode = stockProduct.getProductCategory().getCode();
                                            isCountable = stockProduct.getProductCategory().getCountable();
                                            Double wageFee = stockProduct.getPayedWageAmount();
                                            if (isCountable && count != null) {
                                                if (categoryCode.equals(BasicProductCategoryCode.CASH.value())) {
                                                    result += count;
                                                } else {
                                                    if (wageFee == null) {
                                                        wageFee = 0.0;
                                                    }
                                                    result += count * wageFee;
                                                }
                                            } else if (weight != null) {
                                                result += weight;
                                            }
                                        }
                                    }

                                    if (accountBalanceList != null && accountBalanceList.size() > 0) {
                                        for (AccountBalance accountBalance : accountBalanceList) {
                                            Double count = accountBalance.getCount();
                                            Double amount = accountBalance.getAmount();
                                            final Product product = accountBalance.getProduct();
                                            final Double payedWageAmount = product.getPayedWageAmount();
                                            final String categoryCode = product.getProductCategory().getCode();
                                            if (categoryCode.equals(BasicProductCategoryCode.COIN.value()) ||
                                                    categoryCode.equals(BasicProductCategoryCode.CURRENCY.value())) {
                                                result += count * payedWageAmount;
                                            } else if (categoryCode.equals(BasicProductCategoryCode.CASH.value())) {
                                                result += count;
                                            } else if (categoryCode.equals(BasicProductCategoryCode.MELTED.value())) {
                                                result += amount * payedWageAmount;
                                            } else {
                                                log.error("something is wrong with accountBalance: " + accountBalance);
                                            }
                                        }
                                    }
                                    value.setValue(result);
                                    return decimalFormat.format(result);

                                }

                                return "0.0";
                            }
                        }
                    } catch (SQLException e) {
                        FxUtil.exceptionOccurred(e);
                    }
                    return "";
                }
            };
        };
    }

    private double calculateTotalCashWorth() {
        double result = 0;

        final ObservableList<Pair> items = cashBasedStockTable.getItems();
        if (items != null) {
            for (Pair item : items) {
                final String key = (String) item.getLeft();
                final Object right = item.getRight();
                if (right != null) {
                    Double value = (Double) right;
                    switch (key) {
                        case "cashDebtors", "cash", "inventoryCheques", "coinsWorth", "currencyWorth", "coinDebtors", "currencyDebtors", "stonesWorth", "cashCreditors", "coinCreditors", "currencyCreditors" -> {
                            result += value;
                        }
                        case "officeCheques", "manufacturedWages" -> {
                            result -= value;
                        }
                    }
                }
            }
        }

        return result;
    }

    private double calculateTotalGoldWorth() {
        double result = 0;

        final ObservableList<Pair> items = goldBasedStockTable.getItems();
        if (items != null) {
            for (Pair item : items) {
                final String key = (String) item.getLeft();
                final Object right = item.getRight();
                if (right != null) {
                    Double value = (Double) right;
                    switch (key) {
                        case "goldDebtors", "meltedWeightBasedOnCarat", "mscWeightBasedOnCarat", "manufacturedInventory", "goldCreditors" -> {
                            result += value;
                        }
                        case "wagedInventoryPercentage" -> {
                            result -= value;
                        }
                    }
                }
            }
        }

        return result;
    }

    private Callback<TableColumn.CellDataFeatures<Product, String>, ObservableValue<String>> getProductFeeValueFactory(String cellName) {
        return param -> {
            Product product = param.getValue();
            String tableId = param.getTableView().getId();

            return new ObjectBinding<>() {
                @Override
                protected String computeValue() {
                    if (product != null) {
                        switch (cellName) {
                            case "title" -> {
                                return product.getProductName();
                            }
                            case "value" -> {
                                if (product.getPayedWageAmount() != null) {
                                    return decimalFormat.format(product.getPayedWageAmount());
                                } else {
                                    return "0.0";
                                }
                            }
                        }
                    }

                    return "";
                }
            };
        };
    }

    @Inject
    public OverallStockManagementController(StockService stockService, ChequeService chequeService, AccountBalanceService accountBalanceService) {
        this.stockService = stockService;
        this.chequeService = chequeService;
        this.accountBalanceService = accountBalanceService;
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException, ApplicationException {
        super.init(url, resourceBundle);

        decimalFormat = new DecimalFormat("0");
        decimalFormat.setMaximumFractionDigits(10);

        setupCashBasedStockTable();
        setupGoldBasedStockTable();
        setupProductFeeTable();
        refreshForm();
    }

    private void setupCashBasedStockTable() {

        var mainTitleColumn = new TableColumn<Pair, String>(message("cashOpeningList"));
        mainTitleColumn.setId("cashOpeningList");
        mainTitleColumn.setCellValueFactory(getPairValueFactory("cashOpeningList"));

        var titleColumn = new TableColumn<Pair, String>(message("title"));
        titleColumn.setId("title");
        titleColumn.setCellValueFactory(getPairValueFactory("title"));

        var valueColumn = new TableColumn<Pair, String>(message("amount"));
        valueColumn.setId("value");
        valueColumn.setPrefWidth(225);
        valueColumn.styleProperty().setValue("-fx-alignment: CENTER;");
        valueColumn.setCellValueFactory(getPairValueFactory("value"));

        mainTitleColumn.getColumns().addAll(titleColumn, valueColumn);

        cashBasedStockTable.getColumns().addAll(mainTitleColumn);


        Pair<String, Double> cashDebtors = new MutablePair<>("cashDebtors", 0.0);
        Pair<String, Double> cashCreditors = new MutablePair<>("cashCreditors", 0.0);
        Pair<String, Double> cash = new MutablePair<>("cash", 0.0);
        Pair<String, Double> inventoryCheques = new MutablePair<>("inventoryCheques", 0.0);
        Pair<String, Double> officeCheques = new MutablePair<>("officeCheques", 0.0);
        Pair<String, Double> manufacturedWages = new MutablePair<>("manufacturedWages", 0.0);
        Pair<String, Double> coinsWorth = new MutablePair<>("coinsWorth", 0.0);
        Pair<String, Double> currencyWorth = new MutablePair<>("currencyWorth", 0.0);
        Pair<String, Double> coinDebtors = new MutablePair<>("coinDebtors", 0.0);
        Pair<String, Double> coinCreditors = new MutablePair<>("coinCreditors", 0.0);
        Pair<String, Double> currencyDebtors = new MutablePair<>("currencyDebtors", 0.0);
        Pair<String, Double> currencyCreditors = new MutablePair<>("currencyCreditors", 0.0);
        Pair<String, Double> stonesWorth = new MutablePair<>("stonesWorth", 0.0);
        Pair<String, Double> dummy1 = new MutablePair<>("", null);
        Pair<String, Double> dummy2 = new MutablePair<>("", null);
        Pair<String, Double> dummy3 = new MutablePair<>("", null);
        Pair<String, Double> totalCashWorth = new MutablePair<>("totalCashWorth", 0.0);

        cashBasedStockTable.getItems().addAll(cashDebtors, cashCreditors, cash, inventoryCheques, officeCheques, manufacturedWages,
                coinsWorth, currencyWorth, coinDebtors, coinCreditors, currencyDebtors, currencyCreditors, stonesWorth, dummy1, dummy2, dummy3, totalCashWorth);
        FxUtil.autoResizeColumns(cashBasedStockTable);
    }

    private void setupGoldBasedStockTable() {
        var mainTitleColumn = new TableColumn<Pair, String>(message("goldOpeningList"));
        mainTitleColumn.setId("goldOpeningList");
        mainTitleColumn.setCellValueFactory(getPairValueFactory("goldOpeningList"));

        var titleColumn = new TableColumn<Pair, String>(message("title"));
        titleColumn.setId("title");
        titleColumn.setCellValueFactory(getPairValueFactory("title"));

        var valueColumn = new TableColumn<Pair, String>(message("amount"));
        valueColumn.setId("value");
        valueColumn.styleProperty().setValue("-fx-alignment: CENTER;");
        valueColumn.setPrefWidth(210);
        valueColumn.setCellValueFactory(getPairValueFactory("value"));

        mainTitleColumn.getColumns().addAll(titleColumn, valueColumn);

        goldBasedStockTable.getColumns().addAll(mainTitleColumn);

        Pair<String, Double> goldDebtors = new MutablePair<>("goldDebtors", 0.0);
        Pair<String, Double> goldCreditors = new MutablePair<>("goldCreditors", 0.0);
        Pair<String, Double> meltedWeightByScale = new MutablePair<>("meltedWeightByScale", 0.0);
        Pair<String, Double> meltedWeightBasedOnCarat = new MutablePair<>("meltedWeightBasedOnCarat", 0.0);
        Pair<String, Double> mscWeightByScale = new MutablePair<>("mscWeightByScale", 0.0);
        Pair<String, Double> mscWeightBasedOnCarat = new MutablePair<>("mscWeightBasedOnCarat", 0.0);
        Pair<String, Double> manufacturedInventory = new MutablePair<>("manufacturedInventory", 0.0);
        Pair<String, Double> wagedInventoryPercentage = new MutablePair<>("wagedInventoryPercentage", 0.0);
        Pair<String, Double> dummy1 = new MutablePair<>("", null);
        Pair<String, Double> dummy2 = new MutablePair<>("", null);
        Pair<String, Double> dummy3 = new MutablePair<>("", null);
        Pair<String, Double> dummy4 = new MutablePair<>("", null);
        Pair<String, Double> dummy5 = new MutablePair<>("", null);
        Pair<String, Double> dummy6 = new MutablePair<>("", null);
        Pair<String, Double> dummy7 = new MutablePair<>("", null);
        Pair<String, Double> dummy8 = new MutablePair<>("", null);
        Pair<String, Double> totalGoldWorth = new MutablePair<>("totalGoldWorth", 0.0);

        goldBasedStockTable.getItems().addAll(goldDebtors, goldCreditors, meltedWeightByScale, meltedWeightBasedOnCarat, mscWeightByScale,
                mscWeightBasedOnCarat, manufacturedInventory, wagedInventoryPercentage, dummy1, dummy2, dummy3, dummy4, dummy5, dummy6, dummy7, dummy8, totalGoldWorth);
        FxUtil.autoResizeColumns(goldBasedStockTable);
    }

    private void setupProductFeeTable() {

        try {

            var mainTitleColumn = new TableColumn<Product, String>(message("feeList"));
            mainTitleColumn.setId("feeList");
            mainTitleColumn.setCellValueFactory(getProductFeeValueFactory("feeList"));

            var titleColumn = new TableColumn<Product, String>(message("title"));
            titleColumn.setId("title");
            titleColumn.setPrefWidth(110);
            titleColumn.setCellValueFactory(getProductFeeValueFactory("title"));

            var valueColumn = new TableColumn<Product, String>(message("fee"));
            valueColumn.setId("fee");
            valueColumn.setPrefWidth(162);
            valueColumn.styleProperty().setValue("-fx-alignment: CENTER;");
            valueColumn.setCellValueFactory(getProductFeeValueFactory("value"));
            valueColumn.setEditable(true);
            valueColumn.setCellFactory(EditCell.forTableColumn(new StringConverter<>() {

                @Override
                public String toString(String object) {
                    return object != null ? object : "";
                }

                @Override
                public String fromString(String string) {
                    if (StringUtils.isNotEmpty(string)) {
                        return string;
                    } else {
                        return "";
                    }
                }
            }));
            valueColumn.setOnEditCommit(event -> {
                String newValue = event.getNewValue();
                String oldValue = null;

                if (event.getOldValue() != null) {
                    oldValue = event.getOldValue();
                }
                if (StringUtils.isEmpty(newValue) && StringUtils.isEmpty(oldValue)) {
                    event.consume();
                    return;
                }
                if (newValue != null && newValue.equals(oldValue)) {
                    event.consume();
                    return;
                }
                try {
                    Product product = event.getTableView().getItems().get(event.getTablePosition().getRow());
                    final ProductCategory productCategory = product.getProductCategory();
                    if (productCategory.getCode().equals(BasicProductCategoryCode.COIN.value()) ||
                            productCategory.getCode().equals(BasicProductCategoryCode.CURRENCY.value()) ||
                            productCategory.getCode().equals(BasicProductCategoryCode.MELTED.value())) {

                        product.setPayedWageAmount(Double.valueOf(newValue));
                        productService.updateProduct(product);
                        refreshForm();
                    }
                } catch (NumberFormatException e) {
                    event.consume();
                } catch (SQLException e) {
                    FxUtil.exceptionOccurred(e);
                }
            });

            mainTitleColumn.getColumns().addAll(titleColumn, valueColumn);
            productFeeTable.getColumns().addAll(mainTitleColumn);
            productFeeTable.setEditable(true);

            final List<Product> productByCategoryCodes = productService.getProductByCategoryCodes(BasicProductCategoryCode.COIN.value(),
                    BasicProductCategoryCode.CURRENCY.value(), BasicProductCategoryCode.MELTED.value());

            productFeeTable.getItems().addAll(productByCategoryCodes);

        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }

    }

    private Double getTotalCashOrGoldWorth(boolean cash) {
        ObservableList<Pair> items;
        if (cash) {
            items = cashBasedStockTable.getItems();
        } else {
            items = goldBasedStockTable.getItems();
        }
        if (items != null) {
            for (Pair item : items) {
                final String key = (String) item.getLeft();
                if (StringUtils.isNotEmpty(key) && key.equals("totalCashWorth")) {
                    final Object right = item.getRight();
                    if (right != null) {
                        return (Double) right;
                    }
                }
            }
        }
        return 0.0;
    }

    @Override
    public void postLoad() {
        try {
            refreshForm();
        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    private Double getTotalGoldWorth() {
        return getTotalCashOrGoldWorth(true);
    }

    private Double getTotalCashWorth() {
        return getTotalCashOrGoldWorth(false);
    }

    private void computeAllStock() throws SQLException {
        final Double totalGoldWorth = getTotalGoldWorth();
        final Double totalCashWorth = getTotalCashWorth();
        final Product goldProduct = productService.getProductByCode(BasicProductCode.MELTED_GOLD.value());
        final Double goldFee = goldProduct.getPayedWageAmount();
        final Product dollar = productService.getProductByName(message("dollar"));
        Double dollarFee = 0.0;
        if (dollar != null && dollar.getPayedWageAmount() != null) {
            dollarFee = dollar.getPayedWageAmount();
        }
        final Product euro = productService.getProductByName(message("euro"));
        Double euroFee = 0.0;
        if (euro != null && euro.getPayedWageAmount() != null) {
            euroFee = euro.getPayedWageAmount();
        }
        final Product dirham = productService.getProductByName(message("dirham"));
        Double dirhamFee = 0.0;
        if (dirham != null && dirham.getPayedWageAmount() != null) {
            dirhamFee = dirham.getPayedWageAmount();
        }
        final Product emamiCoin = productService.getProductByName(message("emami.coin"));
        Double emamiCoinFee = 0.0;
        if (emamiCoin != null && emamiCoin.getPayedWageAmount() != null) {
            emamiCoinFee = emamiCoin.getPayedWageAmount();
        }

        Double totalStockBasedOnGold = 0.0;
        if (goldFee != null && goldFee != 0) {
            totalStockBasedOnGold = totalCashWorth / (goldFee / Constants.ONE_MITHQAL * Constants.BASE_CARAT / 705) + totalGoldWorth;
            stockBasedOnGoldInput.setText(decimalFormat.format(totalStockBasedOnGold));
        }

        Double totalStockBasedOnCash = (goldFee / Constants.ONE_MITHQAL * Constants.BASE_CARAT / 705 * totalGoldWorth) + totalCashWorth;
        stockBasedOnRialInput.setText(decimalFormat.format(totalStockBasedOnCash));

        if (dollarFee != 0) {
            stockBasedOnDollarInput.setText(decimalFormat.format(totalStockBasedOnCash / dollarFee));
        }

        if (euroFee != 0) {
            stockBasedOnEuroInput.setText(decimalFormat.format(totalStockBasedOnCash / euroFee));
        }

        if (dirhamFee != 0) {
            stockBasedOnDirhamInput.setText(decimalFormat.format(totalStockBasedOnCash / dirhamFee));
        }

        if (emamiCoinFee != 0) {
            stockBasedOnEmamiCoinInput.setText(decimalFormat.format(totalStockBasedOnCash / emamiCoinFee));
        }

    }

    private void refreshForm() throws SQLException {
        goldBasedStockTable.refresh();
        cashBasedStockTable.refresh();
        computeAllStock();
    }

}
