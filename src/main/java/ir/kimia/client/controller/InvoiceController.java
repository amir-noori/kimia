package ir.kimia.client.controller;


import ir.kimia.client.common.*;
import ir.kimia.client.controller.model.InvoicePrintData;
import ir.kimia.client.data.model.*;
import ir.kimia.client.data.model.InvoiceRecord.DealType;
import ir.kimia.client.data.model.InvoiceRecord.InvoiceRecordType;
import ir.kimia.client.exception.InvoiceValidationException;
import ir.kimia.client.service.api.*;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.CellType;
import ir.kimia.client.ui.ComboBoxAutoComplete;
import ir.kimia.client.ui.EditCell;
import ir.kimia.client.ui.PartyStringConverter;
import ir.kimia.client.util.DateUtil;
import ir.kimia.client.util.FxUtil;
import ir.kimia.client.util.MathUtil;
import ir.kimia.client.util.PrintUtil;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static ir.kimia.client.common.Constants.THOUSAND_SEPARATOR;

/**
 * @author Amir
 */
@FxAppScoped
public class InvoiceController extends BaseController {

    private static final Logger log = LogManager.getLogger(InvoiceController.class);

    private static DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();


    @FXML
    private TextField regardPercentInput;
    @FXML
    private TextField invoiceStonesInput;
    @FXML
    private TextField differenceInput;
    @FXML
    private TextField discountInput;
    @FXML
    private TextField documentNumberInput;
    @FXML
    private TextField invoiceNumberInput;
    @FXML
    private TextField invoiceDateInput;
    @FXML
    private TextField partyCodeInput;
    @FXML
    private ComboBox<Party> partyNameComboBox;
    @FXML
    private TableView<InvoiceRecord> invoiceTable;
    @FXML
    private TableView<AccountBalance> previousBalanceTable;
    @FXML
    private TableView<AccountBalance> invoiceBalanceTable;
    @FXML
    private TableView<AccountBalance> finalBalanceTable;
    @FXML
    private TableView<String> totalTradesTable;
    @FXML
    private TableView<InvoiceRecordType> shortcutHelpTable;
    @FXML
    private Button newInvoiceBtn;
    @FXML
    private Button printInvoiceBtn;
    @FXML
    private Button recordInvoiceBtn;
    @FXML
    private Button temporaryRecordInvoiceBtn;
    @FXML
    private Button removeInvoiceBtn;


    private static class COLUMN_INDEX {
        public static int groupAndProductCode = 0;
        public static int productDescription = 1;
        public static int dealType = 2;
        public static int count = 3;
        public static int weightByScale = 4;
        public static int carat = 5;
        public static int weightBasedOnCarat = 6;
        public static int wagePercentage = 7;
        public static int weightByPercentage = 8;
        public static int quote = 9;
        public static int price = 10;
        public static int fee = 11;
        public static int totalFee = 12;
        public static int officeBenefit = 13;
        public static int benefitAmount = 14;
        public static int tax = 15;
        public static int taxAmount = 16;
        public static int totalAmount = 17;

    }

    private final InvoiceService invoiceService;
    private final InvoiceRecordService invoiceRecordService;
    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private final PartyService partyService;
    private final AccountBalanceService accountBalanceService;
    private final ReportService reportService;

    private static Double MSC_CARAT = 740.0;

    private InvoiceRecordType defaultInvoiceRecordType;


    private Double computeTotalFee(InvoiceRecord invoiceRecord) {
        Product product = invoiceRecord.getProduct();
        if (product != null) {
            Boolean isCountable = product.getProductCategory().getCountable();
            if (isCountable) {
                Integer count = invoiceRecord.getCount();
                Double fee = invoiceRecord.getFee();
                if (count != null && fee != null) {
                    return count * fee;
                }
            } else {
                Double weightBasedOnCarat = getWeightBasedOnCarat(invoiceRecord);
                Double fee = invoiceRecord.getFee();
                if (fee != null && weightBasedOnCarat != null) {
                    return fee * weightBasedOnCarat;
                }
            }
        }
        return null;
    }

    private Double computePrice(InvoiceRecord invoiceRecord) {
        Double quote = invoiceRecord.getQuote();
        Double weightBasedOnCarat = getWeightBasedOnCarat(invoiceRecord);
        if (quote != null && weightBasedOnCarat != null) {
            // TODO: what is 705?
            return (quote / (Constants.ONE_MITHQAL * (705 / Constants.GOLD_QUOTE))) * weightBasedOnCarat;
        }

        return null;
    }

    private Double computeTotalAmount(InvoiceRecord invoiceRecord) {
        switch (invoiceRecord.getInvoiceRecordType()) {
            case PAYMENT_ORDER_CASH, CASH, BANK, CHEQUE, MSC -> {
                if (invoiceRecord.getAmount() != null) {
                    return invoiceRecord.getAmount();
                }
            }
        }

        // other cases
        Double price = computePrice(invoiceRecord);
        price = price != null ? price : 0.0;
        Double totalFee = computeTotalFee(invoiceRecord);
        totalFee = totalFee != null ? totalFee : 0.0;
        Double officeBenefit = invoiceRecord.getBenefit();
        officeBenefit = officeBenefit != null ? officeBenefit : 0.0;
        Double tax = invoiceRecord.getTax();
        tax = tax != null ? tax : 0.0;

        return price + totalFee + officeBenefit + tax;
    }

    protected EventHandler<TableColumn.CellEditEvent<InvoiceRecord, String>> onInvoiceTableCellCommitHandler(String cellName) {
        return new EventHandler<>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Object newValue = event.getNewValue();
                Object oldValue = event.getOldValue();
                final ObservableList items = event.getTableView().getItems();
                final int row = event.getTablePosition().getRow();
                if (items != null && items.size() > 0 && row < items.size()) {
                    InvoiceRecord invoiceRecord = (InvoiceRecord) items.get(row);
                    switch (cellName) {
                        case "groupAndProductCode" -> {
                            // TODO
                        }
                        case "productDescription" -> {
                            // TODO
                        }
                        case "dealType" -> {
                            // TODO
                        }
                        case "count" -> {
                            // TODO
                        }
                        case "weightByScale" -> {
                            // TODO
                        }
                        case "carat" -> {
                            // TODO
                        }
                        case "weightBasedOnCarat" -> {
                            // TODO
                        }
                        case "wagePercentage" -> {
                            // TODO
                        }
                        case "weightByPercentage" -> {
                            // TODO
                        }
                        case "quote" -> {
                            // TODO
                        }
                        case "price" -> {
                            // TODO
                        }
                        case "fee" -> {
                            // TODO
                        }
                        case "totalFee" -> {
                            // TODO
                        }
                        case "officeBenefit" -> {
                            // TODO
                        }
                        case "tax" -> {
                            // TODO
                        }
                        case "totalAmount" -> {
                            // TODO
                        }

                    }
                }
            }
        };
    }

    private Callback<TableColumn<InvoiceRecord, String>, TableCell<InvoiceRecord, String>> getInvoiceTableCellFactory(String cellName) {

        CellType cellType;
        switch (cellName) {
            case "wagePercentage", "quote", "price", "fee", "totalFee", "officeBenefit", "tax", "totalAmount" -> {
                cellType = CellType.THOUSAND_SEPARATED_NUMBER;
            }
            default -> {
                cellType = CellType.TEXT;
            }
        }

        return EditCell.forTableColumn(new StringConverter<>() {

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
        }, afterCellCommit(), beforeCellCommit(), getEditableExtractor(cellName), getCellEditEventHandler(cellName), cellType);
    }

    private void setupStoneForInvoiceRecord(InvoiceRecord invoiceRecord, String inputText, KeyEvent keyEvent) {
        inputText = inputText.replaceAll(ShortCuts.SHOW_STONE_DIALOG, "");
        try {
            ((TextField) keyEvent.getSource()).setText(inputText);
            Collection<Stone> stoneCollection = invoiceRecord.getStones();
            List<Stone> selectedStones;
            if (stoneCollection != null) {
                selectedStones = showStoneDialog(new ArrayList<>(stoneCollection));
            } else {
                selectedStones = showStoneDialog(new ArrayList<>());
            }
            invoiceRecord.setStones(selectedStones);
        } catch (NumberFormatException ignored) {
        }

        keyEvent.consume();
    }

    private EventHandler<KeyEvent> getCellEditEventHandler(String cellName) {
        return keyEvent -> {

            String text = keyEvent.getText();
            final boolean altDown = keyEvent.isAltDown();
            final KeyCode keyCode = keyEvent.getCode();

            if (altDown && keyCode.equals(KeyCode.ENTER)) {
                // @TODO: backward editing does not work, maybe it is EditCell fault.
                final int focusedRow = invoiceTable.getFocusModel().getFocusedIndex();
                final int focusColumn = invoiceTable.getFocusModel().getFocusedCell().getColumn();
                final TableColumn<InvoiceRecord, ?> invoiceRecordTableColumn = invoiceTable.getColumns().get(focusColumn - 1);
                focusOn(focusedRow, invoiceRecordTableColumn, true);
                return;
            }

            InvoiceRecord invoiceRecord = invoiceTable.getFocusModel().getFocusedItem();

            String inputText = "";
            if (keyEvent.getSource() != null) {
                inputText = ((TextField) keyEvent.getSource()).getText();
            }


            if (StringUtils.isNotEmpty(text)) {
                switch (cellName) {
                    case "groupAndProductCode" -> {
                        // TODO
                    }
                    case "productDescription" -> {
                        // TODO
                    }
                    case "dealType" -> {
                        // TODO
                    }
                    case "count" -> {
                        final InvoiceRecordType invoiceRecordType = invoiceRecord.getInvoiceRecordType();
                        if (text.toLowerCase().equals(ShortCuts.SHOW_STONE_DIALOG) && invoiceRecordType != null && invoiceRecordType.equals(InvoiceRecordType.STONE)) {
                            setupStoneForInvoiceRecord(invoiceRecord, inputText, keyEvent);
                        }
                    }
                    case "weightByScale" -> {
                        final InvoiceRecordType invoiceRecordType = invoiceRecord.getInvoiceRecordType();
                        if (text.toLowerCase().equals(ShortCuts.SHOW_STONE_DIALOG) && invoiceRecordType != null && invoiceRecordType.equals(InvoiceRecordType.MANUFACTURED)) {
                            setupStoneForInvoiceRecord(invoiceRecord, inputText, keyEvent);
                        }
                    }
                    case "carat" -> {
                        // TODO
                    }
                    case "weightBasedOnCarat" -> {
                        // TODO
                    }
                    case "wagePercentage" -> {
                        // TODO
                    }
                    case "weightByPercentage" -> {
                        // TODO
                    }
                    case "quote" -> {
                        if (text.toLowerCase().equals(ShortCuts.GRAM_TO_MITHQAL)) {
                            inputText = inputText.replaceAll(ShortCuts.GRAM_TO_MITHQAL, "");
                            try {
                                Double quote = Double.parseDouble(inputText);
                                quote = quote * 705 * Constants.ONE_MITHQAL / Constants.GOLD_QUOTE;
                                ((TextField) keyEvent.getSource()).setText(decimalFormat.format(quote));

                            } catch (NumberFormatException ignored) {
                            }

                            keyEvent.consume();
                        }
                    }
                    case "price" -> {
                        // TODO
                    }
                    case "fee" -> {
                        // TODO
                    }
                    case "totalFee" -> {
                        // TODO
                    }
                    case "officeBenefit" -> {
                        // TODO
                    }
                    case "tax" -> {
                        // TODO
                    }
                    case "totalAmount" -> {
                        // TODO
                    }
                }
            }
        };
    }

    private Callback<TableColumn.CellDataFeatures<InvoiceRecord, String>, ObservableValue<String>> getInvoiceCellValueFactory(String cellName) {
        return param -> {
            InvoiceRecord invoiceRecord = param.getValue();
            Product recordProduct = invoiceRecord.getProduct();
            InvoiceRecord.InvoiceRecordType invoiceRecordType = invoiceRecord.getInvoiceRecordType();

            return new ObjectBinding<>() {
                @Override
                protected String computeValue() {

                    boolean isProduct = false;
                    if (invoiceRecordType != null &&
                            (
                                    invoiceRecordType.equals(InvoiceRecordType.PRODUCT) || invoiceRecordType.equals(InvoiceRecordType.COIN) ||
                                            invoiceRecordType.equals(InvoiceRecordType.CURRENCY) || invoiceRecordType.equals(InvoiceRecordType.MANUFACTURED)
                            )
                    ) {
                        isProduct = true;
                    }

                    switch (cellName) {
                        case "groupAndProductCode" -> {
                            if (recordProduct != null && isProduct) {
                                ProductCategory productCategory = recordProduct.getProductCategory();
                                if (productCategory != null) {
                                    String productCode = recordProduct.getCode();
                                    if (StringUtils.isNotEmpty(productCode)) {
                                        return productCategory.getCode() + "-" + productCode;
                                    } else {
                                        return productCategory.getCode() + "-";
                                    }
                                }
                            } else if (invoiceRecordType != null && (invoiceRecordType.equals(InvoiceRecordType.GOLD_TO_CASH_CONVERSION) || invoiceRecordType.equals(InvoiceRecordType.CASH_TO_GOLD_CONVERSION))) {
                                return ShortCuts.GOLD_AND_CASH_CONVERSION.toUpperCase();
                            } else {
                                if (invoiceRecordType == InvoiceRecordType.UNDEFINED) {
                                    return "";
                                } else {
                                    return String.valueOf(invoiceRecordType.value());
                                }
                            }
                        }
                        case "productDescription" -> {
                            if (recordProduct != null && isProduct) {
                                final ProductCategory productCategory = recordProduct.getProductCategory();
                                String productCategoryTitle = "";
                                String productName = "?";
                                if (productCategory != null && productCategory.getTitle() != null) {
                                    productCategoryTitle = productCategory.getTitle();
                                }
                                if (recordProduct.getProductName() != null) {
                                    productName = recordProduct.getProductName();
                                }
                                return productCategoryTitle + " - " + productName;
                            } else if (invoiceRecordType != null) {
                                String productName = "";
                                if (recordProduct != null) {
                                    productName = recordProduct.getProductName();
                                }
                                switch (invoiceRecordType) {
                                    case BANK -> {
                                        return message("bank");
                                    }
                                    case CHEQUE -> {
                                        return message("cheque");
                                    }
                                    case PAYMENT_ORDER_CASH -> {
                                        return message("payment.order.cash") + " " + productName;
                                    }
                                    case PAYMENT_ORDER_CURRENCY -> {
                                        return message("payment.order.currency") + " " + productName;
                                    }
                                    case PAYMENT_ORDER_GOLD -> {
                                        return message("payment.order.gold") + " " + productName;
                                    }
                                    case CASH_TO_GOLD_CONVERSION -> {
                                        return message("cash_to_gold_conversion");
                                    }
                                    case GOLD_TO_CASH_CONVERSION -> {
                                        return message("gold_to_cash_conversion");
                                    }
                                    case STONE -> {
                                        return message("stone");
                                    }
                                }

                                return productName;
                            }
                        }
                        case "dealType" -> {
                            Integer dealType = invoiceRecord.getDealType();
                            if (dealType != null) {
                                return message("deal.type." + InvoiceRecord.DealType.getByValue(dealType).name().toLowerCase());
                            }
                        }
                        case "count" -> {
                            Integer count = invoiceRecord.getCount();
                            return count != null ? String.valueOf(count) : "";
                        }
                        case "weightByScale" -> {
                            Double weightByScale = invoiceRecord.getWeightByScale();
                            return weightByScale != null ? decimalFormat.format(weightByScale) : "";
                        }
                        case "carat" -> {
                            if (recordProduct != null && recordProduct.getProductCategory().getCountable()) {
                                return "";
                            }
                            Double carat = invoiceRecord.getCarat();
                            if (carat != null) {
                                return String.valueOf(carat);
                            } else {
                                if (recordProduct != null) {
                                    final ProductCategory productCategory = recordProduct.getProductCategory();
                                    final String categoryCode = productCategory.getCode();
                                    final Boolean countable = productCategory.getCountable();
                                    if (categoryCode.equals(BasicProductCategoryCode.MSC.value())) {
                                        return String.valueOf(MSC_CARAT);
                                    } else if (categoryCode.equals(BasicProductCategoryCode.MANUFACTURED.value()) ||
                                            (countable != null && !countable && (!categoryCode.equals(BasicProductCategoryCode.STONE.value())))) {
                                        return decimalFormat.format(recordProduct.getCarat());
                                    }
                                } else {
                                    return String.valueOf(ApplicationContext.getOfficeCarat());
                                }
                            }
                        }
                        case "weightBasedOnCarat" -> {
                            Double weightByScale = invoiceRecord.getWeightByScale();
                            Double carat = invoiceRecord.getCarat();
                            if (weightByScale != null && carat != null && carat > 0.0) {
                                return decimalFormat.format(weightByScale * carat / Constants.GOLD_QUOTE);
                            }
                        }
                        case "wagePercentage" -> {
                            Double wagePercentage = invoiceRecord.getWagePercentage();
                            if (wagePercentage != null) {
                                return decimalFormat.format(wagePercentage);
                            }
                        }
                        case "weightByPercentage" -> {
                            Double weightByScale = invoiceRecord.getWeightByScale();
                            Double wagePercentage = invoiceRecord.getWagePercentage();

                            if (weightByScale != null && wagePercentage != null) {
                                return decimalFormat.format(weightByScale * wagePercentage / 100);
                            }
                        }
                        case "quote" -> {
                            Double quote = invoiceRecord.getQuote();
                            if (quote != null) {
                                return decimalFormat.format(quote);
                            }
                        }
                        case "price" -> {
                            Double price = computePrice(invoiceRecord);
                            if (price != null) {
                                return decimalFormat.format(price);
                            }
                        }
                        case "fee" -> {
                            Double fee = invoiceRecord.getFee();
                            if (fee != null) {
                                return decimalFormat.format(fee);
                            }
                        }
                        case "totalFee" -> {
                            Double totalFee = computeTotalFee(invoiceRecord);
                            if (totalFee != null) {
                                return decimalFormat.format(totalFee);
                            }
                        }
                        case "officeBenefit" -> {
                            Double benefit = invoiceRecord.getBenefit();
                            if (benefit != null) {
                                return decimalFormat.format(benefit);
                            }
                        }
                        case "benefitAmount" -> {
                            Double price = computePrice(invoiceRecord);
                            Double totalFee = computeTotalFee(invoiceRecord);
                            Double officeBenefit = invoiceRecord.getBenefit();
                            if (price != null && totalFee != null && officeBenefit != null && officeBenefit != 0) {
                                return decimalFormat.format((price + totalFee) * officeBenefit / 100);
                            }
                        }
                        case "tax" -> {
                            Double tax = invoiceRecord.getTax();
                            if (tax != null) {
                                return decimalFormat.format(tax);
                            }
                        }
                        case "taxAmount" -> {
                            Double price = computePrice(invoiceRecord);
                            Double totalFee = computeTotalFee(invoiceRecord);
                            Double officeBenefit = invoiceRecord.getBenefit();
                            Double tax = invoiceRecord.getTax();
                            if (price != null && totalFee != null && officeBenefit != null && tax != null && tax != 0) {
                                return decimalFormat.format((price * totalFee * officeBenefit) * tax / 100);
                            }

                        }
                        case "totalAmount" -> {
                            return decimalFormat.format(computeTotalAmount(invoiceRecord));
                        }
                    }
                    return "";
                }
            };
        };
    }

    private Function<InvoiceRecord, ObservableValue<Boolean>> getEditableExtractor(String cellName) {
        return invoiceRecord -> new ObjectBinding<>() {
            @Override
            protected Boolean computeValue() {

                if (invoiceRecord != null) {
                    InvoiceRecordType invoiceRecordType = invoiceRecord.getInvoiceRecordType();
                    Integer dealTypeValue = invoiceRecord.getDealType();
                    DealType dealType = null;
                    boolean isPaymentOrReceived = false;
                    if (dealTypeValue != null) {
                        isPaymentOrReceived = dealTypeValue.equals(DealType.PAYMENT.value()) || dealTypeValue.equals(DealType.RECEIVE.value()) ||
                                dealTypeValue.equals(DealType.PAYMENT_RETURNED.value()) || dealTypeValue.equals(DealType.RECEIVE_RETURNED.value());
                        dealType = InvoiceRecord.DealType.getByValue(dealTypeValue);
                    }

                    Product product = invoiceRecord.getProduct();
                    ProductCategory productCategory;
                    String productCategoryCode = "";
                    if (product != null) {
                        productCategory = product.getProductCategory();
                        productCategoryCode = productCategory.getCode();
                    }

                    switch (cellName) {
                        case "groupAndProductCode" -> {
                            return true;
                        }
                        case "productDescription" -> {
                            return false; // no need to edit, must be filled automatically.
                        }
                        case "dealType" -> {
                            return true;
                        }
                        case "count" -> {
                            switch (invoiceRecordType) {
                                case MELTED, CASH, BANK, CHEQUE, MSC -> {
                                    return false;
                                }
                            }
                        }
                        case "weightByScale" -> {
                            if (product != null && product.getProductCategory().getCountable()) {
                                return false;
                            }
                            if (invoiceRecordType != null) {
                                switch (invoiceRecordType) {
                                    case CASH, BANK, CHEQUE, COIN, STONE -> {
                                        return false;
                                    }
                                    case PRODUCT -> {
                                        if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value()) ||
                                                productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                                            return false;
                                        }
                                    }
                                    case CASH_TO_GOLD_CONVERSION, GOLD_TO_CASH_CONVERSION -> {
                                        return true;
                                    }
                                }
                            }
                        }
                        case "carat" -> {
                            if (invoiceRecordType != null) {
                                switch (invoiceRecordType) {
                                    case MANUFACTURED, CASH, BANK, CHEQUE, COIN, CURRENCY, CASH_TO_GOLD_CONVERSION, GOLD_TO_CASH_CONVERSION, STONE -> {
                                        return false;
                                    }
                                    case PRODUCT -> {
                                        if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value()) ||
                                                productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                                            return false;
                                        }
                                    }
                                }
                            }
                            if (product != null && product.getProductCategory().getCountable()) {
                                return false;
                            }
                        }
                        case "weightBasedOnCarat" -> {
                            if (invoiceRecordType != null) {
                                switch (invoiceRecordType) {
                                    case MELTED, CASH, BANK, CHEQUE, COIN, CURRENCY, MSC, CASH_TO_GOLD_CONVERSION, GOLD_TO_CASH_CONVERSION, STONE -> {
                                        return false;
                                    }
                                    case PRODUCT -> {
                                        if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value()) ||
                                                productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                                            return false;
                                        }
                                    }
                                }
                            }
                            if (product != null && product.getProductCategory().getCountable()) {
                                return false;
                            }
                            if (dealType != null) {
                                if (product != null && !product.getProductCategory().getCountable() &&
                                        ((dealType.equals(DealType.SELL) || dealType.equals(DealType.PURCHASE)))) {
                                    return false;
                                }
                            }
                        }
                        case "wagePercentage" -> {
                            if (invoiceRecordType != null) {
                                switch (invoiceRecordType) {
                                    case MELTED, CASH, BANK, CHEQUE, MSC, COIN, CURRENCY, CASH_TO_GOLD_CONVERSION, GOLD_TO_CASH_CONVERSION, STONE -> {
                                        return false;
                                    }
                                    case PRODUCT -> {
                                        if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value()) ||
                                                productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        case "weightByPercentage" -> {
                            // this column is computed automatically and should not be edited.
                            return false;
                        }
                        case "quote" -> {
                            if (invoiceRecordType != null) {
                                switch (invoiceRecordType) {
                                    case MELTED -> {
                                        if (dealTypeValue != null && (
                                                dealTypeValue.equals(DealType.SELL.value()) ||
                                                        dealTypeValue.equals(DealType.PURCHASE.value()) ||
                                                        dealTypeValue.equals(DealType.SELL_BY_CASH.value()) ||
                                                        dealTypeValue.equals(DealType.PURCHASE_BY_CASH.value()))) {
                                            return true;
                                        }
                                        return false;
                                    }
                                    case MSC -> {
                                        if (dealType != null) {
                                            if (dealType.equals(DealType.PAYMENT) || dealType.equals(DealType.RECEIVE)) {
                                                return false;
                                            }
                                        }
                                    }
                                    case CASH, BANK, CHEQUE, COIN, CURRENCY, STONE -> {
                                        return false;
                                    }
                                    case PRODUCT -> {
                                        if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value()) ||
                                                productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                                            return false;
                                        }
                                    }
                                    case CASH_TO_GOLD_CONVERSION, GOLD_TO_CASH_CONVERSION -> {
                                        return true;
                                    }
                                }
                            }
                            if (isPaymentOrReceived) {
                                return false;
                            }
                        }
                        case "price" -> {
                            if (invoiceRecordType != null) {
                                switch (invoiceRecordType) {
                                    case MELTED, CASH, BANK, CHEQUE, CURRENCY, COIN, CASH_TO_GOLD_CONVERSION, GOLD_TO_CASH_CONVERSION, STONE -> {
                                        return false;
                                    }
                                    case MANUFACTURED -> {
                                        if (isPaymentOrReceived) {
                                            return false;
                                        }
                                    }
                                    case PRODUCT -> {
                                        if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value()) ||
                                                productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                                            return false;
                                        }
                                    }
                                    case MSC -> {
                                        if (dealType != null) {
                                            if (dealType.equals(DealType.PAYMENT) || dealType.equals(DealType.RECEIVE)) {
                                                return false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        case "fee" -> {
                            if (invoiceRecordType != null) {
                                switch (invoiceRecordType) {
                                    case COIN, CURRENCY -> {
                                        return true;
                                    }
                                    case MELTED, CASH, BANK, CHEQUE, MSC, CASH_TO_GOLD_CONVERSION, GOLD_TO_CASH_CONVERSION, STONE -> {
                                        return false;
                                    }
                                    case PRODUCT -> {
                                        if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value()) ||
                                                productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                        case "totalFee" -> {
                            return false;
                        }
                        case "officeBenefit" -> {
                            if (invoiceRecordType != null) {
                                switch (invoiceRecordType) {
                                    case MELTED, CASH, BANK, CHEQUE, MSC, COIN, CURRENCY, CASH_TO_GOLD_CONVERSION, GOLD_TO_CASH_CONVERSION, STONE -> {
                                        return false;
                                    }
                                    case MANUFACTURED -> {
                                        if (isPaymentOrReceived) {
                                            return false;
                                        }
                                    }
                                    case PRODUCT -> {
                                        if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value()) ||
                                                productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        case "benefitAmount" -> {
                            return false;
                        }
                        case "tax" -> {
                            if (invoiceRecordType != null) {

                                switch (invoiceRecordType) {
                                    case MELTED, CASH, BANK, CHEQUE, MSC, COIN, CURRENCY, CASH_TO_GOLD_CONVERSION, GOLD_TO_CASH_CONVERSION, STONE -> {
                                        return false;
                                    }
                                    case MANUFACTURED -> {
                                        if (isPaymentOrReceived) {
                                            return false;
                                        }
                                    }
                                    case PRODUCT -> {
                                        if (productCategoryCode.equals(BasicProductCategoryCode.CURRENCY.value()) ||
                                                productCategoryCode.equals(BasicProductCategoryCode.COIN.value())) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        case "taxAmount" -> {
                            return false;
                        }
                        case "totalAmount" -> {
                            if (invoiceRecordType != null) {
                                switch (invoiceRecordType) {
                                    case STONE -> {
                                        return true;
                                    }
                                    case MSC -> {
                                        if (dealType != null) {
                                            if (dealType.equals(DealType.PAYMENT) || dealType.equals(DealType.RECEIVE)) {
                                                return false;
                                            }
                                        }
                                    }
                                    case CASH, PAYMENT_ORDER_CASH, BANK, CHEQUE -> {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    }
                }

                return true;
            }
        };
    }

    private BiFunction<Object, EditCell, Void> beforeCellCommit() {
        return (object, actualCell) -> {
            TextField editingTextField = null;
            String textInputData = null;
            if (object != null) {
                editingTextField = (TextField) object;
                textInputData = editingTextField.getText();
                editingTextField.requestFocus();
            }
            TablePosition<InvoiceRecord, ?> focusedCell = invoiceTable.getFocusModel().getFocusedCell();
            InvoiceRecord invoiceRecord = invoiceTable.getFocusModel().getFocusedItem();

            if (focusedCell != null && focusedCell.getTableColumn() != null) {

                String columnId = focusedCell.getTableColumn().getId();

                switch (columnId) {
                    case "groupAndProductCode" -> {
                        // TODO

                    }
                    case "productDescription" -> {
                        // TODO
                    }
                    case "dealType" -> {
                        DealType dealType = showDealTypeDialog(invoiceRecord);
                        if (dealType != null) {
                            invoiceRecord.setDealType(dealType.value());
                            actualCell.commitEdit(message("deal.type." + dealType.name().toLowerCase()));
                        }
                    }
                    case "count" -> {
                        // TODO
                    }
                    case "weightByScale" -> {
                        // TODO
                    }
                    case "carat" -> {
                        // TODO
                    }
                    case "weightBasedOnCarat" -> {
                        // TODO
                    }
                    case "wagePercentage" -> {
                        // TODO
                    }
                    case "weightByPercentage" -> {
                        // TODO
                    }
                    case "quote" -> {
                        // TODO
                    }
                    case "price" -> {
                        // TODO
                    }
                    case "fee" -> {
                        // TODO
                    }
                    case "totalFee" -> {
                        // TODO
                    }
                    case "officeBenefit" -> {
                        // TODO
                    }
                    case "tax" -> {
                        // TODO
                    }
                    case "totalAmount" -> {
                        // TODO
                    }

                }
            }

            return null;
        };
    }

    private BiFunction<Object, EditCell, Void> afterCellCommit() {
        return (object, actualCell) -> {
            try {
                TextField editingTextField = null;
                String textInputData = null;
                if (object != null) {
                    editingTextField = (TextField) object;
                    textInputData = editingTextField.getText();
                    textInputData = textInputData.replaceAll(THOUSAND_SEPARATOR, "");
                }
                TablePosition<InvoiceRecord, ?> focusedCell = invoiceTable.getFocusModel().getFocusedCell();
                ObservableList<TableColumn<InvoiceRecord, ?>> columns = invoiceTable.getColumns();
                int focusedCellRow = focusedCell.getRow();
                int focusedCellColumn = focusedCell.getColumn();
                TableColumn<InvoiceRecord, ?> nextColumn = getNextColumn(columns, focusedCellColumn);
                boolean shouldFocusOnCell = false;
                InvoiceRecord invoiceRecord = invoiceTable.getFocusModel().getFocusedItem();
                InvoiceRecordType invoiceRecordType = invoiceRecord.getInvoiceRecordType();

                if (focusedCell != null && focusedCell.getTableColumn() != null) {

                    String columnId = focusedCell.getTableColumn().getId();

                    switch (columnId) {
                        case "groupAndProductCode" -> {
                            Product product = null;
                            if (StringUtils.isEmpty(textInputData)) {
//                                product = showProductAndProductCategoryDialog(null, false);
                                final List<Stock> stocks = showStocksTable(null, null, true);
                                if (stocks != null && stocks.size() > 0) {
                                    final Stock stock = stocks.get(0);
                                    product = stock.getProduct();
                                }
                                invoiceRecord.setProduct(product);
                                invoiceRecord.setInvoiceRecordType(InvoiceRecord.InvoiceRecordType.PRODUCT);
                            } else {
                                if (!textInputData.contains("-")) {
                                    if (textInputData.toLowerCase().equals(ShortCuts.GOLD_AND_CASH_CONVERSION)) {
                                        Boolean goldToCash = false;
                                        if (invoiceRecordType.equals(InvoiceRecordType.GOLD_TO_CASH_CONVERSION)) {
                                            goldToCash = showGoldAndCashConversionDialog(true);
                                        } else {
                                            goldToCash = showGoldAndCashConversionDialog(false);
                                        }
                                        if (goldToCash) {
                                            invoiceRecord.setInvoiceRecordType(InvoiceRecordType.GOLD_TO_CASH_CONVERSION);
                                        } else {
                                            invoiceRecord.setInvoiceRecordType(InvoiceRecordType.CASH_TO_GOLD_CONVERSION);
                                        }
                                    } else {
                                        invoiceRecordType = InvoiceRecordType.getByValue(Integer.parseInt(textInputData));
                                        if (invoiceRecordType != null) {

                                            switch (invoiceRecordType) {
                                                case COIN, CURRENCY, STONE, MANUFACTURED, MSC, MELTED -> {
                                                    String productCategoryCode = null;
                                                    invoiceRecord.setInvoiceRecordType(invoiceRecordType);
                                                    if (invoiceRecordType.equals(InvoiceRecordType.COIN)) {
                                                        productCategoryCode = BasicProductCategoryCode.COIN.value();
                                                    } else if (invoiceRecordType.equals(InvoiceRecordType.CURRENCY)) {
                                                        productCategoryCode = BasicProductCategoryCode.CURRENCY.value();
                                                    } else if (invoiceRecordType.equals(InvoiceRecordType.STONE)) {
                                                        productCategoryCode = BasicProductCategoryCode.STONE.value();
                                                    } else if (invoiceRecordType.equals(InvoiceRecordType.MANUFACTURED)) {
                                                        productCategoryCode = BasicProductCategoryCode.MANUFACTURED.value();
                                                    } else if (invoiceRecordType.equals(InvoiceRecordType.MSC)) {
                                                        productCategoryCode = BasicProductCategoryCode.MSC.value();
                                                    } else if (invoiceRecordType.equals(InvoiceRecordType.MELTED)) {
                                                        productCategoryCode = BasicProductCategoryCode.MELTED.value();
                                                    }
                                                    if (productCategoryCode != null) {
                                                        if (invoiceRecordType.equals(InvoiceRecordType.MELTED)) {
                                                            Product meltedProduct = productService.getProductByCode(BasicProductCode.MELTED_GOLD.value());
                                                            invoiceRecord.setProduct(meltedProduct);
                                                        } else {
                                                            final List<Stock> stocks = showStocksTable(null, productCategoryCode, true);
                                                            if (stocks != null && stocks.size() > 0) {
                                                                final Stock stock = stocks.get(0);
                                                                product = stock.getProduct();
                                                                invoiceRecord.setProduct(product);
                                                            }
                                                        }
                                                    }
                                                }
                                                default -> {
                                                    product = setProductByCommonInvoiceTypeCode(invoiceRecordType, invoiceRecord);
                                                }
                                            }

                                            switch (invoiceRecordType) {
                                                case DISCOUNT -> {
                                                    discountInput.requestFocus();
                                                    invoiceTable.getItems().remove(invoiceRecord);
                                                    return null;
                                                }
                                            }

                                        } else {
                                            FxUtil.error("shortcut.code.is.invalid");
                                        }
                                    }
                                } else {
                                    String[] splitByHyphen = textInputData.split("-");
                                    String productCategoryCode = splitByHyphen[0];
                                    if (splitByHyphen.length > 1) {
                                        String productCode = splitByHyphen[1];
                                        product = productService.getProductByCode(productCode);
                                    } else {
                                        product = new Product();
                                        ProductCategory productCategoryByCode = productCategoryService.getProductCategoryByCode(productCategoryCode);
                                        product.setProductCategory(productCategoryByCode);
                                        if (productCategoryByCode != null) {
                                            final List<Stock> stocks = showStocksTable(null, productCategoryCode, true);
                                            if (stocks != null && stocks.size() > 0) {
                                                final Stock stock = stocks.get(0);
                                                product = stock.getProduct();
                                            }
                                        }
                                    }

                                    if (product != null) {
                                        invoiceRecord.setProduct(product);
                                    } else {
                                        FxUtil.error("no.such.product");
                                    }
                                }
                            }

                            /*
                                if the product is not empty then there is no need to edit
                                product description column and jump directly to deal type column
                             */
                            nextColumn = columns.get(focusedCellColumn + 2);
                            shouldFocusOnCell = true;
                        }
                        case "productDescription" -> {
                            shouldFocusOnCell = true;
                        }
                        case "dealType" -> {
                            if (StringUtils.isEmpty(textInputData)) {
                                final DealType dealType = showDealTypeDialog(invoiceRecord);
                                if (dealType != null) {
                                    invoiceRecord.setDealType(dealType.value());
                                }
                            }
                            if (invoiceRecord.getDealType() != null) {
                                DealType dealType = DealType.getByValue(invoiceRecord.getDealType());
                                if (dealType != null) {
                                    invoiceRecord.setDealType(dealType.value());
                                    if (invoiceRecordType != null) {
                                        switch (invoiceRecordType) {
                                            case MELTED -> {
                                                nextColumn = columns.get(COLUMN_INDEX.weightByScale);
                                            }
                                            case PAYMENT_ORDER_CASH -> {
                                                nextColumn = columns.get(COLUMN_INDEX.totalAmount);
                                            }
                                        }
                                    }
                                }
                            }

                            if (invoiceRecordType != null) {
                                if (invoiceRecordType.equals(InvoiceRecordType.BANK)) {
                                    invoiceRecord.setBank(showBankDialog(invoiceRecord.getBank()));
                                }
                            }
                            shouldFocusOnCell = true;
                        }
                        case "count" -> {
                            if (StringUtils.isNotEmpty(textInputData)) {
                                try {
                                    invoiceRecord.setCount(Integer.valueOf(textInputData));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            shouldFocusOnCell = true;
                        }
                        case "weightByScale" -> {
                            if (StringUtils.isNotEmpty(textInputData)) {
                                try {
                                    invoiceRecord.setWeightByScale(Double.valueOf(textInputData));
                                } catch (NumberFormatException ignored) {
                                }
                            } else {
                                invoiceRecord.setWeightByScale(0.0);
                            }

                            final Integer dealTypeValue = invoiceRecord.getDealType();
                            DealType dealType = null;
                            if (dealTypeValue != null) {
                                dealType = InvoiceRecord.DealType.getByValue(dealTypeValue);
                            }

                            if (invoiceRecordType != null && invoiceRecordType.equals(InvoiceRecordType.MELTED) && dealType != null &&
                                    (dealType.equals(DealType.PAYMENT) || dealType.equals(DealType.SELL))) {
                                List<Stock> selectedStocks = showMeltedStockWithFinenessDialog(invoiceRecord.getWeightByScale());
                                if (selectedStocks != null) {
                                    int stockCount = 0;
                                    for (Stock stock : selectedStocks) {
                                        if (stockCount == 0) {
                                            invoiceRecord.setCarat(stock.getCarat());
                                            invoiceRecord.setProduct(stock.getProduct());
                                            invoiceRecord.setWeightByScale(stock.getWeight());
                                            if (invoiceRecord.getWeightByScale() != null && invoiceRecord.getWeightByScale() != 0) {
                                                // if the weight is specified by the user then it must be set as the weight
                                                invoiceRecord.setWeightByScale(invoiceRecord.getWeightByScale());
                                            }
                                        } else {
                                            InvoiceRecord newInvoiceRecord = new InvoiceRecord();
                                            newInvoiceRecord.setCarat(stock.getCarat());
                                            newInvoiceRecord.setDealType(invoiceRecord.getDealType());
                                            newInvoiceRecord.setInvoiceRecordType(InvoiceRecordType.MELTED);
                                            newInvoiceRecord.setProduct(stock.getProduct());
                                            newInvoiceRecord.setWeightByScale(stock.getWeight());
                                            invoiceTable.getItems().add(newInvoiceRecord);
                                        }
                                        stockCount++;
                                    }
                                }
                            }

                            shouldFocusOnCell = true;
                        }
                        case "carat" -> {
                            if (StringUtils.isNotEmpty(textInputData)) {
                                try {
                                    invoiceRecord.setCarat(Double.valueOf(textInputData));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            shouldFocusOnCell = true;
                        }
                        case "weightBasedOnCarat" -> {
                            // nothing to do here.
                            shouldFocusOnCell = true;
                        }
                        case "wagePercentage" -> {
                            if (StringUtils.isNotEmpty(textInputData)) {
                                try {
                                    final Double percentage = Double.valueOf(textInputData);
                                    if (percentage > 0 && percentage < 100) {
                                        invoiceRecord.setWagePercentage(percentage);
                                        nextColumn = getNextColumn(columns, focusedCellColumn);
                                    } else {
                                        FxUtil.warning("percentage.value.invalid");
                                        nextColumn = getNextColumn(columns, COLUMN_INDEX.wagePercentage - 1);
                                    }
                                } catch (NumberFormatException ignored) {
                                    nextColumn = getNextColumn(columns, COLUMN_INDEX.wagePercentage - 1);
                                }
                            }
                            shouldFocusOnCell = true;
                        }
                        case "weightByPercentage" -> {
                            // nothing to do here.
                            shouldFocusOnCell = true;
                        }
                        case "quote" -> {
                            if (StringUtils.isNotEmpty(textInputData)) {
                                try {
                                    invoiceRecord.setQuote(Double.valueOf(textInputData.replaceAll(",", "")));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            shouldFocusOnCell = true;
                        }
                        case "price" -> {
                            // nothing to do here.
                            shouldFocusOnCell = true;
                        }
                        case "fee" -> {
                            if (StringUtils.isNotEmpty(textInputData)) {
                                try {
                                    invoiceRecord.setFee(Double.valueOf(textInputData));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            shouldFocusOnCell = true;
                        }
                        case "totalFee" -> {
                            // nothing to do here.
                            shouldFocusOnCell = true;
                        }
                        case "officeBenefit" -> {
                            if (StringUtils.isNotEmpty(textInputData)) {
                                try {
                                    invoiceRecord.setBenefit(Double.valueOf(textInputData));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            shouldFocusOnCell = true;
                        }
                        case "benefitAmount" -> {
                            // nothing to do here.
                            shouldFocusOnCell = true;
                        }
                        case "tax" -> {
                            if (StringUtils.isNotEmpty(textInputData)) {
                                try {
                                    invoiceRecord.setTax(Double.valueOf(textInputData));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                        case "taxAmount" -> {
                            // nothing to do here.
                        }
                        case "totalAmount" -> {
                            if (invoiceRecordType.equals(InvoiceRecordType.PAYMENT_ORDER_CASH) ||
                                    invoiceRecordType.equals(InvoiceRecordType.CASH) ||
                                    invoiceRecordType.equals(InvoiceRecordType.BANK) ||
                                    invoiceRecordType.equals(InvoiceRecordType.CHEQUE)) {
                                if (StringUtils.isNotEmpty(textInputData)) {
                                    try {
                                        invoiceRecord.setAmount(MathUtil.round(Double.parseDouble(textInputData)));
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }

                            if (invoiceRecordType.equals(InvoiceRecordType.CASH) || invoiceRecordType.equals(InvoiceRecordType.BANK)) {
                                invoiceRecord.setDescription(showDescriptionDialog(invoiceRecord.getDescription()));
                            }
                        }

                    }
                }
                refreshTables();
                if (shouldFocusOnCell) {
                    focusOn(focusedCellRow, nextColumn, false);
                }
                if (nextColumn == null) { // means that this was the last column to edit and a new row must be created
                    if (defaultInvoiceRecordType != null) {
                        addNewInvoiceRecord(defaultInvoiceRecordType);
                    } else {
                        addNewInvoiceRecord(InvoiceRecordType.UNDEFINED);
                    }
                }
            } catch (SQLException e) {
                FxUtil.exceptionOccurred(e);
            }

            return null;
        };
    }

    private void focusOn(int row, TableColumn<InvoiceRecord, ?> column, boolean reverse) {
        if (column != null) {
            invoiceTable.getFocusModel().focus(row, column);
            if (column.isEditable()) {
                invoiceTable.edit(row, column);
            } else {
                TablePosition<InvoiceRecord, ?> focusedCell = invoiceTable.getFocusModel().getFocusedCell();
                int focusedCellColumn = focusedCell.getColumn();
                ObservableList<TableColumn<InvoiceRecord, ?>> columns = invoiceTable.getColumns();

                if (reverse) {
                    if (focusedCellColumn - 1 != 0) {
                        column = columns.get(focusedCellColumn - 1);
                    }
                } else {
                    if (focusedCellColumn + 1 != columns.size()) {
                        column = columns.get(focusedCellColumn + 1);
                    }
                }
                focusOn(row, column, reverse);
            }
        }
    }

    private TableColumn getNextColumn(ObservableList<TableColumn<InvoiceRecord, ?>> columns, int currentColumnIndex) {
        TableColumn<InvoiceRecord, ?> nextColumn = null;
        if (currentColumnIndex + 1 < columns.size()) {
            nextColumn = columns.get(currentColumnIndex + 1);
            InvoiceRecord invoiceRecord = invoiceTable.getFocusModel().getFocusedItem();
            if (nextColumn.isEditable() && getEditableExtractor(nextColumn.getId()).apply(invoiceRecord).getValue()) {
                return nextColumn;
            } else {
                return getNextColumn(columns, currentColumnIndex + 1);
            }
        } else {
            return null;
        }
    }

    @Inject
    public InvoiceController(InvoiceService invoiceService, InvoiceRecordService invoiceRecordService, StockService stockService,
                             ProductService productService, ProductCategoryService productCategoryService, PartyService partyService,
                             AccountBalanceService accountBalanceService, ReportService reportService) {
        this.invoiceService = invoiceService;
        this.invoiceRecordService = invoiceRecordService;
        this.stockService = stockService;
        this.productService = productService;
        this.productCategoryService = productCategoryService;
        this.partyService = partyService;
        this.accountBalanceService = accountBalanceService;
        this.reportService = reportService;
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {

        decimalFormat.setMaximumFractionDigits(4);
        DecimalFormatSymbols otherSymbols = decimalFormat.getDecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        decimalFormat.setDecimalFormatSymbols(otherSymbols);

        final SystemParameter defaultInvoiceTypeParam = getParam(SysParams.DEFAULT_INVOICE_TYPE);
        if (defaultInvoiceTypeParam != null && StringUtils.isNotEmpty(defaultInvoiceTypeParam.getValue())) {
            defaultInvoiceRecordType = InvoiceRecordType.getByValue(Integer.parseInt(defaultInvoiceTypeParam.getValue()));
        }

        setHelpTableRows();
        setupInvoiceTable();
        setupPreviousBalanceTable();
        setupInvoiceBalanceTable();
        setupFinalBalanceTable();
        setupTotalBalanceTable();
        resetForm();
        setupInvoiceForm();

    }

    @Override
    public void postLoad() {
        super.postLoad();
        setupSceneShortcuts();
        setupInvoiceTableShortcuts();
        setupInvoiceTableColumnWidths();

        if (stage != null) {
            stage.widthProperty().addListener((observableValue, number, t1) -> setupInvoiceTableColumnWidths());
            stage.maximizedProperty().addListener((observableValue, aBoolean, t1) -> setupInvoiceTableColumnWidths());

            stage.getScene().getStylesheets().add("/css/invoice.css");
        }

    }

    private void resetForm() {
        setFocusNodes(documentNumberInput, invoiceNumberInput, invoiceDateInput, partyCodeInput, partyNameComboBox);
        documentNumberInput.clear();
        invoiceNumberInput.clear();
        invoiceDateInput.setText(DateUtil.getCurrentPersianDateString());
        partyCodeInput.clear();
        partyNameComboBox.getSelectionModel().clearSelection();
        invoiceTable.getItems().clear();
        previousBalanceTable.getItems().clear();
        invoiceBalanceTable.getItems().clear();
        finalBalanceTable.getItems().clear();
        documentNumberInput.requestFocus();
    }

    private void onPartySelect(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            final Party selectedItem = partyNameComboBox.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                partyNameComboBox.requestFocus();
            } else {
                final ObservableList<InvoiceRecord> invoiceTableItems = invoiceTable.getItems();
                if (invoiceTableItems == null || invoiceTableItems.size() == 0) {
                    if (defaultInvoiceRecordType != null) {
                        addNewInvoiceRecord(defaultInvoiceRecordType);
                    } else {
                        addNewInvoiceRecord(InvoiceRecordType.UNDEFINED);
                    }
                    loadPreviousBalanceData();
                } else if (invoiceTableItems.size() > 0) {
                    TableColumn<InvoiceRecord, ?> columnToEdit = invoiceTable.getColumns().get(COLUMN_INDEX.groupAndProductCode);
                    invoiceTable.edit(invoiceTableItems.size() - 1, columnToEdit);
                }
            }
        }
    }

    private void setupInvoiceForm() throws SQLException {

        List<Party> allParties = partyService.getAllParties();
        partyNameComboBox.setConverter(new PartyStringConverter(allParties));
        partyNameComboBox.setItems(FXCollections.observableArrayList(allParties));
        partyNameComboBox.setOnKeyPressed(this::onPartySelect);

        partyNameComboBox.setOnAction(event -> {
            Party selectedItem = partyNameComboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                try {
                    Integer nextInvoiceNumber = invoiceService.getNextInvoiceNumber(selectedItem);
                    invoiceNumberInput.setText(String.valueOf(nextInvoiceNumber));
                    partyCodeInput.setText(selectedItem.getCode());
                } catch (SQLException e) {
                    FxUtil.exceptionOccurred(e);
                }
            }
        });
        partyNameComboBox.focusedProperty().addListener((observable, oldValue, newValue) -> partyNameComboBox.show());
        new ComboBoxAutoComplete<>(partyNameComboBox);

        documentNumberInput.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                try {
                    Integer nextDocumentNumber = invoiceService.getNextDocumentNumber();
                    documentNumberInput.setText(String.valueOf(nextDocumentNumber));
                } catch (SQLException e) {
                    FxUtil.exceptionOccurred(e);
                }
            }
        });
        documentNumberInput.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            String documentNumberInputText = documentNumberInput.getText();
            Integer documentNumber = 0;
            try {
                documentNumber = Integer.parseInt(documentNumberInputText);
            } catch (NumberFormatException ignored) {
            }

            KeyCode keyCode = event.getCode();
            boolean upOrDown = keyCode.equals(KeyCode.UP) || keyCode.equals(KeyCode.DOWN);
            if (keyCode.equals(KeyCode.UP)) {
                if (documentNumber >= 0) {
                    documentNumberInput.setText(String.valueOf(documentNumber + 1));
                } else {
                    documentNumberInput.setText("0");
                }
            } else if (keyCode.equals(KeyCode.DOWN)) {
                if (documentNumber > 0) {
                    documentNumberInput.setText(String.valueOf(documentNumber - 1));
                } else {
                    documentNumberInput.setText("0");
                }
            }

            if (upOrDown) {
                try {
                    final String documentNumberText = documentNumberInput.getText();
                    final Invoice invoiceByDocumentNumber = invoiceService.getByDocumentNumber(Integer.valueOf(documentNumberText));
                    if (invoiceByDocumentNumber != null && invoiceByDocumentNumber.getInvoiceRecords() != null) {
                        invoiceTable.setItems(FXCollections.observableList(new ArrayList<>(invoiceByDocumentNumber.getInvoiceRecords())));

                        invoiceDateInput.setText(DateUtil.convertDateToPersianString(invoiceByDocumentNumber.getCreateTime()));
                        invoiceNumberInput.setText(String.valueOf(invoiceByDocumentNumber.getInvoiceNumber()));
                        partyNameComboBox.getSelectionModel().select(invoiceByDocumentNumber.getParty());
                        partyCodeInput.setText(invoiceByDocumentNumber.getParty().getCode());
                        invoiceTable.refresh();
                    }
                } catch (SQLException e) {
                    FxUtil.exceptionOccurred(e);
                }
            }

        });

        invoiceDateInput.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            String dateInputText = invoiceDateInput.getText();


            KeyCode keyCode = event.getCode();
            boolean upOrDown = keyCode.equals(KeyCode.UP) || keyCode.equals(KeyCode.DOWN);
            if (keyCode.equals(KeyCode.UP)) {
                dateInputText = DateUtil.getNextPersianDate(dateInputText);
            } else if (keyCode.equals(KeyCode.DOWN)) {
                dateInputText = DateUtil.getPreviousPersianDate(dateInputText);
            }

            if (upOrDown) {
                invoiceDateInput.setText(dateInputText);
            }

        });

        invoiceDateInput.setText(DateUtil.getCurrentPersianDateString());

        partyCodeInput.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                try {
                    String partyCodeInputText = partyCodeInput.getText();
                    if (StringUtils.isNotEmpty(partyCodeInputText)) {
                        Party partyByCode = partyService.getPartyByCode(partyCodeInputText);
                        if (partyByCode == null) {
                            FxUtil.warning("no.such.party");
                            partyCodeInput.requestFocus();
                        } else {
                            partyNameComboBox.getSelectionModel().select(partyByCode);
                            partyNameComboBox.requestFocus();
                        }
                    }
                } catch (SQLException e) {
                    FxUtil.exceptionOccurred(e);
                }
            }
        });

        FxUtil.handleCaretPosition(documentNumberInput, invoiceNumberInput, invoiceDateInput);
    }

    private Double getWeightBasedOnCarat(InvoiceRecord invoiceRecord) {
        Double carat = invoiceRecord.getCarat();
        Double weightByScale = invoiceRecord.getWeightByScale();
        Double weightBasedOnCarat = null;
        if (carat == null) {
            carat = ApplicationContext.getOfficeCarat();
        }
        if (weightByScale != null) {
            weightBasedOnCarat = (weightByScale * carat) / Constants.GOLD_QUOTE;
        }
        return weightBasedOnCarat;
    }

    private void setupTotalBalanceTable() {
        // TODO
        totalTradesTable.setPlaceholder(new Label(message("table.empty.placeholder")));
    }

    private void setupFinalBalanceTable() {
        setupBalanceTable(finalBalanceTable);
    }

    private void setupInvoiceBalanceTable() {
        setupBalanceTable(invoiceBalanceTable);
    }

    private void setupPreviousBalanceTable() {
        setupBalanceTable(previousBalanceTable);
    }

    private void setupBalanceTable(TableView tableView) {
        var titleColumn = new TableColumn<AccountBalance, String>(message("product"));
        titleColumn.setId("title");
        titleColumn.setCellValueFactory(getBalanceValueFactory("title"));

        var debitColumn = new TableColumn<AccountBalance, String>(message("debit"));
        debitColumn.setId("debit");
        debitColumn.styleProperty().setValue("-fx-alignment: CENTER-RIGHT;");
        debitColumn.setCellValueFactory(getBalanceValueFactory("debit"));

        var creditColumn = new TableColumn<AccountBalance, String>(message("credit"));
        creditColumn.setId("credit");
        creditColumn.styleProperty().setValue("-fx-alignment: CENTER-RIGHT;");
        creditColumn.setCellValueFactory(getBalanceValueFactory("credit"));

        tableView.setPlaceholder(new Label(message("table.empty.placeholder")));

        tableView.getColumns().addAll(titleColumn, debitColumn, creditColumn);

    }

    private Callback<TableColumn.CellDataFeatures<AccountBalance, String>, ObservableValue<String>> getBalanceValueFactory(String cellName) {
        return param -> {
            if (param != null) {
                AccountBalance balanceData = param.getValue();
                if (balanceData != null) {
                    Product product = balanceData.getProduct();
                    final Boolean countable = product.getProductCategory().getCountable();
                    final Double count = balanceData.getCount();
                    final Double amount = balanceData.getAmount();
                    return new ObjectBinding<>() {
                        @Override
                        protected String computeValue() {
                            if (product != null) {
                                switch (cellName) {
                                    case "title" -> {
                                        return product.getProductName();
                                    }
                                    case "debit" -> {
                                        if (countable && count != null && count < 0) {
                                            return decimalFormat.format(count);
                                        } else if (amount != null && amount < 0) {
                                            return decimalFormat.format(amount);
                                        }
                                    }
                                    case "credit" -> {
                                        if (countable && count != null && count > 0) {
                                            return decimalFormat.format(count);
                                        } else if (amount != null && amount > 0) {
                                            return decimalFormat.format(amount);
                                        }
                                    }
                                }
                            }
                            return "";
                        }
                    };
                }
            }
            return null;
        };
    }

    private void setupInvoiceTable() {

        var groupAndProductCodeColumn = new TableColumn<InvoiceRecord, String>(message("group.and.product.code.column"));
        groupAndProductCodeColumn.setCellValueFactory(getInvoiceCellValueFactory("groupAndProductCode"));
        groupAndProductCodeColumn.setId("groupAndProductCode");
        groupAndProductCodeColumn.setEditable(true);
        groupAndProductCodeColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("groupAndProductCode"));
        groupAndProductCodeColumn.setCellFactory(getInvoiceTableCellFactory("groupAndProductCode"));

        var productDescriptionColumn = new TableColumn<InvoiceRecord, String>(message("product.name"));
        productDescriptionColumn.setCellValueFactory(getInvoiceCellValueFactory("productDescription"));
        productDescriptionColumn.setId("productDescription");
        productDescriptionColumn.setEditable(true);
        productDescriptionColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("productDescription"));
        productDescriptionColumn.setCellFactory(getInvoiceTableCellFactory("productDescription"));

        var dealTypeColumn = new TableColumn<InvoiceRecord, String>(message("deal.type"));
        dealTypeColumn.setCellValueFactory(getInvoiceCellValueFactory("dealType"));
        dealTypeColumn.setId("dealType");
        dealTypeColumn.setEditable(true);
        dealTypeColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("dealType"));
        dealTypeColumn.setCellFactory(getInvoiceTableCellFactory("dealType"));

        var countColumn = new TableColumn<InvoiceRecord, String>(message("count"));
        countColumn.setCellValueFactory(getInvoiceCellValueFactory("count"));
        countColumn.setId("count");
        countColumn.setEditable(true);
        countColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("count"));
        countColumn.setCellFactory(getInvoiceTableCellFactory("count"));
        countColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var weightByScaleColumn = new TableColumn<InvoiceRecord, String>(message("weight.by.scale"));
        weightByScaleColumn.setCellValueFactory(getInvoiceCellValueFactory("weightByScale"));
        weightByScaleColumn.setId("weightByScale");
        weightByScaleColumn.setEditable(true);
        weightByScaleColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("weightByScale"));
        weightByScaleColumn.setCellFactory(getInvoiceTableCellFactory("weightByScale"));
        weightByScaleColumn.setStyle("-fx-alignment: CENTER-RIGHT;");


        var caratColumn = new TableColumn<InvoiceRecord, String>(message("carat"));
        caratColumn.setCellValueFactory(getInvoiceCellValueFactory("carat"));
        caratColumn.setId("carat");
        caratColumn.setEditable(true);
        caratColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("carat"));
        caratColumn.setCellFactory(getInvoiceTableCellFactory("carat"));
        caratColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var weightBasedOnCaratColumn = new TableColumn<InvoiceRecord, String>(message("weight.based.on.carat"));
        weightBasedOnCaratColumn.setCellValueFactory(getInvoiceCellValueFactory("weightBasedOnCarat"));
        weightBasedOnCaratColumn.setId("weightBasedOnCarat");
        weightBasedOnCaratColumn.setEditable(true);
        weightBasedOnCaratColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("weightBasedOnCarat"));
        weightBasedOnCaratColumn.setCellFactory(getInvoiceTableCellFactory("weightBasedOnCarat"));
        weightBasedOnCaratColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var wagePercentageColumn = new TableColumn<InvoiceRecord, String>(message("wage.percentage"));
        wagePercentageColumn.setCellValueFactory(getInvoiceCellValueFactory("wagePercentage"));
        wagePercentageColumn.setId("wagePercentage");
        wagePercentageColumn.setEditable(true);
        wagePercentageColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("wagePercentage"));
        wagePercentageColumn.setCellFactory(getInvoiceTableCellFactory("wagePercentage"));
        wagePercentageColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var weightByPercentageColumn = new TableColumn<InvoiceRecord, String>(message("weight.by.percentage"));
        weightByPercentageColumn.setCellValueFactory(getInvoiceCellValueFactory("weightByPercentage"));
        weightByPercentageColumn.setId("weightByPercentage");
        weightByPercentageColumn.setEditable(true);
        weightByPercentageColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("weightByPercentage"));
        weightByPercentageColumn.setCellFactory(getInvoiceTableCellFactory("weightByPercentage"));
        weightByPercentageColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var quoteColumn = new TableColumn<InvoiceRecord, String>(message("quote"));
        quoteColumn.setCellValueFactory(getInvoiceCellValueFactory("quote"));
        quoteColumn.setId("quote");
        quoteColumn.setEditable(true);
        quoteColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("quote"));
        quoteColumn.setCellFactory(getInvoiceTableCellFactory("quote"));
        quoteColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var priceColumn = new TableColumn<InvoiceRecord, String>(message("price"));
        priceColumn.setCellValueFactory(getInvoiceCellValueFactory("price"));
        priceColumn.setId("price");
        priceColumn.setEditable(true);
        priceColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("price"));
        priceColumn.setCellFactory(getInvoiceTableCellFactory("price"));
        priceColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var feeColumn = new TableColumn<InvoiceRecord, String>(message("fee"));
        feeColumn.setCellValueFactory(getInvoiceCellValueFactory("fee"));
        feeColumn.setId("fee");
        feeColumn.setEditable(true);
        feeColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("fee"));
        feeColumn.setCellFactory(getInvoiceTableCellFactory("fee"));
        feeColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var totalFeeColumn = new TableColumn<InvoiceRecord, String>(message("total.fee"));
        totalFeeColumn.setCellValueFactory(getInvoiceCellValueFactory("totalFee"));
        totalFeeColumn.setId("totalFee");
        totalFeeColumn.setEditable(true);
        totalFeeColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("totalFee"));
        totalFeeColumn.setCellFactory(getInvoiceTableCellFactory("totalFee"));
        totalFeeColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var officeBenefitColumn = new TableColumn<InvoiceRecord, String>(message("office.benefit"));
        officeBenefitColumn.setCellValueFactory(getInvoiceCellValueFactory("officeBenefit"));
        officeBenefitColumn.setId("officeBenefit");
        officeBenefitColumn.setEditable(true);
        officeBenefitColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("officeBenefit"));
        officeBenefitColumn.setCellFactory(getInvoiceTableCellFactory("officeBenefit"));
        officeBenefitColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var benefitAmountColumn = new TableColumn<InvoiceRecord, String>(message("benefit.amount"));
        benefitAmountColumn.setCellValueFactory(getInvoiceCellValueFactory("benefitAmount"));
        benefitAmountColumn.setId("benefitAmount");
        benefitAmountColumn.setEditable(true);
        benefitAmountColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("benefitAmount"));
        benefitAmountColumn.setCellFactory(getInvoiceTableCellFactory("benefitAmount"));
        benefitAmountColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var taxColumn = new TableColumn<InvoiceRecord, String>(message("tax"));
        taxColumn.setCellValueFactory(getInvoiceCellValueFactory("tax"));
        taxColumn.setId("tax");
        taxColumn.setEditable(true);
        taxColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("tax"));
        taxColumn.setCellFactory(getInvoiceTableCellFactory("tax"));
        taxColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var taxAmountColumn = new TableColumn<InvoiceRecord, String>(message("tax.amount"));
        taxAmountColumn.setCellValueFactory(getInvoiceCellValueFactory("taxAmount"));
        taxAmountColumn.setId("taxAmount");
        taxAmountColumn.setEditable(true);
        taxAmountColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("taxAmount"));
        taxAmountColumn.setCellFactory(getInvoiceTableCellFactory("taxAmount"));
        taxAmountColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        var totalAmountColumn = new TableColumn<InvoiceRecord, String>(message("total.amount"));
        totalAmountColumn.setCellValueFactory(getInvoiceCellValueFactory("totalAmount"));
        totalAmountColumn.setId("totalAmount");
        totalAmountColumn.setEditable(true);
        totalAmountColumn.setOnEditCommit(onInvoiceTableCellCommitHandler("totalAmount"));
        totalAmountColumn.setCellFactory(getInvoiceTableCellFactory("totalAmount"));
        totalAmountColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        invoiceTable.setPlaceholder(new Label(message("table.empty.placeholder")));

        invoiceTable.getColumns().addAll(groupAndProductCodeColumn, productDescriptionColumn, dealTypeColumn, countColumn, weightByScaleColumn,
                caratColumn, weightBasedOnCaratColumn, wagePercentageColumn, weightByPercentageColumn, quoteColumn, priceColumn, feeColumn,
                totalFeeColumn, officeBenefitColumn, benefitAmountColumn, taxColumn, taxAmountColumn, totalAmountColumn);
        invoiceTable.setEditable(true);

    }

    private void setupInvoiceTableColumnWidths() {
        double invoiceTableWidth = invoiceTable.getPrefWidth();
        if (invoiceTable.getWidth() != 0 && invoiceTable.getPrefWidth() != 1.0) {
            invoiceTableWidth = invoiceTable.getWidth();
        } else if (invoiceTableWidth == 0) {
            invoiceTableWidth = 1;
        }

        invoiceTable.getColumns().get(COLUMN_INDEX.groupAndProductCode).setPrefWidth(invoiceTableWidth * 0.7 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.productDescription).setPrefWidth(invoiceTableWidth * 1.5 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.dealType).setPrefWidth(invoiceTableWidth * 0.7 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.count).setPrefWidth(invoiceTableWidth * 0.8 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.weightByScale).setPrefWidth(invoiceTableWidth * 0.8 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.carat).setPrefWidth(invoiceTableWidth * 0.8 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.weightBasedOnCarat).setPrefWidth(invoiceTableWidth * 0.8 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.wagePercentage).setPrefWidth(invoiceTableWidth * 0.8 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.weightByPercentage).setPrefWidth(invoiceTableWidth * 0.8 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.quote).setPrefWidth(invoiceTableWidth * 1.4 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.price).setPrefWidth(invoiceTableWidth * 1.4 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.fee).setPrefWidth(invoiceTableWidth * 1.3 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.totalFee).setPrefWidth(invoiceTableWidth * 1.3 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.officeBenefit).setPrefWidth(invoiceTableWidth * 0.8 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.benefitAmount).setPrefWidth(invoiceTableWidth * 0.8 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.tax).setPrefWidth(invoiceTableWidth * 0.8 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.taxAmount).setPrefWidth(invoiceTableWidth * 0.8 / 18);
        invoiceTable.getColumns().get(COLUMN_INDEX.totalAmount).setPrefWidth(invoiceTableWidth * 2 / 18);

    }

    private void setupSceneShortcuts() {
        Node containerVBox = stage.getScene().lookup("#containerVBox");
        containerVBox.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            String keyChar = keyEvent.getCode().getChar();
            if (keyEvent.isControlDown()) {
                switch (keyChar) {
                    case "F" -> {
                        // A new invoice
                        resetForm();
                    }
                    case "D" -> {
                        // delete current invoice record
                        int focusedIndex = invoiceTable.getFocusModel().getFocusedIndex();
                        final ObservableList<InvoiceRecord> invoiceTableItems = invoiceTable.getItems();
                        if (invoiceTableItems != null && invoiceTableItems.size() > 0) {
                            InvoiceRecord selectedItem = invoiceTableItems.get(focusedIndex);
                            boolean confirmed = FxUtil.confirm("remove.confirm");
                            if (confirmed) {

                                invoiceTable.edit(-1, null);
                                if (selectedItem.getId() == null) {
                                    selectedItem.setId(-1);
                                }
                                invoiceTable.getItems().remove(selectedItem);
                                invoiceTable.layout();
                                invoiceTable.refresh();
                            }
                        }
                    }
                    case "N" -> {
                        // A new invoice record
                        if (defaultInvoiceRecordType != null) {
                            addNewInvoiceRecord(defaultInvoiceRecordType);
                        } else {
                            addNewInvoiceRecord(InvoiceRecordType.UNDEFINED);
                        }
                    }
                    case "S" -> {
                        recordInvoiceBtnAction(null);
                    }
                    case "M" -> {
                        temporaryRecordInvoiceBtnAction(null);
                    }
                    case "P" -> {
                        printInvoiceBtnAction(null);
                    }
                }
            } else if (keyEvent.isAltDown()) {
                switch (keyChar) {
                    case "1" -> {
                        log.debug("1 is pressed");
                    }
                }
            }
        });

        invoiceTable.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {

                    ContextMenu contextMenu = new ContextMenu();

                    EventTarget eventTarget = mouseEvent.getTarget();
                    Node menuNode = invoiceTable;
                    if (eventTarget instanceof EditCell) {
                        TableRow<?> tableRow = ((EditCell<?, ?>) eventTarget).getTableRow();
                        menuNode = tableRow;
                        if (tableRow != null) {
                            Object rowItem = tableRow.getItem();
                            if (rowItem instanceof InvoiceRecord) {
                                InvoiceRecord invoiceRecord = ((InvoiceRecord) rowItem);

                                MenuItem removeMenu = new MenuItem(message("remove"));
                                removeMenu.setOnAction(event -> {
                                    if (FxUtil.confirm("remove.confirm")) {
                                        invoiceTable.getItems().remove(invoiceRecord);
                                    }
                                });

                                MenuItem descriptionMenu = new MenuItem(message("regard"));
                                descriptionMenu.setOnAction(event -> {
                                    String description = showDescriptionDialog(invoiceRecord.getDescription());
                                    invoiceRecord.setDescription(description);
                                });

                                MenuItem stoneMenu = new MenuItem(message("stone"));
                                stoneMenu.setOnAction(event -> {
                                    final Collection<Stone> stoneCollection = invoiceRecord.getStones();
                                    ObservableList<Stone> stones = null;
                                    if (stoneCollection != null) {
                                        stones = FXCollections.observableArrayList(stoneCollection);
                                    }
                                    List<Stone> result = showStoneDialog(stones);
                                    invoiceRecord.setStones(result);
                                });

                                MenuItem currencyMenu = new MenuItem(message("currency"));
                                currencyMenu.setOnAction(event -> {
                                    Product selectedCurrency = showCoinOrCurrencySelectionDialog(invoiceRecord.getProduct(), BasicProductCategoryCode.CURRENCY.value());
                                    invoiceRecord.setProduct(selectedCurrency);
                                });

                                MenuItem coinMenu = new MenuItem(message("coin"));
                                coinMenu.setOnAction(event -> {
                                    Product selectedCurrency = showCoinOrCurrencySelectionDialog(invoiceRecord.getProduct(), BasicProductCategoryCode.COIN.value());
                                    invoiceRecord.setProduct(selectedCurrency);
                                });

                                switch (invoiceRecord.getInvoiceRecordType()) {
                                    case CASH, BANK -> {
                                        contextMenu.getItems().add(descriptionMenu);
                                    }
                                    case MANUFACTURED, STONE -> {
                                        contextMenu.getItems().add(stoneMenu);
                                    }
                                    case CURRENCY, PAYMENT_ORDER_CURRENCY -> {
                                        contextMenu.getItems().add(currencyMenu);
                                    }
                                    case COIN -> {
                                        contextMenu.getItems().add(coinMenu);
                                    }
                                }
                                contextMenu.getItems().add(removeMenu);
                            }
                        }
                    }

                    if (contextMenu.getItems() != null && contextMenu.getItems().size() > 0) {
                        contextMenu.show(menuNode, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                    }
                }
            }
        });

    }

    private void setupInvoiceTableShortcuts() {
        invoiceTable.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            String keyChar = keyEvent.getCode().getChar();
            if (keyEvent.isControlDown()) {
                switch (keyChar) {
                    case "T" -> {
                        log.debug("T is pressed");
                    }
                    case "R" -> {
                        log.debug("R is pressed");
                    }
                    case "N" -> {
                        log.debug("N is pressed");
                    }

                }
            }
        });

        invoiceTable.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                EventTarget mouseEventTarget = mouseEvent.getTarget();
                InvoiceRecord invoiceRecord = null;
                if (mouseEventTarget instanceof EditCell) {
                    invoiceRecord = (InvoiceRecord) ((EditCell) mouseEvent.getTarget()).getTableRow().getItem();
                } else if (mouseEventTarget instanceof TableRow) {
                    invoiceRecord = (InvoiceRecord) ((TableRow) mouseEvent.getTarget()).getItem();
                }

                if (invoiceRecord != null) {
                    // TODO: show all related invoice records data.-
                }
            }
        });
    }

    private void setHelpTableRows() {

        TableColumn<InvoiceRecordType, String> helpColumn = new TableColumn<>(message("document.type"));
        helpColumn.setCellValueFactory(param -> {
            if (param != null) {
                InvoiceRecordType value = param.getValue();
                return new ObjectBinding<>() {
                    @Override
                    protected String computeValue() {
                        return String.valueOf(value.value());
                    }
                };
            }
            return null;
        });

        var cellFactory = new Callback<TableColumn<InvoiceRecordType, String>, TableCell<InvoiceRecordType, String>>() {
            @Override
            public TableCell call(final TableColumn<InvoiceRecordType, String> param) {
                final TableCell<InvoiceRecordType, String> cell = new TableCell<InvoiceRecordType, String>() {

                    final Button btn = new Button("");

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        btn.getStyleClass().add("invoice-help-btn");
                        ContextMenu contextMenu = new ContextMenu();
                        MenuItem setDefaultMenu = new MenuItem(message("set.as.default"));
                        setDefaultMenu.setOnAction(event -> {
                            final Object userData = ((MenuItem) event.getTarget()).getUserData();
                            if (userData != null) {
                                final InvoiceRecordType invoiceRecordType = InvoiceRecordType.getByValue(Integer.parseInt((String) userData));
                                defaultInvoiceRecordType = invoiceRecordType;
                                setParam(SysParams.DEFAULT_INVOICE_TYPE, (String) userData);
                            }
                            shortcutHelpTable.refresh();
                        });

                        MenuItem unsetDefaultMenu = new MenuItem(message("unset.as.default"));
                        unsetDefaultMenu.setOnAction(event -> {
                            final Object userData = ((MenuItem) event.getTarget()).getUserData();
                            if (userData != null) {
                                defaultInvoiceRecordType = null;
                                setParam(SysParams.DEFAULT_INVOICE_TYPE, "");
                            }
                            shortcutHelpTable.refresh();
                        });

                        contextMenu.getItems().add(setDefaultMenu);
                        setDefaultMenu.setUserData(item);
                        unsetDefaultMenu.setUserData(item);
                        btn.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                            if (event.getButton() == MouseButton.SECONDARY) {
                                contextMenu.show(btn, event.getScreenX(), event.getScreenY());
                            }
                        });

                        if (empty) {
                            setGraphic(null);
                        } else {
                            final InvoiceRecordType invoiceRecordType = InvoiceRecordType.getByValue(Integer.parseInt(item));
                            String message = message("invoice.shortcut.help." + invoiceRecordType.name().toLowerCase());
                            if (invoiceRecordType.equals(defaultInvoiceRecordType)) {
                                btn.setStyle("-fx-background-color: lime");
                                contextMenu.getItems().add(unsetDefaultMenu);
                            } else {
                                btn.setStyle("-fx-background-color: #CCCCCC");
                            }
                            btn.setText(message);
                            btn.setPrefWidth(100);
                            Tooltip tooltip = new Tooltip(message);
                            tooltip.setShowDelay(new Duration(100));
                            btn.setTooltip(tooltip);
                            btn.setOnAction(event -> {
                                addNewInvoiceRecord(InvoiceRecordType.getByValue(Integer.parseInt(item)));
                            });
                            setGraphic(btn);
                            btn.setStyle("-fx-font-size: 14;");

                        }
                        setText(null);
                    }
                };
                return cell;
            }
        };
        helpColumn.setCellFactory(cellFactory);

        shortcutHelpTable.getColumns().addAll(helpColumn);
        for (InvoiceRecordType code : InvoiceRecordType.values()) {
            if (code != InvoiceRecordType.UNDEFINED) {
                shortcutHelpTable.getItems().add(code);
            }
        }
    }

    private void addNewInvoiceRecord(InvoiceRecordType invoiceRecordType) {
        if (invoiceRecordType != null && invoiceRecordType.equals(InvoiceRecordType.DISCOUNT)) {
            discountInput.requestFocus();
            return;
        }
        InvoiceRecord newInvoiceRecord = new InvoiceRecord();
        Product product;
        newInvoiceRecord.setInvoiceRecordType(InvoiceRecord.InvoiceRecordType.PRODUCT);

        product = setProductByCommonInvoiceTypeCode(invoiceRecordType, newInvoiceRecord);

        newInvoiceRecord.setProduct(product);
        invoiceTable.getItems().add(newInvoiceRecord);
        invoiceTable.layout();
        if (invoiceRecordType == InvoiceRecordType.UNDEFINED || (product != null && StringUtils.isEmpty(product.getCode()))) {
            editLastRecord(0); // 0 is for column product category and product code
        } else {
            editLastRecord(2); // 2 is for deal type
        }

    }

    public void newInvoiceBtnAction(ActionEvent event) {
        resetForm();
    }

    private void validateInvoiceForm() throws InvoiceValidationException, SQLException {

        // validate party
        Party party = partyNameComboBox.getSelectionModel().getSelectedItem();
        if (party == null) {
            throw new InvoiceValidationException("please.select.party");
        }

        // validate document number
        String documentNumberInputText = documentNumberInput.getText();
        try {
            if (invoiceService.documentNumberExists(Integer.valueOf(documentNumberInputText))) {
                throw new InvoiceValidationException("document.number.exists");
            }
        } catch (NumberFormatException e) {
            throw new InvoiceValidationException("document.number.invalid");
        }

        // validate invoice number
        String invoiceNumberInputText = invoiceNumberInput.getText();
        try {
            if (invoiceService.invoiceNumberExists(party, Integer.valueOf(invoiceNumberInputText))) {
                throw new InvoiceValidationException("invoice.number.exists");
            }
        } catch (NumberFormatException e) {
            throw new InvoiceValidationException("invoice.number.invalid");
        }

        // validate date
        String invoiceDateInputText = invoiceDateInput.getText();
        if (StringUtils.isNotEmpty(invoiceDateInputText)) {
            if (DateUtil.isGreaterThanToday(invoiceDateInputText)) {
                throw new InvoiceValidationException("date.must.be.less.than.today");
            }
        } else {
            throw new InvoiceValidationException("date.is.required");
        }
    }

    public void printInvoiceBtnAction(ActionEvent event) {
        final ObservableList<InvoiceRecord> invoiceTableItems = invoiceTable.getItems();
        if (invoiceTableItems == null || invoiceTableItems.size() == 0) {
            FxUtil.warning("no.invoice.record.for.print");
        } else {
            final InvoicePrintData printDate = getPrintDate();
            final HashMap<String, Object> params = new HashMap<>();
            params.put("documentNumber", printDate.getDocumentNumber());
            params.put("invoiceNumber", printDate.getInvoiceNumber());
            params.put("invoiceDate", printDate.getDate());
            params.put("partyName", printDate.getPartyName());
            params.put("partyCode", printDate.getPartyCode());
            PrintUtil.printByJasper("jasper/test.jrxml", params, printDate.getInvoiceRecordPrintDataList());
        }
    }

    private InvoicePrintData getPrintDate() {
        InvoicePrintData invoicePrintData = new InvoicePrintData();
        invoicePrintData.setDocumentNumber(documentNumberInput.getText());
        invoicePrintData.setInvoiceNumber(invoiceNumberInput.getText());
        invoicePrintData.setDate(invoiceDateInput.getText());
        invoicePrintData.setPartyCode(partyCodeInput.getText());
        final Party selectedParty = partyNameComboBox.getSelectionModel().getSelectedItem();
        if (selectedParty != null) {
            invoicePrintData.setPartyName(selectedParty.getPartyName());
        }

        List<InvoicePrintData.InvoiceRecordPrintData> invoiceRecordPrintDataList = new ArrayList<>();
        invoicePrintData.setInvoiceRecordPrintDataList(invoiceRecordPrintDataList);
        final ObservableList<InvoiceRecord> invoiceTableItems = invoiceTable.getItems();
        if (invoiceTableItems != null) {
            for (InvoiceRecord invoiceRecord : invoiceTableItems) {
                InvoicePrintData.InvoiceRecordPrintData invoiceRecordPrintData = new InvoicePrintData.InvoiceRecordPrintData();
                final Integer dealTypeValue = invoiceRecord.getDealType();
                final DealType dealType = DealType.getByValue(dealTypeValue);
                final InvoiceRecordType invoiceRecordType = invoiceRecord.getInvoiceRecordType();
                final Product product = invoiceRecord.getProduct();
                ProductCategory productCategory = null;
                if (product != null) {
                    productCategory = product.getProductCategory();
                }


                if (invoiceRecord.getAmount() != null) {
                    invoiceRecordPrintData.setAmount(decimalFormat.format(invoiceRecord.getAmount()));
                }
                if (invoiceRecord.getCarat() != null) {
                    invoiceRecordPrintData.setCarat(decimalFormat.format(invoiceRecord.getCarat()));
                }
                if (invoiceRecord.getCount() != null) {
                    invoiceRecordPrintData.setCount(decimalFormat.format(invoiceRecord.getCount()));
                }
                if (invoiceRecord.getFee() != null) {
                    invoiceRecordPrintData.setFee(decimalFormat.format(invoiceRecord.getFee()));
                }
                if (invoiceRecord.getWeightByScale() != null) {
                    invoiceRecordPrintData.setWeight(decimalFormat.format(invoiceRecord.getWeightByScale()));
                }
                final String description = invoiceRecord.getDescription();
                invoiceRecordPrintData.setDescription(description);

                if (dealType != null) {
                    final String dealTypeMessage = message("deal.type." + dealType.name().toLowerCase());
                    String invoiceRecordTypeMessage = "";
                    if (invoiceRecordType != null) {
                        invoiceRecordTypeMessage = message("invoice.shortcut.help." + invoiceRecordType.name().toLowerCase());
                    }
                    switch (invoiceRecordType) {
                        case PRODUCT, MANUFACTURED, MELTED, COIN, CURRENCY, STONE, MSC -> {
                            if (productCategory != null) {
                                invoiceRecordPrintData.setDescription(dealTypeMessage + " " + productCategory.getTitle() + " " + product.getProductName());
                            }
                        }
                        case BANK, CASH, CHEQUE, PAYMENT_ORDER_CASH, PAYMENT_ORDER_CURRENCY, PAYMENT_ORDER_GOLD -> {
                            invoiceRecordPrintData.setDescription(dealTypeMessage + " " + invoiceRecordTypeMessage + " " + description);
                        }
                    }
                }

                invoiceRecordPrintDataList.add(invoiceRecordPrintData);
            }
        }
        return invoicePrintData;
    }

    public void recordInvoiceBtnAction(ActionEvent event) {
        doRecordInvoice(true);
    }

    public void temporaryRecordInvoiceBtnAction(ActionEvent event) {
        doRecordInvoice(false);
    }

    private void doRecordInvoice(boolean isFinalized) {
        ObservableList<InvoiceRecord> invoiceRecords = invoiceTable.getItems();

        // use this array to record so that we do not have to modify invoiceRecords and face ConcurrentModificationException
        List<InvoiceRecord> invoiceRecordList = new ArrayList<>();
        try {

            if (invoiceRecords != null) {
                // cleanup invoice records
                for (InvoiceRecord invoiceRecord : invoiceRecords) {
                    final InvoiceRecordType invoiceRecordType = invoiceRecord.getInvoiceRecordType();
                    if (invoiceRecordType != null && !invoiceRecordType.equals(InvoiceRecordType.UNDEFINED)) {
                        if (invoiceRecord.getProduct() == null && (invoiceRecord.getAmount() == null || invoiceRecord.getAmount() == 0) &&
                                (invoiceRecord.getCount() == null || invoiceRecord.getCount() == 0) &&
                                (invoiceRecord.getWeightByScale() == null || invoiceRecord.getWeightByScale() == 0) && invoiceRecord.getDealType() == null) {
                            // dont add, it is empty record
                        } else {
                            invoiceRecordList.add(invoiceRecord);
                        }
                    }
                }
            }

            if (invoiceRecordList.size() == 0) {
                throw new InvoiceValidationException("no.invoice.record");
            }
            validateInvoiceForm();

            Invoice invoice = new Invoice();
            invoice.setCreateTime(DateUtil.convertPersianStringToDate(invoiceDateInput.getText()));
            invoice.setInvoiceNumber(Integer.valueOf(invoiceNumberInput.getText()));
            invoice.setDocumentNumber(Integer.valueOf(documentNumberInput.getText()));
            invoice.setInvoiceRecords(invoiceRecordList);
            invoice.setParty(partyNameComboBox.getSelectionModel().getSelectedItem());
            invoice.setFinalized(isFinalized);

            invoiceService.createOrUpdateInvoice(invoice);
            FxUtil.info("invoice.create.success");

        } catch (InvoiceValidationException e) {
            FxUtil.error(e.getMessageKey());
            return;
        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    public void removeInvoiceBtnAction(ActionEvent event) {

    }

    private void editLastRecord(int columnIndex) {
        TableColumn<InvoiceRecord, ?> columnToEdit = invoiceTable.getColumns().get(columnIndex);
        invoiceTable.requestFocus();
        int invoiceTableSize = invoiceTable.getItems().size() - 1;
        invoiceTable.getFocusModel().focus(invoiceTableSize, columnToEdit);
        invoiceTable.edit(invoiceTableSize, columnToEdit);
    }

    private Product setProductByCommonInvoiceTypeCode(InvoiceRecordType invoiceRecordType, InvoiceRecord newInvoiceRecord) {
        Product product = null;
        try {
            switch (invoiceRecordType) {
                case CASH -> {
                    product = productService.getProductByCode(BasicProductCode.CASH.value());
                }
                case MELTED -> {
                    product = productService.getProductByCode(BasicProductCode.MELTED_GOLD.value());
                }
                case MSC -> {
                    product = productService.getProductByCode(BasicProductCode.MSC_GOLD.value());
                }
                case MANUFACTURED -> {
                    product = new Product();
                    ProductCategory manufacturedProductCategory = productCategoryService.getProductCategoryByCode(BasicProductCategoryCode.MANUFACTURED.value());
                    product.setProductCategory(manufacturedProductCategory);
                }
                case CURRENCY -> {
                    product = new Product();
                    ProductCategory manufacturedProductCategory = productCategoryService.getProductCategoryByCode(BasicProductCategoryCode.CURRENCY.value());
                    product.setProductCategory(manufacturedProductCategory);
                }
                case COIN -> {
                    product = new Product();
                    ProductCategory manufacturedProductCategory = productCategoryService.getProductCategoryByCode(BasicProductCategoryCode.COIN.value());
                    product.setProductCategory(manufacturedProductCategory);
                }
                case BANK -> {
                }
                case CHEQUE -> {
                }
                case GOLD_DEBIT_OR_DEPOSIT -> {
                }
                case CASH_DEBIT_OR_DEPOSIT -> {
                }
                case CURRENCY_DEBIT_OR_DEPOSIT -> {
                }
                case STONE -> {
                    product = new Product();
                    ProductCategory stoneProductCategory = productCategoryService.getProductCategoryByCode(BasicProductCategoryCode.STONE.value());
                    product.setProductCategory(stoneProductCategory);
                }
                case PAYMENT_ORDER_CASH, PAYMENT_ORDER_CURRENCY, PAYMENT_ORDER_GOLD -> {
                    Party paymentOrderParty = showPartySelectionDialog(null);
                }
                case DISCOUNT -> {
                }
                case MELTED_CURRENCY_TRANSACTIONS -> {
                }
                case MANUFACTURED_CURRENCY_TRANSACTIONS -> {
                }
                case CASH_TO_GOLD_CONVERSION -> {
                }
                case UNDEFINED -> {
                }
            }
            if (newInvoiceRecord != null) {
                newInvoiceRecord.setInvoiceRecordType(invoiceRecordType);
                if (product != null) {
                    newInvoiceRecord.setProduct(product);
                }
            }

        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }
        return product;
    }

    private void loadPreviousBalanceData() {
        try {
            final Party party = partyNameComboBox.getSelectionModel().getSelectedItem();
            if (party != null) {
                final List<AccountBalance> accountBalanceList = accountBalanceService.getAccountBalanceByParty(party);
                previousBalanceTable.getItems().clear();
                previousBalanceTable.setItems(FXCollections.observableList(accountBalanceList));
            }
        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    private void loadInvoiceBalanceData() {
        final ObservableList<InvoiceRecord> invoiceTableItems = invoiceTable.getItems();
        List<AccountBalance> accountBalanceList = new ArrayList<>();
        if (invoiceTableItems != null) {
            for (InvoiceRecord invoiceRecord : invoiceTableItems) {
                final Integer dealTypeValue = invoiceRecord.getDealType();
                final DealType dealType = DealType.getByValue(dealTypeValue);
                if (dealType != null) {
                    switch (dealType) {
                        case PURCHASE_BY_CASH, SELL_BY_CASH -> {
                            AccountBalance accountBalance = new AccountBalance();
                            accountBalance.setProduct(invoiceRecord.getProduct());
                            accountBalance.setAmount(invoiceRecord.getWeightByScale());
                            if (invoiceRecord.getCount() != null) {
                                accountBalance.setCount(Double.valueOf(invoiceRecord.getCount()));
                            }
                            accountBalanceList.add(accountBalance);
                        }
                    }
                }
            }
        }
        invoiceBalanceTable.getItems().setAll(accountBalanceList);
        invoiceBalanceTable.refresh();
    }

    private void loadFinalBalanceData() {
        final ObservableList<AccountBalance> previousBalanceTableItems = previousBalanceTable.getItems();
        final ObservableList<AccountBalance> invoiceBalanceTableItems = invoiceBalanceTable.getItems();
        List<AccountBalance> finalBalanceList = new ArrayList<>();

        if (invoiceBalanceTableItems != null) {
            for (AccountBalance currentAccountBalance : invoiceBalanceTableItems) {
                final Product product = currentAccountBalance.getProduct();
                final AccountBalance previousBalance = findAccountBalanceByProduct(product, previousBalanceTableItems);
                if (previousBalance != null) {
                    AccountBalance finalBalance = new AccountBalance();
                    finalBalance.setProduct(product);
                    Double currentAccountBalanceAmount = currentAccountBalance.getAmount();
                    if (currentAccountBalanceAmount == null) {
                        currentAccountBalanceAmount = 0.0;
                    }
                    Double previousBalanceAmount = previousBalance.getAmount();
                    if (previousBalanceAmount == null) {
                        previousBalanceAmount = 0.0;
                    }

                    Double currentAccountBalanceCount = currentAccountBalance.getCount();
                    if (currentAccountBalanceCount == null) {
                        currentAccountBalanceCount = 0.0;
                    }
                    Double previousBalanceCount = previousBalance.getCount();
                    if (previousBalanceCount == null) {
                        previousBalanceCount = 0.0;
                    }

                    finalBalance.setAmount(currentAccountBalanceAmount + previousBalanceAmount);
                    finalBalance.setCount(currentAccountBalanceCount + previousBalanceCount);
                    finalBalanceList.add(finalBalance);
                }
            }
        }
        finalBalanceTable.getItems().setAll(finalBalanceList);
    }

    private AccountBalance findAccountBalanceByProduct(Product product, ObservableList<AccountBalance> accountBalances) {
        if (accountBalances != null) {
            for (AccountBalance accountBalance : accountBalances) {
                final Product accountBalanceProduct = accountBalance.getProduct();
                if (accountBalanceProduct != null && accountBalanceProduct.equals(product)) {
                    return accountBalance;
                }
            }
        }
        return null;
    }

    private void refreshTables() {

        final TableColumn<InvoiceRecord, ?> weightByPercentageColumn = invoiceTable.getColumns().get(COLUMN_INDEX.weightByPercentage);
        final ObservableList<InvoiceRecord> invoiceTableItems = invoiceTable.getItems();
        if (invoiceTableItems != null) {
            final int invoiceTableItemsSize = invoiceTableItems.size();
            Double totalPercentageValue = 0.0;
            for (int i = 0; i < invoiceTableItemsSize; i++) {
                final ObservableValue<?> cellObservableValue = weightByPercentageColumn.getCellObservableValue(i);
                if (cellObservableValue != null) {
                    try {
                        totalPercentageValue += Double.parseDouble(String.valueOf(cellObservableValue.getValue()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            regardPercentInput.setText(String.valueOf(totalPercentageValue));


            Double totalStonePrice = 0.0;
            for (InvoiceRecord invoiceRecord : invoiceTableItems) {
                final Collection<Stone> stones = invoiceRecord.getStones();
                if (stones != null) {
                    for (Stone stone : stones) {
                        totalStonePrice += stone.getPrice();
                    }
                }
            }
            invoiceStonesInput.setText(String.valueOf(totalStonePrice));
        }

        // TODO: maybe move loadPreviousBalanceData to a more appropriate place
        loadPreviousBalanceData();
        loadInvoiceBalanceData();
        loadFinalBalanceData();

        invoiceTable.refresh();
        invoiceTable.layout();
        previousBalanceTable.refresh();
        invoiceBalanceTable.refresh();
        finalBalanceTable.refresh();
        totalTradesTable.refresh();
    }

}
