package ir.kimia.client.controller;

import com.j256.ormlite.dao.Dao;
import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.common.BasicProductCode;
import ir.kimia.client.controller.model.AccountBalanceModel;
import ir.kimia.client.data.model.AccountBalance;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.service.api.AccountBalanceService;
import ir.kimia.client.service.api.PartyService;
import ir.kimia.client.service.api.ProductService;
import ir.kimia.client.service.api.StockService;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.ActionButtonTableCell;
import ir.kimia.client.ui.EditCell;
import ir.kimia.client.ui.PartyTypeStringConverter;
import ir.kimia.client.util.FxUtil;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

@FxAppScoped
public class AccountBalanceManagementController extends BaseController {

    private static final Logger log = LogManager.getLogger(AccountBalanceManagementController.class);

    private final StockService stockService;
    private final PartyService partyService;
    private final ProductService productService;
    private final AccountBalanceService accountBalanceService;

    private Map<Party, AccountBalanceModel> baseBalanceMap;

    protected List<Product> coinCategoryProducts;
    protected List<Product> notSelectedCoinCategoryProducts;
    protected List<Product> selectedCoinCategoryProducts;

    protected List<Product> currencyCategoryProducts;
    protected List<Product> notSelectedCurrencyCategoryProducts;
    protected List<Product> selectedCurrencyCategoryProducts;

    @FXML
    private ComboBox<Party.PartyType> partyTypeCombobox;

    @FXML
    private TableView<AccountBalanceModel> basicAccountBalanceTable;

    @FXML
    private TableView<AccountBalance> coinBalanceTable;

    @FXML
    private TableView<AccountBalance> currencyBalanceTable;

    @FXML
    private Button addCoinBtn;

    @FXML
    private Button addCurrencyBtn;

    private StringConverter<Double> getDoubleCellFactoryForBaseAccountBalanceModel() {
        return new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                if(object != null) {
                    DecimalFormat df = new DecimalFormat("0");
                    df.setMaximumFractionDigits(10);
                    return df.format(object);
                } else {
                    return "";
                }
            }

            @Override
            public Double fromString(String string) {
                try {
                    return Double.valueOf(string);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        };
    }

    private StringConverter<Double> getDoubleCellFactoryForAccountBalance() {
        return new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return object != null ? object.toString() : "";
            }

            @Override
            public Double fromString(String string) {
                try {
                    return Double.valueOf(string);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        };
    }

    @Override
    public void postLoad() {
        super.postLoad();
        // select the first item in post load to trigger the onselect action.
        partyTypeCombobox.getSelectionModel().selectFirst();
    }

    @Inject
    public AccountBalanceManagementController(StockService stockService, PartyService partyService, ProductService productService, AccountBalanceService accountBalanceService) {
        this.partyService = partyService;
        this.stockService = stockService;
        this.productService = productService;
        this.accountBalanceService = accountBalanceService;
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {

        Image addImage = new Image(getClass().getResourceAsStream("/images/add.png"));
        ImageView addCoinImageView = new ImageView(addImage);
        addCoinImageView.setFitHeight(25);
        addCoinImageView.setFitWidth(25);
        ImageView addCurrencyImageView = new ImageView(addImage);
        addCurrencyImageView.setFitHeight(25);
        addCurrencyImageView.setFitWidth(25);
        addCoinBtn.setGraphic(addCoinImageView);
        addCurrencyBtn.setGraphic(addCurrencyImageView);

        baseBalanceMap = new HashMap<>();

        partyTypeCombobox.getItems().addAll(
                Party.PartyType.ALL,
                Party.PartyType.CUSTOMER,
                Party.PartyType.MANUFACTURER,
                Party.PartyType.BANK,
                Party.PartyType.INDIVIDUAL);
        partyTypeCombobox.setConverter(new PartyTypeStringConverter());
        partyTypeCombobox.setOnAction(event -> {
            try {
                basicAccountBalanceTable.getItems().clear();
                basicAccountBalanceTable.getColumns().clear();
                coinBalanceTable.getItems().clear();
                coinBalanceTable.getColumns().clear();
                currencyBalanceTable.getItems().clear();
                currencyBalanceTable.getColumns().clear();
                baseBalanceMap.clear();
                loadOfficeBalances();
                loadTablesData();
            } catch (Exception e) {
                FxUtil.exceptionOccurred(e);
            }
        });

        selectedCurrencyCategoryProducts = new ArrayList<>();
        selectedCoinCategoryProducts = new ArrayList<>();

    }

    private EventHandler<TableColumn.CellEditEvent<AccountBalance, Double>> onBalanceCellCommitHandler(String cellName) {
        return new EventHandler<>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Object newValue = event.getNewValue();
                Object oldValue = event.getOldValue();
                if (newValue.equals(oldValue)) {
                    event.consume();
                    return;
                }
                try {
                    Double newValueAsDouble = Double.valueOf(String.valueOf(newValue));
                    Object rowObject = event.getTableView().getItems().get(event.getTablePosition().getRow());
                    AccountBalance accountBalance = (AccountBalance) rowObject;

                    Double currentCount = accountBalance.getCount();
                    if (currentCount == null) {
                        currentCount = 0.0;
                    }
                    switch (cellName) {
                        case "coinDebit", "currencyDebit" -> {
                            if (currentCount > 0) {
                                Double computedAmount = - Math.abs(newValueAsDouble);
                                accountBalance.setCount(computedAmount);
                            } else {
                                accountBalance.setCount(-Math.abs(newValueAsDouble));
                            }
                        }
                        case "coinCredit", "currencyCredit" -> {
                            if (currentCount < 0) {
                                Double computedAmount = Math.abs(newValueAsDouble);
                                accountBalance.setCount(computedAmount);
                            } else {
                                accountBalance.setCount(newValueAsDouble);
                            }
                        }
                    }

                    handleBalanceUpdate(accountBalance);
                    coinBalanceTable.refresh();
                    currencyBalanceTable.refresh();
                } catch (NumberFormatException e) {
                    event.consume();
                } catch (IndexOutOfBoundsException e) {
                    log.warn(e.getMessage());
                }
            }
        };
    }

    private EventHandler<TableColumn.CellEditEvent<AccountBalanceModel, Double>> onBaseBalanceCellCommitHandler(String cellName) {
        return new EventHandler<>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Object newValue = event.getNewValue();
                Object oldValue = event.getOldValue();
                if (newValue.equals(oldValue)) {
                    event.consume();
                    return;
                }
                try {
                    Double newValueAsDouble = Double.valueOf(String.valueOf(newValue));
                    if (newValueAsDouble < 0) {
                        FxUtil.error("value.must.be.grater.than.zero");
                        return;
                    }
                    Object rowObject = event.getTableView().getItems().get(event.getTablePosition().getRow());
                    AccountBalanceModel baseBalanceModel = (AccountBalanceModel) rowObject;
                    AccountBalance accountToBeUpdatedOrCreated = null;
                    switch (cellName) {
                        case "cashDebit" -> {
                            AccountBalance cashBalance = baseBalanceModel.getCashBalance();
                            if (cashBalance == null) {
                                cashBalance = new AccountBalance();
                                cashBalance.setCount(0.0);
                                baseBalanceModel.setCashBalance(cashBalance);
                            }
                            Double currentCount = cashBalance.getCount();
                            if (currentCount > 0) {
                                Double computedAmount = - Math.abs(newValueAsDouble);
                                cashBalance.setCount(computedAmount);
                            } else {
                                cashBalance.setCount(-Math.abs(newValueAsDouble));
                            }
                            accountToBeUpdatedOrCreated = cashBalance;
                        }
                        case "cashCredit" -> {
                            AccountBalance cashBalance = baseBalanceModel.getCashBalance();
                            if (cashBalance == null) {
                                cashBalance = new AccountBalance();
                                cashBalance.setCount(0.0);
                                baseBalanceModel.setCashBalance(cashBalance);
                            }
                            Double currentAmount = cashBalance.getCount();
                            if (currentAmount < 0) {
                                Double computedAmount = Math.abs(newValueAsDouble);
                                cashBalance.setCount(computedAmount);
                            } else {
                                cashBalance.setCount(newValueAsDouble);
                            }
                            accountToBeUpdatedOrCreated = cashBalance;
                        }
                        case "goldDebit" -> {
                            AccountBalance goldBalance = baseBalanceModel.getGoldBalance();
                            if (goldBalance == null) {
                                goldBalance = new AccountBalance();
                                goldBalance.setAmount(0.0);
                                baseBalanceModel.setGoldBalance(goldBalance);
                            }
                            Double currentAmount = goldBalance.getAmount();
                            if (currentAmount > 0) {
                                Double computedAmount = - Math.abs(newValueAsDouble);
                                goldBalance.setAmount(computedAmount);
                            } else {
                                goldBalance.setAmount(-Math.abs(newValueAsDouble));
                            }
                            accountToBeUpdatedOrCreated = goldBalance;
                        }
                        case "goldCredit" -> {
                            AccountBalance goldBalance = baseBalanceModel.getGoldBalance();
                            if (goldBalance == null) {
                                goldBalance = new AccountBalance();
                                goldBalance.setAmount(0.0);
                                baseBalanceModel.setGoldBalance(goldBalance);
                            }
                            Double currentAmount = goldBalance.getAmount();
                            if (currentAmount < 0) {
                                Double computedAmount = Math.abs(newValueAsDouble);
                                goldBalance.setAmount(computedAmount);
                            } else {
                                goldBalance.setAmount(newValueAsDouble);
                            }
                            accountToBeUpdatedOrCreated = goldBalance;
                        }
                    }
                    handleBalanceUpdate(accountToBeUpdatedOrCreated);
                    basicAccountBalanceTable.refresh();
                } catch (NumberFormatException e) {
                    event.consume();
                }
            }
        };
    }

    private Callback<TableColumn.CellDataFeatures<AccountBalanceModel, Double>, ObservableValue<Double>> goldOrCashCellValueFactory(boolean isCredit, boolean isGold) {
        return param -> new ObjectBinding<>() {
            @Override
            protected Double computeValue() {
                if (param == null || param.getValue() == null)
                    return 0.0;
                Double amount = 0.0;
                if (isGold && param.getValue().getGoldBalance() != null) {
                    amount = param.getValue().getGoldBalance().getAmount();
                } else if (!isGold && param.getValue().getCashBalance() != null) {
                    amount = param.getValue().getCashBalance().getCount();
                }
                if (isCredit) {
                    return amount > 0 ? Math.abs(amount) : 0;
                } else {
                    return amount < 0 ? Math.abs(amount) : 0;
                }
            }
        };
    }

    private Callback<TableColumn.CellDataFeatures<AccountBalance, Double>, ObservableValue<Double>> coinOrCurrencyCellValueFactory(boolean isCredit) {
        return param -> new ObjectBinding<>() {
            @Override
            protected Double computeValue() {
                if (param == null || param.getValue() == null)
                    return 0.0;

                AccountBalance accountBalance = param.getValue();
                Double amount = accountBalance.getCount(); // because it is coin or currency getCount is used
                if (amount == null) {
                    amount = 0.0;
                }
                if (isCredit) {
                    return amount > 0 ? Math.abs(amount) : 0;
                } else {
                    return amount < 0 ? Math.abs(amount) : 0;
                }
            }
        };
    }

    private void handleBalanceUpdate(AccountBalance accountBalance) {
        try {
            if (accountBalance != null) {
                Party party = accountBalance.getParty();
                if (party == null) {
                    AccountBalanceModel selectedBaseAccountBalanceModel = basicAccountBalanceTable.getSelectionModel().getSelectedItem();
                    party = selectedBaseAccountBalanceModel.getParty();
                    if (selectedBaseAccountBalanceModel != null && party != null) {
                        accountBalance.setParty(party);
                    } else {
                        FxUtil.warning("no.party.is.selected");
                        log.warn("selected BaseAccountBalanceModel is null. Cannot create or update AccountBalance");
                        return;
                    }
                }
                Dao.CreateOrUpdateStatus createOrUpdateStatus = accountBalanceService.createOrUpdate(accountBalance);
                if (createOrUpdateStatus.isCreated()) {
                    String productCategoryCode = accountBalance.getProduct().getProductCategory().getCode();
                    if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value())) {
                        List<AccountBalance> currencyBalanceList = baseBalanceMap.get(party).getCurrencyBalanceList();
                        if (currencyBalanceList == null) {
                            currencyBalanceList = new ArrayList<>();
                        }
                        currencyBalanceList.add(accountBalance);
                    } else if (productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                        List<AccountBalance> coinBalanceList = baseBalanceMap.get(party).getCoinBalanceList();
                        if (coinBalanceList == null) {
                            coinBalanceList = new ArrayList<>();
                        }
                        coinBalanceList.add(accountBalance);
                    }
                }
            }
        } catch (Exception e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    private void handleAccountBalanceProductUpdate(TableColumn.CellEditEvent<AccountBalance, Product> event) {
        try {
            AccountBalance accountBalance = event.getTableView().getItems().get(event.getTablePosition().getRow());
            Product newValue = event.getNewValue();
            Product oldValue = event.getOldValue();
            if (newValue != null && FxUtil.confirm("change.stock.product.type.confirm")) {
                accountBalance.setProduct(newValue);
                selectCurrencyCategoryProductsItem(newValue);
                removeCurrencyCategoryProductsItem(oldValue);
                accountBalanceService.createOrUpdate(accountBalance);
            } else if (oldValue != null) {
                accountBalance.setProduct(oldValue);
            }
        } catch (Exception e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    private void loadTablesData() throws SQLException {
        loadBaseAccountBalanceTableData();
    }

    private void loadCurrencyAccountBalanceTableData() throws SQLException {
        currencyBalanceTable.getColumns().clear();
        var doubleCellFactoryForBalance = EditCell.<AccountBalance, Double>forTableColumn(getDoubleCellFactoryForAccountBalance());

        notSelectedCurrencyCategoryProducts = productService.getProductsByCategoryCode(BasicProductCategoryCode.CURRENCY.value());
        var currencyNameColumn = new TableColumn<AccountBalance, Product>(message("product.name"));
        currencyNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AccountBalance, Product>, ObservableValue<Product>>() {
            @Override
            public ObservableValue<Product> call(TableColumn.CellDataFeatures<AccountBalance, Product> cell) {
                ObservableValue<Product> productObservableValue = new ObjectBinding<Product>() {
                    @Override
                    protected Product computeValue() {
                        return cell.getValue().getProduct();
                    }
                };
                return productObservableValue;
            }
        });

        currencyNameColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<Product>() {

            @Override
            public String toString(Product object) {
                if (object != null && object.getProductName() != null) return object.getProductName();
                if (notSelectedCurrencyCategoryProducts != null && notSelectedCurrencyCategoryProducts.size() > 0) {
                    return notSelectedCurrencyCategoryProducts.get(0).getProductName();
                }
                return "";
            }

            @Override
            public Product fromString(String string) {
                for (Product categoryProduct : notSelectedCurrencyCategoryProducts) {
                    if (categoryProduct.getProductName() != null && categoryProduct.getProductName().equals(string)) {
                        return categoryProduct;
                    }
                }
                if (notSelectedCurrencyCategoryProducts != null && notSelectedCurrencyCategoryProducts.size() > 0) {
                    return notSelectedCurrencyCategoryProducts.get(0);
                }
                return new Product();
            }
        }, FXCollections.observableList(notSelectedCurrencyCategoryProducts)));

        currencyNameColumn.setOnEditCommit(new EventHandler<>() {
            @Override
            public void handle(TableColumn.CellEditEvent<AccountBalance, Product> event) {
                handleAccountBalanceProductUpdate(event);
            }
        });

        var currencyDebitColumn = new TableColumn<AccountBalance, Double>(message("currency.debit"));
        currencyDebitColumn.setCellValueFactory(coinOrCurrencyCellValueFactory(false));
        currencyDebitColumn.setEditable(true);
        currencyDebitColumn.getStyleClass().add("debit-column");
        currencyDebitColumn.setCellFactory(doubleCellFactoryForBalance);
        currencyDebitColumn.setOnEditCommit(onBalanceCellCommitHandler("currencyDebit"));

        var currencyCreditColumn = new TableColumn<AccountBalance, Double>(message("currency.credit"));
        currencyCreditColumn.setCellValueFactory(coinOrCurrencyCellValueFactory(true));
        currencyCreditColumn.getStyleClass().add("credit-column");
        currencyCreditColumn.setEditable(true);
        currencyCreditColumn.setCellFactory(doubleCellFactoryForBalance);
        currencyCreditColumn.setOnEditCommit(onBalanceCellCommitHandler("currencyCredit"));


        TableColumn<AccountBalance, Button> currencyDeleteColumn = new TableColumn<>(message("remove"));
        currencyDeleteColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
        currencyDeleteColumn.setCellFactory(ActionButtonTableCell.<AccountBalance>forTableColumn(message("remove"), (AccountBalance accountBalance) -> {
            try {
                if (accountBalance != null) {
                    if (FxUtil.confirm("remove.confirm")) {
                        removeCurrencyCategoryProductsItem(accountBalance.getProduct());
                        accountBalanceService.remove(accountBalance);
                        currencyBalanceTable.getItems().remove(accountBalance);
                    }
                }
            } catch (Exception e) {
                FxUtil.exceptionOccurred(e);
            }
            return accountBalance;
        }));

        currencyBalanceTable.getColumns().addAll(currencyNameColumn, currencyDebitColumn, currencyCreditColumn, currencyDeleteColumn);
        currencyBalanceTable.setEditable(true);
        FxUtil.autoResizeColumns(currencyBalanceTable);
    }

    private void loadCoinBalanceTableData() throws SQLException {
        coinBalanceTable.getColumns().clear();
        var doubleCellFactoryForBalance = EditCell.<AccountBalance, Double>forTableColumn(getDoubleCellFactoryForAccountBalance());
        TableColumn<AccountBalance, Product> coinNameColumn = new TableColumn<AccountBalance, Product>(message("product.name"));
        coinNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AccountBalance, Product>, ObservableValue<Product>>() {
            @Override
            public ObservableValue<Product> call(TableColumn.CellDataFeatures<AccountBalance, Product> cell) {
                ObservableValue<Product> productObservableValue = new ObjectBinding<>() {
                    @Override
                    protected Product computeValue() {
                        return cell.getValue().getProduct();
                    }
                };
                return productObservableValue;
            }
        });

        notSelectedCoinCategoryProducts = productService.getProductsByCategoryCode(BasicProductCategoryCode.COIN.value());
        coinNameColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<Product>() {

            @Override
            public String toString(Product object) {
                if (object != null && object.getProductName() != null) return object.getProductName();
                if (notSelectedCoinCategoryProducts != null && notSelectedCoinCategoryProducts.size() > 0) {
                    return notSelectedCoinCategoryProducts.get(0).getProductName();
                }
                return "";
            }

            @Override
            public Product fromString(String string) {
                for (Product categoryProduct : notSelectedCoinCategoryProducts) {
                    if (categoryProduct.getProductName() != null && categoryProduct.getProductName().equals(string)) {
                        return categoryProduct;
                    }
                }
                if (notSelectedCoinCategoryProducts != null && notSelectedCoinCategoryProducts.size() > 0) {
                    return notSelectedCoinCategoryProducts.get(0);
                }
                return new Product();
            }
        }, FXCollections.observableList(notSelectedCoinCategoryProducts)));

        coinNameColumn.setOnEditCommit(event -> handleAccountBalanceProductUpdate(event));

        var coinDebitColumn = new TableColumn<AccountBalance, Double>(message("coin.debit"));
        coinDebitColumn.setCellValueFactory(coinOrCurrencyCellValueFactory(false));
        coinDebitColumn.getStyleClass().add("debit-column");
        coinDebitColumn.setEditable(true);
        coinDebitColumn.setCellFactory(doubleCellFactoryForBalance);
        coinDebitColumn.setOnEditCommit(onBalanceCellCommitHandler("coinDebit"));


        var coinCreditColumn = new TableColumn<AccountBalance, Double>(message("coin.credit"));
        coinCreditColumn.setCellValueFactory(coinOrCurrencyCellValueFactory(true));
        coinCreditColumn.setEditable(true);
        coinCreditColumn.getStyleClass().add("credit-column");
        coinCreditColumn.setCellFactory(doubleCellFactoryForBalance);
        coinCreditColumn.setOnEditCommit(onBalanceCellCommitHandler("coinCredit"));


        TableColumn<AccountBalance, Button> coinDeleteColumn = new TableColumn<>(message("remove"));
        coinDeleteColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
        coinDeleteColumn.setCellFactory(ActionButtonTableCell.<AccountBalance>forTableColumn(message("remove"), (AccountBalance accountBalance) -> {
            try {
                if (accountBalance != null) {
                    if (FxUtil.confirm("remove.confirm")) {
                        removeCoinCategoryProductsItem(accountBalance.getProduct());
                        accountBalanceService.remove(accountBalance);
                        coinBalanceTable.getItems().remove(accountBalance);
                    }
                }
            } catch (Exception e) {
                FxUtil.exceptionOccurred(e);
            }
            return accountBalance;
        }));

        coinBalanceTable.getColumns().addAll(coinNameColumn, coinDebitColumn, coinCreditColumn, coinDeleteColumn);
        coinBalanceTable.setEditable(true);
        FxUtil.autoResizeColumns(coinBalanceTable);
    }

    private void loadBaseAccountBalanceTableData() {

        var doubleCellFactoryForBaseAccountBalanceModel = EditCell.<AccountBalanceModel, Double>forTableColumn(getDoubleCellFactoryForBaseAccountBalanceModel());

        var partyTypeColumn = new TableColumn<AccountBalanceModel, String>(message("party.type"));
        partyTypeColumn.setCellValueFactory(object -> new SimpleStringProperty(message("party.type." + object.getValue().getParty().getPartyType())));

        var partyNameColumn = new TableColumn<AccountBalanceModel, String>(message("party"));
        partyNameColumn.setCellValueFactory(object -> new SimpleStringProperty(object.getValue().getParty().getPartyName()));

        var partyCodeColumn = new TableColumn<AccountBalanceModel, String>(message("party.code"));
        partyCodeColumn.setCellValueFactory(object -> new SimpleStringProperty(object.getValue().getParty().getCode()));

        var partyCashDebitColumn = new TableColumn<AccountBalanceModel, Double>(message("cash.debit"));
        partyCashDebitColumn.setCellValueFactory(goldOrCashCellValueFactory(false, false));
        partyCashDebitColumn.getStyleClass().add("debit-column");
        partyCashDebitColumn.setEditable(true);
        partyCashDebitColumn.setCellFactory(doubleCellFactoryForBaseAccountBalanceModel);
        partyCashDebitColumn.setOnEditCommit(onBaseBalanceCellCommitHandler("cashDebit"));

        var partyCashCreditColumn = new TableColumn<AccountBalanceModel, Double>(message("cash.credit"));
        partyCashCreditColumn.setCellValueFactory(goldOrCashCellValueFactory(true, false));
        partyCashCreditColumn.getStyleClass().add("credit-column");
        partyCashCreditColumn.setEditable(true);
        partyCashCreditColumn.setCellFactory(doubleCellFactoryForBaseAccountBalanceModel);
        partyCashCreditColumn.setOnEditCommit(onBaseBalanceCellCommitHandler("cashCredit"));

        var partyGoldDebitColumn = new TableColumn<AccountBalanceModel, Double>(message("gold.debit"));
        partyGoldDebitColumn.setCellValueFactory(goldOrCashCellValueFactory(false, true));
        partyGoldDebitColumn.setEditable(true);
        partyGoldDebitColumn.getStyleClass().add("debit-column");
        partyGoldDebitColumn.setCellFactory(doubleCellFactoryForBaseAccountBalanceModel);
        partyGoldDebitColumn.setOnEditCommit(onBaseBalanceCellCommitHandler("goldDebit"));

        var partyGoldCreditColumn = new TableColumn<AccountBalanceModel, Double>(message("gold.credit"));
        partyGoldCreditColumn.setCellValueFactory(goldOrCashCellValueFactory(true, true));
        partyGoldCreditColumn.getStyleClass().add("credit-column");
        partyGoldCreditColumn.setEditable(true);
        partyGoldCreditColumn.setCellFactory(doubleCellFactoryForBaseAccountBalanceModel);
        partyGoldCreditColumn.setOnEditCommit(onBaseBalanceCellCommitHandler("goldCredit"));

        basicAccountBalanceTable.getColumns().addAll(partyTypeColumn, partyNameColumn, partyCodeColumn, partyCashDebitColumn, partyCashCreditColumn, partyGoldDebitColumn, partyGoldCreditColumn);
        basicAccountBalanceTable.setEditable(true);
        basicAccountBalanceTable.getItems().addAll(baseBalanceMap.values());
        basicAccountBalanceTable.getSelectionModel().selectedItemProperty().addListener((baseAccountBalanceModelObservable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    loadCurrencyAccountBalanceTableData();
                    loadCoinBalanceTableData();
                } catch (SQLException sqlException) {
                    FxUtil.exceptionOccurred(sqlException);
                }
                AccountBalanceModel accountBalanceModel = baseAccountBalanceModelObservable.getValue();
                coinBalanceTable.getItems().clear();
                if (accountBalanceModel.getCoinBalanceList() != null) {
                    List<AccountBalance> coinBalanceList = accountBalanceModel.getCoinBalanceList();
                    coinBalanceTable.getItems().addAll(coinBalanceList);
                    for (AccountBalance coinBalance : coinBalanceList) {
                        selectCoinCategoryProductsItem(coinBalance.getProduct());
                    }
                }
                currencyBalanceTable.getItems().clear();
                if (accountBalanceModel.getCurrencyBalanceList() != null) {
                    List<AccountBalance> currencyBalanceList = accountBalanceModel.getCurrencyBalanceList();
                    currencyBalanceTable.getItems().addAll(currencyBalanceList);
                    for (AccountBalance currencyBalance : currencyBalanceList) {
                        selectCurrencyCategoryProductsItem(currencyBalance.getProduct());
                    }
                }
            }
        });
        FxUtil.autoResizeColumns(basicAccountBalanceTable);
    }

    private void loadOfficeBalances() {
        try {
            List<AccountBalance> officeBalanceModels = accountBalanceService.getBalancesByPartyType(partyTypeCombobox.getSelectionModel().getSelectedItem());
            for (AccountBalance accountBalance : officeBalanceModels) {
                Product accountBalanceProduct = accountBalance.getProduct();
                String productCategoryCode = accountBalanceProduct.getProductCategory().getCode();
                Party accountBalanceParty = accountBalance.getParty();

                AccountBalanceModel accountBalanceModel;
                if (baseBalanceMap.containsKey(accountBalanceParty)) {
                    accountBalanceModel = baseBalanceMap.get(accountBalanceParty);
                } else {
                    accountBalanceModel = new AccountBalanceModel();
                    accountBalanceModel.setParty(accountBalanceParty);
                    baseBalanceMap.put(accountBalanceParty, accountBalanceModel);
                }

                if (productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                    List<AccountBalance> coinBalanceList = accountBalanceModel.getCoinBalanceList();
                    if (coinBalanceList == null) {
                        coinBalanceList = new ArrayList<>();
                    }
                    coinBalanceList.add(accountBalance);
                    accountBalanceModel.setCoinBalanceList(coinBalanceList);
                } else if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value())) {
                    List<AccountBalance> currencyBalanceList = accountBalanceModel.getCurrencyBalanceList();
                    if (currencyBalanceList == null) {
                        currencyBalanceList = new ArrayList<>();
                    }
                    currencyBalanceList.add(accountBalance);
                    accountBalanceModel.setCurrencyBalanceList(currencyBalanceList);
                } else {
                    if (productCategoryCode.equals(BasicProductCategoryCode.CASH.value())) {
                        accountBalanceModel.setCashBalance(accountBalance);
                    } else if (productCategoryCode.equals(BasicProductCategoryCode.MELTED.value()) &&
                            // TODO: make sure MELTED_GOLD should be used here or MSC_GOLD
                            accountBalanceProduct.getCode().equals(BasicProductCode.MELTED_GOLD.value())) {
                        accountBalanceModel.setGoldBalance(accountBalance);
                    }
                }
            }

        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
    }

    @FXML
    private void onCoinBalanceAdd(ActionEvent event) {
        AccountBalance accountBalance = new AccountBalance();
        if (notSelectedCoinCategoryProducts != null && notSelectedCoinCategoryProducts.size() > 0) {
            Product product = notSelectedCoinCategoryProducts.get(0);
            selectCoinCategoryProductsItem(product);
            accountBalance.setProduct(product);
            coinBalanceTable.getItems().addAll(accountBalance);
        } else {
            FxUtil.error("no.coin.product.exists");
        }
    }

    @FXML
    private void onCurrencyBalanceAdd(ActionEvent event) {
        AccountBalance accountBalance = new AccountBalance();
        if (notSelectedCurrencyCategoryProducts != null && notSelectedCurrencyCategoryProducts.size() > 0) {
            Product product = notSelectedCurrencyCategoryProducts.get(0);
            selectCurrencyCategoryProductsItem(product);
            accountBalance.setProduct(product);
            currencyBalanceTable.getItems().addAll(accountBalance);
        } else {
            FxUtil.error("no.currency.product.exists");
        }
    }

    protected void selectCoinCategoryProductsItem(Product product) {
        for (int i = 0; i < notSelectedCoinCategoryProducts.size(); i++) {
            Product p = notSelectedCoinCategoryProducts.get(i);
            if (p.getId().equals(product.getId())) {
                notSelectedCoinCategoryProducts.remove(p);
                selectedCoinCategoryProducts.add(p);
            }
        }
    }

    protected void removeCoinCategoryProductsItem(Product product) {
        for (int i = 0; i < selectedCoinCategoryProducts.size(); i++) {
            Product p = selectedCoinCategoryProducts.get(i);
            if (p.getId().equals(product.getId())) {
                notSelectedCoinCategoryProducts.add(p);
                selectedCoinCategoryProducts.remove(p);
            }
        }
    }

    protected void selectCurrencyCategoryProductsItem(Product product) {
        for (int i = 0; i < notSelectedCurrencyCategoryProducts.size(); i++) {
            Product p = notSelectedCurrencyCategoryProducts.get(i);
            if (p.getId().equals(product.getId())) {
                notSelectedCurrencyCategoryProducts.remove(p);
                selectedCurrencyCategoryProducts.add(p);
            }
        }
    }

    protected void removeCurrencyCategoryProductsItem(Product product) {
        for (int i = 0; i < selectedCurrencyCategoryProducts.size(); i++) {
            Product p = selectedCurrencyCategoryProducts.get(i);
            if (p.getId().equals(product.getId())) {
                notSelectedCurrencyCategoryProducts.add(p);
                selectedCurrencyCategoryProducts.remove(p);
            }
        }
    }
}
