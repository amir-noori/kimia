package ir.kimia.client.controller;

import com.google.common.eventbus.Subscribe;
import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.data.model.*;
import ir.kimia.client.event.CloseCommandEvent;
import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.api.*;
import ir.kimia.client.ui.*;
import ir.kimia.client.util.FxUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;


public abstract class BaseController implements Initializable {

    private static final Logger log = LogManager.getLogger(BaseController.class);

    protected static final ResourceBundle resourceBundle = ApplicationContext.getResourceBundle();

    protected Stage stage;

    protected List<Node> focusNodes;

    protected FormFocusController formFocusController;

    protected boolean isFormDataValid = true;

    ValidationSupport validationSupport;

    protected SystemParameterService systemParameterService;
    protected StoneTypeService stoneTypeService;
    protected FinenessService finenessService;
    protected PurityEvaluatorService purityEvaluatorService;
    protected StockService stockService;
    protected PartyService partyService;
    protected ProductService productService;
    protected ProductCategoryService productCategoryService;
    protected BankService bankService;

    @Inject
    public void setBankService(BankService service) {
        bankService = service;
    }

    @Inject
    public void setProductCategoryService(ProductCategoryService service) {
        productCategoryService = service;
    }

    @Inject
    public void setProductService(ProductService service) {
        productService = service;
    }

    @Inject
    public void setPartyService(PartyService service) {
        partyService = service;
    }

    @Inject
    public void setStockService(StockService service) {
        stockService = service;
    }

    @Inject
    public void setFinenessService(FinenessService service) {
        finenessService = service;
    }

    @Inject
    public void setPurityEvaluatorService(PurityEvaluatorService service) {
        purityEvaluatorService = service;
    }

    @Inject
    public void setStoneTypeService(StoneTypeService service) {
        stoneTypeService = service;
    }

    @Inject
    public void setSystemParameterService(SystemParameterService parameterService) {
        systemParameterService = parameterService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            validationSupport = new ValidationSupport();
            ApplicationContext.getEventBus().register(this);
            init(url, resourceBundle);
        } catch (Exception e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    protected void showTooltipOnFocus(Control control) {
        showTooltipOnFocus(control, false);
    }

    protected void showTooltipOnFocus(Control control, boolean showOnLoad) {
        final Stage currentStage;
        currentStage = ApplicationContext.getPrimaryStage();
        Screen screen = Screen.getPrimary();
        Rectangle2D visualBounds = screen.getVisualBounds();

        if (control != null) {
            Tooltip originalTooltip = control.getTooltip();
            if (originalTooltip != null) {
                Tooltip newTooltip = new Tooltip(originalTooltip.getText());
                newTooltip.setStyle("-fx-font-size: 18;");
                control.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (control.isFocused()) {
                        newTooltip.show(currentStage, visualBounds.getWidth() - 400, 100);
                    } else {
                        newTooltip.hide();
                    }
                });
                if (showOnLoad) {
                    newTooltip.show(currentStage, visualBounds.getWidth() - 400, 100);
                }
            }
        }
    }

    public void init(URL url, ResourceBundle resourceBundle) throws SQLException, ApplicationException {
        // Do nothing. This method should be overwritten by subclasses.
    }

    /**
     * postLoad can be overwritten by any controller and it will be called once stage is fully loaded.
     */
    public void postLoad() {
        initFocus();
        setShortcuts();
    }

    protected void setParam(String name, String value) {
        try {
            systemParameterService.setParameter(name, value);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
    }

    protected SystemParameter getParam(String name) {
        try {
            return systemParameterService.getParameter(name);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    protected String showDescriptionDialog(String initialDescription) {
        PopupDialog dialog = new PopupDialog(500);
        GridPane grid = dialog.getGrid();
        TextField description = new TextField(initialDescription != null ? initialDescription : "");
        grid.add(new Label(message("regard")), 0, 0);
        grid.add(description, 1, 0);
        dialog.setFocusNodes(description);
        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
            if (buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                return description.getText();
            } else if (buttonData.equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                return initialDescription;
            }
        }
        return null;
    }

    protected Bank showBankDialog(Bank initialBank) {
        try {
            PopupDialog dialog = new PopupDialog(500);
            GridPane grid = dialog.getGrid();
            ComboBox<Bank> bankCombobox = new ComboBox<>();
            List<Bank> allBanks = bankService.getAllBanks();
            bankCombobox.setConverter(new BankStringConverter(allBanks));
            bankCombobox.setEditable(true);
            bankCombobox.setItems(FXCollections.observableArrayList(allBanks));
            if (initialBank == null) {
                bankCombobox.getSelectionModel().selectFirst();
            } else {
                bankCombobox.getSelectionModel().select(initialBank);
            }
            new ComboBoxAutoComplete<>(bankCombobox);
            grid.add(new Label(message("bank")), 0, 0);
            grid.add(bankCombobox, 1, 0);

            dialog.setFocusNodes(bankCombobox);

            Optional result = dialog.showAndWait();
            if (result.isPresent()) {
                ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
                if (buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                    return bankCombobox.getSelectionModel().getSelectedItem();
                } else if (buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                    return initialBank;
                }
            }

        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }

        return null;
    }

    protected InvoiceRecord.DealType showDealTypeDialog(InvoiceRecord invoiceRecord) {

        InvoiceRecord.DealType dealType = null;
        if (invoiceRecord.getDealType() != null) {
            dealType = InvoiceRecord.DealType.getByValue(invoiceRecord.getDealType());
        }

        Product product = invoiceRecord.getProduct();
        List<InvoiceRecord.DealType> dealTypeChoices = new ArrayList<>();

        if (product != null) {
            ProductCategory productCategory = product.getProductCategory();
            boolean isCountable = productCategory.getCountable() != null ? productCategory.getCountable() : false;
            if (productCategory != null) {
                String productCategoryCode = productCategory.getCode();
                if (productCategoryCode.equals(BasicProductCategoryCode.MELTED.value())
                        || productCategoryCode.equals(BasicProductCategoryCode.COIN.value())
                        || productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value())) {
                    dealTypeChoices.add(InvoiceRecord.DealType.PURCHASE);
                    dealTypeChoices.add(InvoiceRecord.DealType.SELL);
                    dealTypeChoices.add(InvoiceRecord.DealType.RECEIVE);
                    dealTypeChoices.add(InvoiceRecord.DealType.PAYMENT);
                    dealTypeChoices.add(InvoiceRecord.DealType.PURCHASE_BY_CASH);
                    dealTypeChoices.add(InvoiceRecord.DealType.SELL_BY_CASH);
                } else if (productCategoryCode.equals(BasicProductCategoryCode.MSC.value())) {
                    dealTypeChoices.add(InvoiceRecord.DealType.PURCHASE);
                    dealTypeChoices.add(InvoiceRecord.DealType.SELL);
                    dealTypeChoices.add(InvoiceRecord.DealType.RECEIVE);
                    dealTypeChoices.add(InvoiceRecord.DealType.PAYMENT);
                } else if (!isCountable) {
                    dealTypeChoices.add(InvoiceRecord.DealType.PURCHASE);
                    dealTypeChoices.add(InvoiceRecord.DealType.SELL);
                    dealTypeChoices.add(InvoiceRecord.DealType.RECEIVE);
                    dealTypeChoices.add(InvoiceRecord.DealType.PAYMENT);
                    dealTypeChoices.add(InvoiceRecord.DealType.RECEIVE_RETURNED);
                    dealTypeChoices.add(InvoiceRecord.DealType.PAYMENT_RETURNED);
                } else if (productCategoryCode.equals(BasicProductCategoryCode.STONE.value())) {
                    dealTypeChoices.add(InvoiceRecord.DealType.PURCHASE);
                    dealTypeChoices.add(InvoiceRecord.DealType.SELL);
                } else if (productCategoryCode.equals(BasicProductCategoryCode.CASH.value())) {
                    dealTypeChoices.add(InvoiceRecord.DealType.RECEIVE);
                    dealTypeChoices.add(InvoiceRecord.DealType.PAYMENT);
                }

            } else {
                final int invoiceRecordTypeValue = invoiceRecord.getInvoiceRecordType().value();
                if (invoiceRecordTypeValue == InvoiceRecord.InvoiceRecordType.BANK.value() ||
                        invoiceRecordTypeValue == InvoiceRecord.InvoiceRecordType.CHEQUE.value()) {
                    dealTypeChoices.add(InvoiceRecord.DealType.RECEIVE);
                    dealTypeChoices.add(InvoiceRecord.DealType.PAYMENT);
                }
            }


        } else { // for non product types invoice records

            InvoiceRecord.InvoiceRecordType invoiceRecordType = invoiceRecord.getInvoiceRecordType();
            switch (invoiceRecordType) {
                case PAYMENT_ORDER_CASH, PAYMENT_ORDER_CURRENCY, PAYMENT_ORDER_GOLD, BANK, CHEQUE -> {
                    dealTypeChoices.add(InvoiceRecord.DealType.RECEIVE);
                    dealTypeChoices.add(InvoiceRecord.DealType.PAYMENT);
                }
                case CASH_TO_GOLD_CONVERSION, GOLD_TO_CASH_CONVERSION -> {
                    dealTypeChoices.add(InvoiceRecord.DealType.SELL_BY_CASH);
                    dealTypeChoices.add(InvoiceRecord.DealType.PURCHASE_BY_CASH);
                }
            }
        }

        PopupDialog dialog = new PopupDialog(500);
        GridPane grid = dialog.getGrid();

        ComboBox<InvoiceRecord.DealType> dealTypeCombobox = new ComboBox<>();
        dealTypeCombobox.setConverter(new DealTypeStringConverter(dealTypeChoices));
        dealTypeCombobox.setItems(FXCollections.observableArrayList(dealTypeChoices));
        dealTypeCombobox.getSelectionModel().selectFirst();
        grid.add(new Label(message("deal.type")), 0, 0);
        grid.add(dealTypeCombobox, 1, 0);
        dialog.setFocusNodes(dealTypeCombobox);

        Optional result = dialog.showAndWait(o -> {
            // TODO: show dealTypeCombobox list after shown
            System.out.println(dialog);
            System.out.println(dealTypeCombobox);
            return null;
        });
        if (result.isPresent()) {
            ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
            if (buttonData.equals(ButtonBar.ButtonData.OK_DONE) || buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                return dealTypeCombobox.getSelectionModel().getSelectedItem();
            }
        }

        return dealType;
    }

    protected Boolean showGoldAndCashConversionDialog(Boolean isGoldSelected) {
        return showBinarySelectDialog(isGoldSelected, message("cash_to_gold_conversion"), message("gold_to_cash_conversion"));
    }


    protected Boolean showBinarySelectDialog(Boolean isFirstSelected, String firstChoice, String secondChoice) {
        PopupDialog dialog = new PopupDialog(500, true);
        GridPane grid = dialog.getGrid();

        final ToggleGroup group = new ToggleGroup();

        RadioButton firstChoiceRadioButton = new RadioButton(firstChoice);
        firstChoiceRadioButton.setToggleGroup(group);

        RadioButton secondChoiceRadioButton = new RadioButton(secondChoice);
        secondChoiceRadioButton.setToggleGroup(group);

        if (isFirstSelected) {
            firstChoiceRadioButton.setSelected(true);
        } else {
            secondChoiceRadioButton.setSelected(true);
        }

        grid.add(firstChoiceRadioButton, 0, 0);
        grid.add(secondChoiceRadioButton, 0, 1);

        dialog.getDialog().getDialogPane().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode().equals(KeyCode.SPACE)) {
                if (firstChoiceRadioButton.isSelected()) {
                    secondChoiceRadioButton.setSelected(true);
                } else {
                    firstChoiceRadioButton.setSelected(true);
                }
                event.consume();
            }
        });

        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
            if (buttonData.equals(ButtonBar.ButtonData.OK_DONE)) {
                return firstChoiceRadioButton.isSelected();
            } else if (buttonData.equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                return isFirstSelected;
            }
        }


        return null;
    }

    protected Product showCoinOrCurrencySelectionDialog(Product initialProduct, String categoryCode) {
        try {
            PopupDialog dialog = new PopupDialog(500);
            GridPane grid = dialog.getGrid();
            ComboBox<Product> productCombobox = new ComboBox<>();
            List<Product> allProducts = productService.getProductsByCategoryCode(categoryCode);
            productCombobox.setConverter(new ProductStringConverter(allProducts));
            productCombobox.setEditable(true);
            productCombobox.setItems(FXCollections.observableArrayList(allProducts));
            productCombobox.getSelectionModel().selectFirst();
            new ComboBoxAutoComplete<>(productCombobox);
            grid.add(new Label(message("product.name")), 0, 0);
            grid.add(productCombobox, 1, 0);

            dialog.setFocusNodes(productCombobox);

            Optional result = dialog.showAndWait();
            if (result.isPresent()) {
                ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
                if (buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                    return productCombobox.getSelectionModel().getSelectedItem();
                } else if (buttonData.equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                    return initialProduct;
                }
            }
        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }

        return null;
    }

    protected Party showPartySelectionDialog(Party initialParty) {
        try {
            PopupDialog dialog = new PopupDialog(500);
            GridPane grid = dialog.getGrid();
            ComboBox<Party> partyCombobox = new ComboBox<>();
            List<Party> allParties = partyService.getAllParties();
            partyCombobox.setConverter(new PartyStringConverter(allParties));
            partyCombobox.setEditable(true);
            partyCombobox.setItems(FXCollections.observableArrayList(allParties));
            partyCombobox.getSelectionModel().selectFirst();
            new ComboBoxAutoComplete<>(partyCombobox);
            grid.add(new Label(message("party.name")), 0, 0);
            grid.add(partyCombobox, 1, 0);

            dialog.setFocusNodes(partyCombobox);

            Optional result = dialog.showAndWait();
            if (result.isPresent()) {
                ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
                if (buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                    return partyCombobox.getSelectionModel().getSelectedItem();
                }
            }


        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }

        return null;
    }

    protected List<Stock> showMeltedStockWithFinenessDialog(Double initialWeight) {

        Callback<TableColumn.CellDataFeatures<Pair<Stock, SimpleBooleanProperty>, String>, ObservableValue<String>> cellValueFactoryCallback = new Callback<>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<Stock, SimpleBooleanProperty>, String> param) {
                return new ObjectBinding<>() {
                    @Override
                    protected String computeValue() {
                        if (param != null) {
                            String columnId = param.getTableColumn().getId();
                            Stock stock = param.getValue().getLeft();
                            DecimalFormat df = new DecimalFormat("0");
                            df.setMaximumFractionDigits(10);
                            Fineness fineness = stock.getFineness();
                            PurityEvaluator purityEvaluator = null;
                            if (fineness != null) {
                                purityEvaluator = fineness.getPurityEvaluator();
                            }

                            switch (columnId) {
                                case "partyName" -> {
                                    Party party = stock.getParty();
                                    if (party != null) {
                                        return stock.getParty().getPartyName();
                                    }
                                }
                                case "finenessNumber" -> {
                                    if (fineness != null) {
                                        return String.valueOf(fineness.getFinenessNumber());
                                    }
                                }
                                case "evaluator" -> {
                                    if (fineness != null) {
                                        if (purityEvaluator != null) {
                                            return purityEvaluator.getName();
                                        }
                                    }
                                }
                                case "conditional" -> {
                                    if (fineness != null && fineness.getConditional()) {
                                        return String.valueOf(fineness.getConditional());
                                    }
                                }
                                case "carat" -> {
                                    if (stock.getCarat() != null) {
                                        return df.format(stock.getCarat());
                                    }
                                }
                                case "supply" -> {
                                    if (stock.getWeight() != null) {
                                        return df.format(stock.getWeight());
                                    }
                                }
                            }
                        }

                        return "";
                    }
                };

            }
        };

        try {

            List<Stock> officeMeltedStocks = stockService.getOfficeMeltedStocks();

            PopupDialog dialog = new PopupDialog(700);
            GridPane grid = dialog.getGrid();

            TextField finenessNumberInput = new TextField();
            finenessNumberInput.setPromptText(message("fineness.number"));
            grid.add(new Label(message("search.by.fineness.number")), 0, 0);
            grid.add(finenessNumberInput, 1, 0);

            TextField weightInput = new TextField();
            weightInput.setPromptText(message("weight"));
            grid.add(new Label(message("search.by.weight")), 0, 1);
            grid.add(weightInput, 1, 1);


            TableView<Pair<Stock, SimpleBooleanProperty>> meltedStocksTable = new TableView<>();
            meltedStocksTable.setEditable(true);
            meltedStocksTable.setPrefWidth(600);


            var selectColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, Boolean>(message("select"));
            selectColumn.setId("select");
            selectColumn.setEditable(true);
            Callback<TableColumn<Pair<Stock, SimpleBooleanProperty>, Boolean>, TableCell<Pair<Stock, SimpleBooleanProperty>, Boolean>> booleanCellFactory =
                    p -> {
                        BooleanCell booleanCell = new BooleanCell((BiFunction<TableRow, Boolean, Void>) (tableRow, value) -> {
                            ((Pair<Stock, SimpleBooleanProperty>) tableRow.getItem()).getRight().set(value);
                            return null;
                        });
                        return booleanCell;
                    };
            selectColumn.setCellFactory(booleanCellFactory);
            selectColumn.setCellValueFactory(param -> new ObjectBinding<Boolean>() {
                @Override
                protected Boolean computeValue() {
                    Pair<Stock, SimpleBooleanProperty> paramValue = param.getValue();
                    if (paramValue != null) {
                        return paramValue.getRight().get();
                    }
                    return false;
                }
            });

            meltedStocksTable.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
                if (event.getCode().equals(KeyCode.SPACE)) {
                    Pair<Stock, SimpleBooleanProperty> focusedItem = meltedStocksTable.getFocusModel().getFocusedItem();
                    Boolean isRowSelected = focusedItem.getRight().getValue();

                    // first check if the initial weight is not null, if it is null then multiple row can be selected.
                    if (initialWeight != null && initialWeight != 0 && meltedStocksTable.getItems() != null && !isRowSelected) {
                        for (Pair<Stock, SimpleBooleanProperty> item : meltedStocksTable.getItems()) {
                            boolean selected = item.getRight().get();
                            Stock stock = item.getLeft();
                            if (selected && !stock.getId().equals(focusedItem.getLeft().getId())) {
                                FxUtil.warning("cannot.select.multiple.rows");
                                meltedStocksTable.requestFocus();
                                return;
                            }
                        }
                    }

                    focusedItem.getRight().setValue(!isRowSelected);
                    meltedStocksTable.refresh();
                    meltedStocksTable.requestFocus();
                    event.consume();
                }
            });
            meltedStocksTable.setOnKeyPressed(event -> {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    dialog.fireConfirm();
                }
            });

            var partyNameColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("party.name"));
            partyNameColumn.setId("partyName");
            partyNameColumn.setCellValueFactory(cellValueFactoryCallback);

            var finenessNumberColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("fineness.number"));
            finenessNumberColumn.setId("finenessNumber");
            finenessNumberColumn.setCellValueFactory(cellValueFactoryCallback);

            var evaluatorColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("purity.evaluator"));
            evaluatorColumn.setId("evaluator");
            evaluatorColumn.setCellValueFactory(cellValueFactoryCallback);

            var conditionalColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("conditional"));
            conditionalColumn.setId("conditional");
            conditionalColumn.setCellValueFactory(cellValueFactoryCallback);

            var caratColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("carat"));
            caratColumn.setId("carat");
            caratColumn.setCellValueFactory(cellValueFactoryCallback);

            var supplyColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("supply"));
            supplyColumn.setId("supply");
            supplyColumn.setCellValueFactory(cellValueFactoryCallback);


            meltedStocksTable.getColumns().addAll(selectColumn, finenessNumberColumn, evaluatorColumn, conditionalColumn, caratColumn, supplyColumn);
            if (meltedStocksTable != null && meltedStocksTable.getItems() != null && officeMeltedStocks != null) {
                List<Pair<Stock, SimpleBooleanProperty>> items = new ArrayList<>();
                for (Stock stock : officeMeltedStocks) {
                    items.add(Pair.of(stock, new SimpleBooleanProperty(false)));
                }
                meltedStocksTable.getItems().addAll(items);
            }

            dialog.getvBox().getChildren().add(meltedStocksTable);

            Optional result = dialog.showAndWait(o -> {
                meltedStocksTable.requestFocus();
                if (meltedStocksTable.getItems() != null && meltedStocksTable.getItems().size() > 0) {
                    meltedStocksTable.getSelectionModel().select(0);
                    meltedStocksTable.getFocusModel().focus(0);
                    meltedStocksTable.getFocusModel().focus(0);
                    TablePosition firstCell = new TablePosition(meltedStocksTable, 0, meltedStocksTable.getColumns().get(0));
                    meltedStocksTable.getSelectionModel().selectFirst();
                    meltedStocksTable.getFocusModel().focus(firstCell);
                }
                return null;
            });
            if (result.isPresent()) {
                ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
                if (buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                    List<Stock> stocks = new ArrayList<>();
                    ObservableList<Pair<Stock, SimpleBooleanProperty>> items = meltedStocksTable.getItems();
                    for (Pair<Stock, SimpleBooleanProperty> item : items) {
                        if (item.getRight().get()) {
                            stocks.add(item.getLeft());
                        }
                    }

                    return stocks;
                }
            }

        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }

        return null;
    }

    protected Fineness showFinenessDialog(Fineness initialFineness) {
        try {
            PopupDialog dialog = new PopupDialog(500);
            GridPane grid = dialog.getGrid();
            ComboBox<PurityEvaluator> evaluatorsCombobox = new ComboBox<>();
            List<PurityEvaluator> allEvaluators = purityEvaluatorService.getAll();
            evaluatorsCombobox.setConverter(new PurityEvaluatorStringConverter(allEvaluators));
            evaluatorsCombobox.setItems(FXCollections.observableArrayList(allEvaluators));
            grid.add(new Label(message("purity.evaluator")), 0, 0);
            grid.add(evaluatorsCombobox, 1, 0);

            TextField finenessNumberInput = new TextField();
            finenessNumberInput.setPromptText(message("fineness.number"));
            grid.add(new Label(message("fineness.number")), 0, 1);
            grid.add(finenessNumberInput, 1, 1);

            ComboBox<Boolean> conditionalCombobox = new ComboBox<>();
            conditionalCombobox.setConverter(new StringConverter<Boolean>() {
                @Override
                public String toString(Boolean object) {
                    if (object != null && object) {
                        return message("yes");
                    } else {
                        return message("no");
                    }
                }

                @Override
                public Boolean fromString(String string) {
                    if (StringUtils.isNotEmpty(string)) {
                        return string.equals(message("yes"));
                    } else {
                        return false;
                    }
                }
            });
            conditionalCombobox.setItems(FXCollections.observableArrayList(true, false));
            grid.add(new Label(message("conditional")), 0, 2);
            grid.add(conditionalCombobox, 1, 2);

            if (initialFineness != null) {
                finenessNumberInput.setText(String.valueOf(initialFineness.getFinenessNumber()));
            }

            dialog.setFocusNodes(evaluatorsCombobox, finenessNumberInput, conditionalCombobox);

            Optional result = dialog.showAndWait();
            if (result.isPresent()) {
                ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
                if (buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                    Fineness fineness = new Fineness();
                    boolean conditional = conditionalCombobox.getValue() != null ? conditionalCombobox.getValue() : false;
                    fineness.setConditional(conditional);
                    String finenessNumberText = finenessNumberInput.getText();
                    Double finenessNumber = 0.0;
                    try {
                        finenessNumber = Double.parseDouble(finenessNumberText);
                    } catch (NumberFormatException e) {
                    }
                    fineness.setFinenessNumber(finenessNumber);
                    fineness.setPurityEvaluator(evaluatorsCombobox.getSelectionModel().getSelectedItem());
                    return fineness;
                }
            }

        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }

        return null;
    }

    protected List<Stone> showStoneDialog(List<Stone> initialStones) {
        try {
            Callback<TableColumn.CellDataFeatures<Stone, String>, ObservableValue<String>> stoneTableCellValueFactoryCallback = new Callback<>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Stone, String> param) {
                    return new ObjectBinding<>() {
                        @Override
                        protected String computeValue() {
                            if (param != null) {
                                String columnId = param.getTableColumn().getId();
                                Stone stone = param.getValue();
                                DecimalFormat df = new DecimalFormat("0");
                                df.setMaximumFractionDigits(10);

                                switch (columnId) {
                                    case "stoneName" -> {
                                        return stone.getStoneType().getName();
                                    }
                                    case "stoneWight" -> {
                                        return df.format(stone.getWeight());
                                    }
                                    case "stoneFee" -> {
                                        return df.format(stone.getFee());
                                    }
                                    case "stonePrice" -> {
                                        return df.format(stone.getPrice());
                                    }
                                }
                            }

                            return "";
                        }
                    };

                }
            };

            PopupDialog dialog = new PopupDialog(700);
            dialog.setIncludeSubmitInFocusNodes(false);

            TableView<Stone> stonesTable = new TableView<>();
            stonesTable.setPrefWidth(700);

            var stoneNameColumn = new TableColumn<Stone, String>(message("stone.name"));
            stoneNameColumn.setId("stoneName");
            stoneNameColumn.setCellValueFactory(stoneTableCellValueFactoryCallback);

            var stoneWightColumn = new TableColumn<Stone, String>(message("weight"));
            stoneWightColumn.setId("stoneWight");
            stoneWightColumn.setCellValueFactory(stoneTableCellValueFactoryCallback);

            var stoneFeeColumn = new TableColumn<Stone, String>(message("fee"));
            stoneFeeColumn.setId("stoneFee");
            stoneFeeColumn.setCellValueFactory(stoneTableCellValueFactoryCallback);

            var stonePriceColumn = new TableColumn<Stone, String>(message("stone.price"));
            stonePriceColumn.setId("stonePrice");
            stonePriceColumn.setCellValueFactory(stoneTableCellValueFactoryCallback);

            TableColumn<Stone, Button> deleteColumn = new TableColumn<>(message("remove"));
            deleteColumn.setCellFactory(ActionButtonTableCell.<Stone>forTableColumn(message("remove"), (Stone targetStone) -> {
                boolean confirmed = FxUtil.confirm("remove.confirm");
                if (confirmed) {
                    stonesTable.getItems().remove(targetStone);
                    stonesTable.refresh();
                }
                return targetStone;
            }));

            stonesTable.getColumns().addAll(stoneNameColumn, stoneWightColumn, stoneFeeColumn, stonePriceColumn, deleteColumn);
            if (initialStones != null && stonesTable.getItems() != null) {
                stonesTable.getItems().addAll(initialStones);
            }

            GridPane grid = dialog.getGrid();
            ComboBox<StoneType> stoneTypeCombobox = new ComboBox<>();
            List<StoneType> allStoneTypes = stoneTypeService.getAllStoneTypes();
            stoneTypeCombobox.setConverter(new StoneTypeStringConverter(allStoneTypes));
            stoneTypeCombobox.setItems(FXCollections.observableArrayList(allStoneTypes));
            stoneTypeCombobox.setPromptText(message("stone.type"));
            grid.add(new Label(message("stone.type")), 0, 0);
            grid.add(stoneTypeCombobox, 1, 0);

            EventHandler onFeeOrWeightChange = new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    System.out.println("");
                }
            };

            TextField weightTextInput = new TextField();
            weightTextInput.setPromptText(message("weight"));
            weightTextInput.addEventHandler(KeyEvent.KEY_RELEASED, onFeeOrWeightChange);
            grid.add(new Label(message("weight")), 0, 1);
            grid.add(weightTextInput, 1, 1);

            TextField feeTextInput = new TextField();
            feeTextInput.setPromptText(message("fee"));
            feeTextInput.addEventHandler(KeyEvent.KEY_RELEASED, onFeeOrWeightChange);
            grid.add(new Label(message("fee")), 0, 2);
            grid.add(feeTextInput, 1, 2);

            TextField priceTextInput = new TextField();
            priceTextInput.setPromptText(message("price"));
            priceTextInput.setEditable(false);
            grid.add(new Label(message("price")), 0, 3);
            grid.add(priceTextInput, 1, 3);

            Button addButton = new Button(message("add"));
            addButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    StoneType selectedStoneType = stoneTypeCombobox.getSelectionModel().getSelectedItem();
                    String weightText = weightTextInput.getText();
                    Double weight = 0.0;
                    String feeText = feeTextInput.getText();
                    Double fee = 0.0;

                    if (selectedStoneType == null) {
                        FxUtil.error("stone.type.is.required");
                        return;
                    }

                    if (StringUtils.isEmpty(weightText)) {
                        FxUtil.error("weight.is.required");
                        return;
                    } else {
                        try {
                            weight = Double.parseDouble(weightText);
                        } catch (NumberFormatException e) {
                            FxUtil.error("weight.is.invalid");
                            return;
                        }
                    }

                    if (StringUtils.isEmpty(feeText)) {
                        FxUtil.error("fee.is.required");
                        return;
                    } else {
                        try {
                            fee = Double.parseDouble(feeText);
                        } catch (NumberFormatException e) {
                            FxUtil.error("fee.is.invalid");
                            return;
                        }
                    }

                    Double price = fee * weight;

                    Stone stone = new Stone();
                    stone.setStoneType(selectedStoneType);
                    stone.setPrice(price);
                    stone.setFee(fee);
                    stone.setWeight(weight);

                    stonesTable.getItems().add(stone);

                    stoneTypeCombobox.getSelectionModel().clearSelection();
                    feeTextInput.clear();
                    priceTextInput.clear();
                    weightTextInput.clear();
                    stoneTypeCombobox.requestFocus();
                }
            });
            grid.add(addButton, 0, 4);
            dialog.setFocusNodes(stoneTypeCombobox, weightTextInput, feeTextInput, addButton);

            dialog.getvBox().getChildren().add(stonesTable);

            Optional result = dialog.showAndWait();

            ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
            if (buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                return stonesTable.getItems();
            } else if (buttonData.equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                return initialStones;
            }
        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }
        return null;
    }

    protected Product showProductAndProductCategoryDialog(Product initialProduct, boolean showManufactured) {
        try {
            PopupDialog dialog = new PopupDialog(500);
            GridPane grid = dialog.getGrid();

            List<ProductCategory> allProductCategories;
            ComboBox<ProductCategory> productCategoryCombobox = new ComboBox<>();
            if (showManufactured) {
                ProductCategory manufacturedCategory = productCategoryService.getProductCategoryByCode(BasicProductCategoryCode.MANUFACTURED.value());
                allProductCategories = new ArrayList<>();
                allProductCategories.add(manufacturedCategory);
            } else {
                allProductCategories = productCategoryService.getAllProductCategories(false);
            }
            productCategoryCombobox.setConverter(new ProductCategoryStringConverter(allProductCategories));
            productCategoryCombobox.setItems(FXCollections.observableArrayList(allProductCategories));
            productCategoryCombobox.setEditable(true);
            new ComboBoxAutoComplete<>(productCategoryCombobox);

            grid.add(new Label(message("product.category")), 0, 0);
            grid.add(productCategoryCombobox, 1, 0);

            ComboBox<Product> productCombobox = new ComboBox<>();
            new ComboBoxAutoComplete<>(productCombobox);
            grid.add(new Label(message("product")), 0, 1);
            grid.add(productCombobox, 1, 1);


            productCategoryCombobox.setOnAction(event -> {
                try {
                    ProductCategory selectedItem = productCategoryCombobox.getSelectionModel().getSelectedItem();
                    List<Product> allProducts;
                    if (showManufactured) {
                        allProducts = productService.getAllManufactured();
                    } else {
                        allProducts = productService.getCategoryProducts(selectedItem);
                    }
                    if (allProducts != null) {
                        productCombobox.setConverter(new ProductStringConverter(allProducts));
                        productCombobox.setItems(FXCollections.observableArrayList(allProducts));
                        productCombobox.setEditable(true);
                        new ComboBoxAutoComplete<>(productCombobox);
                        productCombobox.getSelectionModel().selectFirst();
                    }
                } catch (SQLException e) {
                    FxUtil.exceptionOccurred(e);
                }
            });
            productCategoryCombobox.getSelectionModel().selectFirst();
            productCategoryCombobox.fireEvent(new ActionEvent()); // calling the fireEvent to manually trigger productCategoryCombobox onAction

            if (showManufactured) {
                productCombobox.setEditable(true);
                dialog.setFocusNodes(productCombobox);
            } else {
                dialog.setFocusNodes(productCategoryCombobox, productCombobox);
            }

            Optional result = dialog.showAndWait();
            if (result.isPresent()) {
                ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
                if (buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                    Product selectedProduct = productCombobox.getSelectionModel().getSelectedItem();
                    return selectedProduct;
                }
            }

        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }

        return null;
    }

    protected List<Stock> showStocksTable(List<Stock> initialStocks, String productCategoryCode, boolean isSingleSelect) {
        try {

            Callback<TableColumn.CellDataFeatures<Pair<Stock, SimpleBooleanProperty>, String>, ObservableValue<String>> productTableCellValueFactoryCallback = param -> new ObjectBinding<>() {
                @Override
                protected String computeValue() {
                    if (param != null) {
                        String columnId = param.getTableColumn().getId();
                        Stock stock = param.getValue().getLeft();
                        DecimalFormat df = new DecimalFormat("0");
                        df.setMaximumFractionDigits(5);

                        Product product = stock.getProduct();

                        if (product != null) {
                            final ProductCategory productCategory = product.getProductCategory();
                            switch (columnId) {
                                case "productCategoryCode" -> {
                                    if (productCategory != null) {
                                        return productCategory.getCode();
                                    }
                                }
                                case "productCode" -> {
                                    return product.getCode();
                                }
                                case "productName" -> {
                                    if (productCategory != null) {
                                        return productCategory.getTitle() + " - " + product.getProductName();
                                    }
                                }
                                case "receivedWageAmount" -> {
                                    if (product.getReceivedWageAmount() != null) {
                                        return df.format(product.getReceivedWageAmount());
                                    }
                                }
                                case "payedWageAmount" -> {
                                    if (product.getPayedWageAmount() != null) {
                                        return df.format(product.getPayedWageAmount());
                                    }
                                }
                                case "receivedWagePercentage" -> {
                                    if (product.getReceivedWagePercentage() != null) {
                                        return df.format(product.getReceivedWagePercentage());
                                    }
                                }
                                case "payedWagePercentage" -> {
                                    if (product.getPayedWagePercentage() != null) {
                                        return df.format(product.getPayedWagePercentage());
                                    }
                                }
                                case "stockAmount" -> {
                                    if (productCategory != null) {
                                        if (productCategory.getCountable() && stock.getCount() != null) {
                                            return df.format(stock.getCount());
                                        } else if (stock.getWeight() != null) {
                                            return df.format(stock.getWeight());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return "";
                }
            };


            PopupDialog dialog = new PopupDialog(1100);
            dialog.setIncludeSubmitInFocusNodes(false);

            TableView<Pair<Stock, SimpleBooleanProperty>> stocksTable = new TableView<>();
            stocksTable.setPrefWidth(1100);

            var selectColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, Boolean>(message("select"));
            selectColumn.setId("select");
            selectColumn.setEditable(true);
            Callback<TableColumn<Pair<Stock, SimpleBooleanProperty>, Boolean>, TableCell<Pair<Stock, SimpleBooleanProperty>, Boolean>> booleanCellFactory =
                    p -> {
                        BooleanCell booleanCell = new BooleanCell((BiFunction<TableRow, Boolean, Void>) (tableRow, value) -> {
                            ((Pair<Stock, SimpleBooleanProperty>) tableRow.getItem()).getRight().set(value);
                            return null;
                        });
                        return booleanCell;
                    };
            selectColumn.setCellFactory(booleanCellFactory);
            selectColumn.setCellValueFactory(param -> new ObjectBinding<Boolean>() {
                @Override
                protected Boolean computeValue() {
                    Pair<Stock, SimpleBooleanProperty> paramValue = param.getValue();
                    if (paramValue != null) {
                        return paramValue.getRight().get();
                    }
                    return false;
                }
            });

            var productCategoryCodeColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("product.category.code"));
            productCategoryCodeColumn.setId("productCategoryCode");
            productCategoryCodeColumn.setCellValueFactory(productTableCellValueFactoryCallback);

            var productCodeColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("product.code"));
            productCodeColumn.setId("productCode");
            productCodeColumn.setCellValueFactory(productTableCellValueFactoryCallback);

            var productNameColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("product.name"));
            productNameColumn.setId("productName");
            productNameColumn.setMinWidth(150);
            productNameColumn.setCellValueFactory(productTableCellValueFactoryCallback);

            var receivedWageAmountColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("received.wage"));
            receivedWageAmountColumn.setId("receivedWageAmount");
            receivedWageAmountColumn.setCellValueFactory(productTableCellValueFactoryCallback);

            var payedWageAmountColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("payed.wage"));
            payedWageAmountColumn.setId("payedWageAmount");
            payedWageAmountColumn.setCellValueFactory(productTableCellValueFactoryCallback);

            var receivedWagePercentageColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("received.wage") + "%");
            receivedWagePercentageColumn.setId("receivedWagePercentage");
            receivedWagePercentageColumn.setCellValueFactory(productTableCellValueFactoryCallback);

            var payedWagePercentageColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("payed.wage") + "%");
            payedWagePercentageColumn.setId("payedWagePercentage");
            payedWagePercentageColumn.setCellValueFactory(productTableCellValueFactoryCallback);

            var stockAmountColumn = new TableColumn<Pair<Stock, SimpleBooleanProperty>, String>(message("stock.amount"));
            stockAmountColumn.setId("stockAmount");
            stockAmountColumn.setCellValueFactory(productTableCellValueFactoryCallback);


            stocksTable.getColumns().addAll(selectColumn, productCategoryCodeColumn, productCodeColumn, productNameColumn, receivedWageAmountColumn,
                    payedWageAmountColumn, receivedWagePercentageColumn, payedWagePercentageColumn, stockAmountColumn);
            if (initialStocks != null && stocksTable.getItems() != null) {
//                stocksTable.getItems().addAll(initialStocks);
            }


            stocksTable.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
                if (event.getCode().equals(KeyCode.SPACE)) {
                    Pair<Stock, SimpleBooleanProperty> focusedItem = stocksTable.getFocusModel().getFocusedItem();
                    Boolean isRowSelected = focusedItem.getRight().getValue();

                    if (stocksTable.getItems() != null && !isRowSelected && isSingleSelect) {
                        for (Pair<Stock, SimpleBooleanProperty> item : stocksTable.getItems()) {
                            boolean selected = item.getRight().get();
                            Stock stock = item.getLeft();
                            if (selected && !stock.getId().equals(focusedItem.getLeft().getId())) {
                                FxUtil.warning("cannot.select.multiple.rows");
                                stocksTable.requestFocus();
                                return;
                            }
                        }
                    }

                    focusedItem.getRight().setValue(!isRowSelected);
                    stocksTable.refresh();
                    stocksTable.requestFocus();
                    event.consume();
                }
            });
            stocksTable.setOnKeyPressed(event -> {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    dialog.fireConfirm();
                }
            });

            GridPane grid = dialog.getGrid();

            TextField searchTextInput = new TextField();
            searchTextInput.setPromptText(message("search"));
            grid.add(searchTextInput, 0, 1);
            grid.add(stocksTable, 0, 2);

            dialog.setFocusNodes(searchTextInput);
            FxUtil.autoResizeColumns(stocksTable);

            List<Stock> allStocks;
            if (StringUtils.isEmpty(productCategoryCode)) {
                allStocks = stockService.getOfficeStocks();
            } else {
                if (productCategoryCode.equals(BasicProductCategoryCode.MANUFACTURED.value())) {
                    allStocks = stockService.getOfficeManufacturedAndWightedStocks();
                } else {
                    allStocks = stockService.getStocksByCategoryCode(productCategoryCode);
                }
            }

            List<Pair<Stock, SimpleBooleanProperty>> pairList = new ArrayList<>();
            for (Stock stock : allStocks) {
                pairList.add(Pair.of(stock, new SimpleBooleanProperty(false)));
            }

            FilteredList<Pair<Stock, SimpleBooleanProperty>> filteredStocks = new FilteredList<>(FXCollections.observableArrayList(pairList));

            searchTextInput.textProperty().addListener(new StockFilterChangeListener<Pair<Stock, SimpleBooleanProperty>>(filteredStocks, stocksTable) {
                @Override
                protected Stock getStock(Pair<Stock, SimpleBooleanProperty> pair) {
                    if (pair != null) {
                        return pair.getLeft();
                    }
                    return null;
                }
            });

            stocksTable.getItems().addAll(filteredStocks);

            Optional result = dialog.showAndWait();

            ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
            if (buttonData.equals(ButtonBar.ButtonData.APPLY)) {
                final ObservableList<Pair<Stock, SimpleBooleanProperty>> items = stocksTable.getItems();
                List<Stock> resultStocks = new ArrayList<>();
                for (Pair<Stock, SimpleBooleanProperty> item : items) {
                    if (item.getRight().get()) {
                        resultStocks.add(item.getLeft());
                    }
                }
                return resultStocks;
            } else if (buttonData.equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                return initialStocks;
            }


        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }

        return null;
    }


    /**
     * @return list of controllers that were included in the parent FXML like stock.fxml
     */
    public List<BaseController> getIncludedControllers() {
        return null;
    }

    protected void setShortcuts() {
    }

    protected void reset() {
        initFocus();
    }

    /**
     * called on those table views that height needs to be based on row counts.
     */
    protected void setEmptyEditableTableStyle(TableView tableView) {
        tableView.setEditable(true);
        tableView.getStylesheets().add("/css/tableview-empty.css");
        tableView.setFixedCellSize(25);
        tableView.prefHeightProperty().bind(tableView.fixedCellSizeProperty().multiply(Bindings.size(tableView.getItems()).add(1.01)));
        tableView.minHeightProperty().bind(tableView.prefHeightProperty());
        tableView.setPlaceholder(new Label());
    }

    /**
     * it should be called on a form to make sure of the form input focus order.
     */
    protected void initFocus() {
        formFocusController = new FormFocusController(focusNodes);
    }

    public List<Node> getFocusNodes() {
        return focusNodes;
    }

    public void setFocusNodes(List<Node> focusNodes) {
        this.focusNodes = focusNodes;
    }

    public void setFocusNodes(Node... focusNodes) {
        this.focusNodes = Arrays.asList(focusNodes);
    }

    /**
     * This method is called by event bus to close all open dialogues
     */
    @Subscribe
    public void onCloseCommand(CloseCommandEvent event) {
        if (!(this instanceof MainController) && !(this instanceof LoginController) && stage != null) {
            stage.close();
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * @param viewFileName:      the fxml file name
     * @param setControllerData: function to be called on controller after it is loaded.
     * @return loaded stage
     */
    public Stage showDialog(String viewFileName, double v, double v1, Modality modality,
                            Function<BaseController, Void> setControllerData) {
        final Stage dialog = new Stage();
        dialog.initModality(modality);
        dialog.initOwner(ApplicationContext.getPrimaryStage());
        FXMLLoader loader = ApplicationContext.getFxApp().loader(getClass().getResource("/fxml/" + viewFileName + ".fxml"));

        try {
            VBox vbox = loader.load();
            Object controller = loader.getController();
            if (setControllerData != null) {
                setControllerData.apply((BaseController) controller);
            }
            if (controller != null) {
                ((BaseController) controller).setStage(dialog);
            }
            Scene dialogScene = new Scene(vbox, v, v1);
            dialogScene.setOnKeyReleased(key -> {
                if (key.getCode().equals(KeyCode.ESCAPE)) {
                    dialog.close();
                }
            });
            dialog.setScene(dialogScene);
            dialog.setAlwaysOnTop(false);
            dialog.getScene().getStylesheets().add("/css/popup-dialog.css");
            dialog.show();
            if (controller != null) {
                ((BaseController) controller).postLoad();
                List<BaseController> includedControllers = ((BaseController) controller).getIncludedControllers();
                if (includedControllers != null) {
                    for (BaseController includedController : includedControllers) {
                        includedController.setStage(dialog);
                        includedController.postLoad();
                    }
                }
            }
        } catch (IOException ioException) {
            FxUtil.exceptionOccurred(ioException);
        }
        return dialog;
    }

    public Stage showDialog(String viewFileName, double v, double v1, Modality modality) {
        return showDialog(viewFileName, v, v1, modality, null);
    }

    protected void changeScene(String viewName, Stage stage) {
        FXMLLoader loader = ApplicationContext.getFxApp().loader(getClass().getResource(viewName));
        Parent root = null;
        try {
            root = loader.load();
            BaseController controller = loader.getController();
            controller.setStage(stage);
            if (viewName.equals("/fxml/main.fxml")) {
                ApplicationContext.setMainController((MainController) controller);
            }
            if (root != null) {
                Screen screen = Screen.getPrimary();
                Rectangle2D visualBounds = screen.getVisualBounds();
                Scene scene = new Scene(root, visualBounds.getWidth(), visualBounds.getHeight());
                scene.getStylesheets().add("/css/popup-dialog.css");
                stage.setScene(scene);
                stage.setMaximized(true);
                controller.postLoad();


                scene.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                    if (keyEvent.isControlDown()) {
                        switch (keyEvent.getCode().getChar()) {
                            case "K" -> ApplicationContext.getMainController().showDialog("productCategoryManagement", 650, 540, Modality.NONE);
                            case "I" -> ApplicationContext.getMainController().showDialog("productManagement", 850, 750, Modality.NONE);
                            case "U" -> ApplicationContext.getMainController().showDialog("userManagement", 700, 600, Modality.NONE);
                            case "H" -> ApplicationContext.getMainController().showDialog("partyManagement", 950, 750, Modality.NONE);
                            case "S" -> ApplicationContext.getMainController().showDialog("stock", visualBounds.getWidth() * 0.83, visualBounds.getHeight() * 0.92, Modality.NONE);
                            case "F" -> {
                                Stage invoiceDialog = ApplicationContext.getMainController().showDialog("invoice", visualBounds.getWidth(), visualBounds.getHeight(), Modality.NONE);
                                invoiceDialog.setMaximized(true);
                            }
                            case "1" -> {
                                ApplicationContext.getMainController().showDialog("partyAccountBalanceReport", visualBounds.getWidth() * 0.80, visualBounds.getHeight() * 0.92, Modality.NONE);
                            }
                        }
                    }
                });

            } else {
                log.error("Root element is null on setting: " + viewName);
            }
        } catch (IOException e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    protected String message(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * validation method for input of type double
     */
    protected ValidationResult validateDouble(Control control, String input) {
        boolean validationFailed = false;
        try {
            if (!StringUtils.isEmpty(input)) {
                Double.parseDouble(input);
            }
        } catch (NumberFormatException e) {
            validationFailed = true;
        }
        isFormDataValid = !validationFailed;
        return ValidationResult.fromMessageIf(control, message("must.be.integer"), Severity.ERROR, validationFailed);
    }

    /**
     * validation method for input of type integer
     */
    protected ValidationResult validateInteger(Control control, String input) {
        boolean validationFailed = false;
        try {
            if (!StringUtils.isEmpty(input)) {
                Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            validationFailed = true;
        }
        isFormDataValid = !validationFailed;
        return ValidationResult.fromMessageIf(control, message("must.be.integer"), Severity.ERROR, validationFailed);
    }

    /**
     * validation method for percentage input
     */
    protected ValidationResult validatePercentage(Control control, String input) {
        boolean validationFailed = false;
        if (StringUtils.isNotEmpty(input)) {
            double parsedValue = 0;
            try {
                parsedValue = Double.parseDouble(input);
            } catch (NumberFormatException e) {
                validationFailed = true;
            }
            if (parsedValue > 100 || parsedValue < 0) {
                validationFailed = true;
            }
        }
        isFormDataValid = !validationFailed;
        return ValidationResult.fromMessageIf(control, message("percentage.value.invalid"), Severity.ERROR, validationFailed);
    }

    /**
     * validation method for mobile number input
     */
    protected ValidationResult validateMobileNumber(Control control, String input) {
        boolean validationFailed = false;
        if (!StringUtils.isEmpty(input)) {
            if (!input.matches("\\d+") || input.length() != 11) {
                validationFailed = true;
            }
        }
        isFormDataValid = !validationFailed;
        return ValidationResult.fromMessageIf(control, message("mobile.invalid"), Severity.ERROR, validationFailed);
    }

    /**
     * validation method for fixed phone number input
     */
    protected ValidationResult validatePhoneNumber(Control control, String input) {
        boolean validationFailed = false;
        if (!StringUtils.isEmpty(input)) {
            if (!input.matches("\\d+")) {
                validationFailed = true;
            }
        }
        isFormDataValid = !validationFailed;
        return ValidationResult.fromMessageIf(control, message("phone.invalid"), Severity.ERROR, validationFailed);

    }

}
