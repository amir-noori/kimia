package ir.kimia.client.controller;


import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.common.BasicProductCode;
import ir.kimia.client.common.Constants;
import ir.kimia.client.data.model.*;
import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.api.ProductService;
import ir.kimia.client.service.api.StockService;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.ActionButtonTableCell;
import ir.kimia.client.ui.EditCell;
import ir.kimia.client.ui.StockFilterChangeListener;
import ir.kimia.client.util.FxUtil;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Function;

@FxAppScoped
public class ProductStockManagementController extends BaseController {

    private static final Logger log = LogManager.getLogger(ProductStockManagementController.class);

    @FXML
    private TextField searchTextField;

    @FXML
    private TableView<Stock> productStockTable;

    private FilteredList<Stock> allProductStocksFilterList;
    private List<Stock> allProductStocks;

    private final StockService stockService;
    private final ProductService productService;

    private Callback<TableColumn.CellDataFeatures<Stock, String>, ObservableValue<String>> stockCellValueFactory(String cellName) {
        return new Callback<TableColumn.CellDataFeatures<Stock, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Stock, String> param) {
                return new ObjectBinding<>() {
                    @Override
                    protected String computeValue() {
                        Stock stock = param.getValue();
                        String result = "";
                        if (stock != null) {
                            Product product = stock.getProduct();
                            if (product != null) {
                                String productCategoryCode = product.getProductCategory().getCode();
                                boolean isCoinOrCurrency = productCategoryCode.equals(BasicProductCategoryCode.COIN.value()) || productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value());

                                switch (cellName) {
                                    case "productCategory" -> {
                                        result = product.getProductCategory().getCode();
                                    }
                                    case "productCode" -> {
                                        result = product.getCode();
                                    }
                                    case "productName" -> {
                                        result = product.getProductCategory().getTitle() + " - " + product.getProductName();
                                    }
                                    case "productCount" -> {
                                        if (stock.getCount() != null) {
                                            return String.valueOf(stock.getCount());
                                        } else {
                                            return "";
                                        }
                                    }
                                    case "productWeight" -> {
                                        if (!product.getProductCategory().getCountable()) {
                                            return String.valueOf(stock.getWeight());
                                        } else {
                                            return "";
                                        }
                                    }
                                    case "carat" -> {
                                        Double stockCarat = stock.getCarat();
                                        if (stockCarat != null) {
                                            return String.valueOf(stockCarat);
                                        } else {
                                            Double carat = product.getCarat();
                                            if (isCoinOrCurrency || carat == null) {
                                                return "";
                                            } else {
                                                return String.valueOf(carat);
                                            }
                                        }
                                    }
                                    case "withCarat" -> {
                                        // TODO
                                        if (isCoinOrCurrency) {
                                            return "";
                                        }
                                    }
                                    case "percentage" -> {
                                        // TODO
                                        if (isCoinOrCurrency) {
                                            return "";
                                        }
                                    }
                                    case "fee" -> {
                                        Double wageFee = stock.getWageFee();
                                        if (wageFee != null) {
                                            return String.valueOf(wageFee);
                                        }
                                        return "";
                                    }
                                    case "stoneWight" -> {
                                        // TODO
                                        if (isCoinOrCurrency) {
                                            return "";
                                        }
                                    }
                                    case "stonePrice" -> {
                                        // TODO
                                        if (isCoinOrCurrency) {
                                            return "";
                                        }
                                    }
                                    case "moneyAmount" -> {
                                        Double wageFee = stock.getWageFee();
                                        Double carat = stock.getCarat();
                                        if (product.getProductCategory().getCountable()) {
                                            Double count = stock.getCount();
                                            if (count != null && wageFee != null) {
                                                return String.valueOf(count * wageFee);
                                            }
                                        } else {
                                            Double weight = stock.getWeight();
                                            if (carat == null) {
                                                carat = Constants.GOLD_QUOTE;
                                            }
                                            if (weight != null && wageFee != null) {
                                                Double weightBasedOnCarat = weight * carat / Constants.GOLD_QUOTE;
                                                return String.valueOf(weightBasedOnCarat * wageFee);
                                            }
                                        }
                                        return "";
                                    }
                                }


                            } else {
                                log.error("Stock product ID is null. Stock is: " + stock);
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }

    private EventHandler<TableColumn.CellEditEvent<Stock, String>> onCellCommitHandler(String cellName, TableView targetTable) {
        return new EventHandler<>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                String newValue = (String) event.getNewValue();
                String oldValue = null;

                if (event.getOldValue() != null) {
                    oldValue = (String) event.getOldValue();
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
                    Stock currentStock = (Stock) event.getTableView().getItems().get(event.getTablePosition().getRow());
                    switch (cellName) {
                        case "productCount" -> {
                            Double productCount = 0.0;
                            Double oldProductCount = 0.0;
                            if (StringUtils.isNotEmpty(newValue)) {
                                productCount = Double.parseDouble(newValue);
                            }
                            if (StringUtils.isNotEmpty(oldValue)) {
                                oldProductCount = Double.parseDouble(oldValue);
                            }
                            if (productCount.equals(oldProductCount)) {
                                event.consume();
                                return;
                            }
                            currentStock.setCount(productCount);

                        }
                        case "productWeight" -> {
                            Double productWeight = 0.0;
                            if (StringUtils.isNotEmpty(newValue)) {
                                productWeight = Double.parseDouble(newValue);
                            }
                            currentStock.setWeight(productWeight);
                        }
                        case "carat" -> {
                            if (StringUtils.isNotEmpty(newValue)) {
                                try {
                                    Double carat = Double.parseDouble(newValue);
                                    currentStock.setCarat(carat);
                                } catch (NumberFormatException e) {
                                    // TODO
                                }
                            }
                        }
                        case "withCarat" -> {
                            // TODO
                        }
                        case "percentage" -> {
                            // TODO
                        }
                        case "fee" -> {
                            if (StringUtils.isNotEmpty(newValue)) {
                                try {
                                    Double fee = Double.parseDouble(newValue);
                                    currentStock.setWageFee(fee);
                                } catch (NumberFormatException e) {
                                    // TODO
                                }
                            }
                        }
                        case "stoneWight" -> {
                            // TODO
                        }
                        case "stonePrice" -> {
                            // TODO
                        }
                        case "moneyAmount" -> {
                            // TODO
                        }
                    }
                    try {
                        handleStockAddOrUpdate(currentStock);

                    } catch (Exception exception) {
                        FxUtil.exceptionOccurred(exception);
                    }
                    productStockTable.refresh();

                } catch (NumberFormatException e) {
                    event.consume();
                }
            }
        };
    }

    private Callback<TableColumn<Stock, String>, TableCell<Stock, String>> productStockCellFactory(String cellName) {
        return EditCell.<Stock, String>forTableColumn(new StringConverter<String>() {

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
        }, afterCellCommit(), beforeCellCommit(), getEditableExtractor(cellName));
    }

    private Function<Stock, ObservableValue<Boolean>> getEditableExtractor(String cellName) {
        return stock -> new ObjectBinding<>() {
            @Override
            protected Boolean computeValue() {
                Product product = null;
                ProductCategory productCategory = null;
                if (stock != null) {
                    product = stock.getProduct();
                    if (product != null) {
                        productCategory = product.getProductCategory();
                    }
                }

                switch (cellName) {
                    case "productCount" -> {
                        if (productCategory != null) {
                            String categoryCode = productCategory.getCode();
                            if (categoryCode != null && categoryCode.equals(BasicProductCategoryCode.MANUFACTURED.value())) {
                                // for manufactured products both count and weight must be editable
                                return true;
                            }
                            return productCategory.getCountable();
                        } else {
                            return false;
                        }
                    }
                    case "productWeight" -> {
                        if (productCategory != null) {
                            return !productCategory.getCountable();
                        } else {
                            return false;
                        }
                    }
                    case "carat" -> {
                        if (productCategory != null) {
                            return !productCategory.getCountable();
                        } else {
                            return false;
                        }
                    }
                    case "withCarat" -> {
                        if (productCategory != null) {
                            return !productCategory.getCountable();
                        } else {
                            return false;
                        }
                    }
                    case "percentage" -> {
                        // TODO
                    }
                    case "fee" -> {
                        // TODO
                    }
                    case "stoneWeight" -> {
                        if (productCategory != null) {
                            String productCategoryCode = productCategory.getCode();
                            return productCategoryCode.equals(BasicProductCategoryCode.MANUFACTURED.value());
                        } else {
                            return false;
                        }
                    }
                    case "stonePrice" -> {
                        if (productCategory != null) {
                            String productCategoryCode = productCategory.getCode();
                            return productCategoryCode.equals(BasicProductCategoryCode.MANUFACTURED.value());
                        } else {
                            return false;
                        }
                    }
                    case "moneyAmount" -> {
                        // TODO
                    }
                }
                return true;
            }
        };
    }

    private void focusOn(int row, TableColumn<Stock, ?> column) {
        productStockTable.getFocusModel().focus(row, column);
        productStockTable.edit(row, column);
    }

    private BiFunction<Object, EditCell, Void> afterCellCommit() {
        return (object, actualCell) -> { // on edit commit
            TablePosition<Stock, ?> focusedCell = productStockTable.getFocusModel().getFocusedCell();
            Stock stock = productStockTable.getFocusModel().getFocusedItem();
            String columnId = focusedCell.getTableColumn().getId();
            int focusedCellRow = focusedCell.getRow();
            int focusedCellColumn = focusedCell.getColumn();
            ObservableList<TableColumn<Stock, ?>> columns = productStockTable.getColumns();
            // go to next editable column
            TableColumn<Stock, ?> nextColumn = null;
            if (focusedCellColumn + 1 != columns.size()) {
                nextColumn = columns.get(focusedCellColumn + 1);
            }

            switch (columnId) {
                case "productCount" -> {
                    if (nextColumn.isEditable()) {
                        focusOn(focusedCellRow, nextColumn);
                    }
                }
                case "productWeight" -> {
                    if (nextColumn.isEditable()) {
                        focusOn(focusedCellRow, nextColumn);
                    }
                }
                case "carat" -> {
                    // TODO
                    nextColumn = columns.get(focusedCellColumn + 2);
                    if (nextColumn.isEditable()) {
                        focusOn(focusedCellRow, nextColumn);
                    }
                }
                case "withCarat" -> {
                    // TODO
                }
                case "percentage" -> {
                    // TODO
                    nextColumn = columns.get(focusedCellColumn + 4);
                    if (nextColumn.isEditable()) {
                        productStockTable.getFocusModel().focus(focusedCellRow, nextColumn);
                        productStockTable.edit(focusedCellRow, nextColumn);
                    }
                }
                case "fee" -> {
                    // TODO
                }
                case "stoneWight" -> {
                    // TODO
                }
                case "stonePrice" -> {
                    // TODO
                }
                case "moneyAmount" -> {
                    // TODO
                    nextColumn = columns.get(3);
                    productStockTable.getFocusModel().focus(focusedCellRow + 1, nextColumn);
                    productStockTable.edit(focusedCellRow + 1, nextColumn);
                }
            }

            return null;
        };
    }

    private BiFunction<Object, EditCell, Void> beforeCellCommit() {
        return (object, actualCell) -> { // on start edit

            TablePosition<Stock, ?> focusedCell = productStockTable.getFocusModel().getFocusedCell();
            Stock stock = productStockTable.getFocusModel().getFocusedItem();

            String columnId = focusedCell.getTableColumn().getId();

            EventHandler<KeyEvent> filterOnDialogKeyRelease = new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        event.consume();
                    }
                }
            };

            stage.addEventFilter(KeyEvent.KEY_RELEASED, filterOnDialogKeyRelease);

            try {
                switch (columnId) {
                    case "productCount" -> {
                        // TODO
                    }
                    case "productWeight" -> {
                        // TODO
                    }
                    case "carat" -> {
                        Fineness initialFineness = stock.getFineness();
                        Fineness fineness = showFinenessDialog(initialFineness);
                        stock.setFineness(fineness);
                        stockService.updateOrCreateStock(stock);
                    }
                    case "withCarat" -> {
                        // TODO
                    }
                    case "percentage" -> {
                        // TODO
                    }
                    case "fee" -> {
                        // TODO
                    }
                    case "moneyAmount" -> {
                        // TODO
                    }
                    case "stoneWeight" -> {
                        ArrayList<Stone> initialStones = stock.getStones() == null ? null : new ArrayList<>(stock.getStones());
                        List<Stone> stones = showStoneDialog(initialStones);
                        stock.setStones(stones);
                        stockService.updateOrCreateStock(stock);
                    }
                    case "stonePrice" -> {

                    }
                }
                // @TODO: remove the on escape filter
                // stage.removeEventFilter(KeyEvent.KEY_RELEASED, filterOnDialogKeyRelease);
            } catch (SQLException e) {
                FxUtil.exceptionOccurred(e);
            }
            return null;
        };
    }

    @Inject
    public ProductStockManagementController(StockService stockService, ProductService productService) {
        this.stockService = stockService;
        this.productService = productService;
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException, ApplicationException {

        // initialize current stocks for office warehouse with all products with amount zero.
        stockService.initOfficeProductStocksWithZeroValues();
        allProductStocks = stockService.getOfficeCurrentProductStocks();
        allProductStocksFilterList = new FilteredList<>(FXCollections.observableArrayList(allProductStocks));
        defineProductTableStockInitialData();

        searchTextField.textProperty().addListener(new StockFilterChangeListener<>(allProductStocksFilterList, productStockTable) {
            @Override
            protected Stock getStock(Stock stock) {
                return stock;
            }
        });
    }

    @Override
    public void postLoad() {
        super.postLoad();
        setupSceneShortcuts();
    }

    private void defineProductTableStockInitialData() {

        var productCategoryColumn = new TableColumn<Stock, String>(message("product.category"));
        productCategoryColumn.setId("productCategory");
        productCategoryColumn.setCellValueFactory(stockCellValueFactory("productCategory"));

        var productCodeColumn = new TableColumn<Stock, String>(message("product.code"));
        productCodeColumn.setId("productCode");
        productCodeColumn.setCellValueFactory(stockCellValueFactory("productCode"));

        var productTitleColumn = new TableColumn<Stock, String>(message("product.name"));
        productTitleColumn.setId("productTitle");
        productTitleColumn.setCellValueFactory(stockCellValueFactory("productName"));

        var productCountColumn = new TableColumn<Stock, String>(message("amount"));
        productCountColumn.setId("productCount");
        productCountColumn.setCellValueFactory(stockCellValueFactory("productCount"));
        productCountColumn.setCellFactory(productStockCellFactory("productCount"));
        productCountColumn.setEditable(true);
        productCountColumn.setOnEditCommit(onCellCommitHandler("productCount", productStockTable));
        productCountColumn.setPrefWidth(100);


        var productWeightColumn = new TableColumn<Stock, String>(message("weight.by.scale"));
        productWeightColumn.setId("productWeight");
        productWeightColumn.setCellValueFactory(stockCellValueFactory("productWeight"));
        productWeightColumn.setCellFactory(productStockCellFactory("productWeight"));
        productWeightColumn.setEditable(true);
        productWeightColumn.setOnEditCommit(onCellCommitHandler("productWeight", productStockTable));
        productWeightColumn.setPrefWidth(100);


        var caratColumn = new TableColumn<Stock, String>(message("gold.carat"));
        caratColumn.setId("carat");
        caratColumn.setCellValueFactory(stockCellValueFactory("carat"));
        caratColumn.setCellFactory(productStockCellFactory("carat"));
        caratColumn.setEditable(true);
        caratColumn.setOnEditCommit(onCellCommitHandler("carat", productStockTable));
        caratColumn.setPrefWidth(100);


        var withCaratColumn = new TableColumn<Stock, String>(message("with.carat"));
        withCaratColumn.setId("withCarat");
        withCaratColumn.setCellValueFactory(stockCellValueFactory("withCarat"));
        withCaratColumn.setCellFactory(productStockCellFactory("withCarat"));
        withCaratColumn.setEditable(true);
        withCaratColumn.setOnEditCommit(onCellCommitHandler("withCarat", productStockTable));
        withCaratColumn.setPrefWidth(100);


        var percentageColumn = new TableColumn<Stock, String>(message("percentage"));
        percentageColumn.setId("percentage");
        percentageColumn.setCellValueFactory(stockCellValueFactory("percentage"));
        percentageColumn.setCellFactory(productStockCellFactory("percentage"));
        percentageColumn.setEditable(true);
        percentageColumn.setOnEditCommit(onCellCommitHandler("percentage", productStockTable));
        percentageColumn.setPrefWidth(100);

        var feeColumn = new TableColumn<Stock, String>(message("fee"));
        feeColumn.setId("fee");
        feeColumn.setCellValueFactory(stockCellValueFactory("fee"));
        feeColumn.setCellFactory(productStockCellFactory("fee"));
        feeColumn.setEditable(true);
        feeColumn.setOnEditCommit(onCellCommitHandler("fee", productStockTable));
        feeColumn.setPrefWidth(100);

        var stoneWightColumn = new TableColumn<Stock, String>(message("stone.weight"));
        stoneWightColumn.setId("stoneWeight");
        stoneWightColumn.setCellValueFactory(stockCellValueFactory("stoneWeight"));
        stoneWightColumn.setCellFactory(productStockCellFactory("stoneWeight"));
        stoneWightColumn.setEditable(true);
        stoneWightColumn.setOnEditCommit(onCellCommitHandler("stoneWeight", productStockTable));
        stoneWightColumn.setPrefWidth(100);

        var stonePriceColumn = new TableColumn<Stock, String>(message("stone.price"));
        stonePriceColumn.setId("stonePrice");
        stonePriceColumn.setCellValueFactory(stockCellValueFactory("stonePrice"));
        stonePriceColumn.setCellFactory(productStockCellFactory("stonePrice"));
        stonePriceColumn.setEditable(true);
        stonePriceColumn.setOnEditCommit(onCellCommitHandler("stonePrice", productStockTable));
        stonePriceColumn.setPrefWidth(100);

        var moneyAmountColumn = new TableColumn<Stock, String>(message("money.amount"));
        moneyAmountColumn.setId("moneyAmount");
        moneyAmountColumn.setCellValueFactory(stockCellValueFactory("moneyAmount"));
        moneyAmountColumn.setCellFactory(productStockCellFactory("moneyAmount"));
        moneyAmountColumn.setEditable(true);
        moneyAmountColumn.setOnEditCommit(onCellCommitHandler("moneyAmount", productStockTable));
        moneyAmountColumn.setPrefWidth(100);

        TableColumn<Stock, Button> deleteColumn = new TableColumn<>(message("remove"));
        deleteColumn.setCellFactory(ActionButtonTableCell.<Stock>forTableColumn(message("remove"), (Stock targetStock) -> {
            boolean confirmed = FxUtil.confirm("remove.confirm");
            if (confirmed) {
                removeStock(targetStock);
            }
            return targetStock;
        }));

        productStockTable.getColumns().addAll(productCategoryColumn, productCodeColumn, productTitleColumn, productCountColumn,
                productWeightColumn, caratColumn, withCaratColumn, percentageColumn, feeColumn,
                stoneWightColumn, stonePriceColumn, moneyAmountColumn, deleteColumn);
        productStockTable.getItems().addAll(allProductStocksFilterList);
        productStockTable.setEditable(true);

    }

    private void removeStock(Stock targetStock) {
        try {
            stockService.removeStock(targetStock);
            productStockTable.getItems().remove(targetStock);
        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    private void setupSceneShortcuts() {
        Node productStockVBox = stage.getScene().lookup("#productStockView");
        productStockVBox.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.isControlDown()) {
                Stock focusedItem = productStockTable.getFocusModel().getFocusedItem();
                String productCode = null;
                if (focusedItem != null) {
                    productCode = focusedItem.getProduct().getCode();
                }
                switch (keyEvent.getCode().getChar()) {
                    case "N" -> {
                        // add a new row
                        if (StringUtils.isNotEmpty(productCode)) {
                            if (productCode.equals(BasicProductCode.MSC_GOLD.value())) {
                                onAddMscAction(null);
                            } else if (productCode.equals(BasicProductCode.MELTED_GOLD.value())) {
                                onAddMeltedAction(null);
                            }
                        }
                    }
                    case "D" -> {
                        // delete a row
                        if (StringUtils.isNotEmpty(productCode)) {
                            if (productCode.equals(BasicProductCode.CASH.value())) {
                                FxUtil.error("cannot.remove.this.stock");
                                return;
                            }
                            boolean confirmed = FxUtil.confirm("remove.confirm");
                            if (confirmed) {
                                removeStock(focusedItem);
                            }
                        }
                    }

                }
            }
        });
    }

    private void handleStockAddOrUpdate(Stock stock) throws Exception {
        stockService.updateOrCreateStock(stock);
    }

    public void onAddMscAction(ActionEvent event) {
        createAndSetProduct(BasicProductCode.MSC_GOLD);
    }

    public void onAddMeltedAction(ActionEvent event) {
        createAndSetProduct(BasicProductCode.MELTED_GOLD);
    }

    private void createAndSetProduct(BasicProductCode productCode) {
        try {
            Stock stock = new Stock();
            Product product = productService.getProductByCode(productCode.value());
            stock.setProduct(product);
            stock.setCount(0.0);
            stock.setWeight(0.0);
            stock.setCarat(Constants.BASE_CARAT);
            stock.setParty(null); // null means current office
            stockService.createStock(stock);
            productStockTable.getItems().add(stock);
            productStockTable.layout();
            productStockTable.refresh();

            TableColumn<Stock, ?> columnToEdit = productStockTable.getColumns().get(4); // 4 is for column weight
            productStockTable.requestFocus();
            final int indexOfRow = productStockTable.getItems().indexOf(stock);
            productStockTable.scrollTo(indexOfRow);
            productStockTable.getFocusModel().focus(indexOfRow, columnToEdit);
            productStockTable.edit(indexOfRow, columnToEdit);

        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }
    }
}
