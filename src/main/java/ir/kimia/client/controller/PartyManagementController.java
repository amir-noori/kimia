package ir.kimia.client.controller;

import com.google.common.eventbus.Subscribe;
import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.common.BasicProductCode;
import ir.kimia.client.common.UserSession;
import ir.kimia.client.controller.model.BaseBalance;
import ir.kimia.client.controller.model.CoinBalance;
import ir.kimia.client.controller.model.CurrencyBalance;
import ir.kimia.client.data.model.*;
import ir.kimia.client.event.BeginningOfCycleCoinEvent;
import ir.kimia.client.event.BeginningOfCycleCurrencyEvent;
import ir.kimia.client.service.api.*;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.ComboBoxAutoComplete;
import ir.kimia.client.ui.PartyStringConverter;
import ir.kimia.client.ui.PartyTypeStringConverter;
import ir.kimia.client.util.FxUtil;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@FxAppScoped
public class PartyManagementController extends BaseController {

    private static final Logger log = LogManager.getLogger(PartyManagementController.class);

    private final StockService stockService;
    private final PartyService partyService;
    private final AddressService addressService;
    private final ProductCategoryService productCategoryService;
    private final ProductService productService;
    private final AccountBalanceService accountBalanceService;

    private List<CoinBalance> coinBalances;
    private List<CurrencyBalance> currencyBalances;

    public ComboBox<Party.PartyType> partyTypeCombobox;
    public TextField partyCodeInput;
    public TextField secondMobileInput;
    public TextField secondPhoneNumberInput;
    public TextField firstMobileInput;
    public TextField firstPhoneNumberInput;
    public TextField moneyDebitInput;
    public TextField moneyCreditInput;
    public TextField goldCreditInput;
    public TextField goldDebitInput;
    public TextArea addressTextArea;
    public ComboBox<Party> partyCombobox;
    public ImageView partyImageView;
    public Button createBtn;
    public Button editBtn;
    public Button removeBtn;
    public Button currencyBtn;
    public Button coinBtn;
    public Button uploadPhotoBtn;
    public Button removePhotoBtn;

    List<Party> partiesByOffice;
    ComboBoxAutoComplete partyAutoCompleteCombobox;

    @Inject
    public PartyManagementController(StockService stockService, PartyService partyService, AddressService addressService, ProductCategoryService productCategoryService,
                                     ProductService productService, AccountBalanceService accountBalanceService) {
        this.partyService = partyService;
        this.stockService = stockService;
        this.addressService = addressService;
        this.productCategoryService = productCategoryService;
        this.productService = productService;
        this.accountBalanceService = accountBalanceService;
    }

    protected ValidationResult validatePartyCode(Control control, String input) {
        boolean validationFailed = false;
        Party.PartyType selectedPartyType = partyTypeCombobox.getSelectionModel().getSelectedItem();
        String validationMessageKey = null;
        if (selectedPartyType == null) {
            validationFailed = true;
            validationMessageKey = "party.type.must.be.selected";
        } else {
            try {
                int partyCode = Integer.valueOf(input);
                switch (selectedPartyType) {
                    case BANK -> {
                        // BANK code can be between 5000 to 5999
                        if (partyCode < 5000 || partyCode >= 6000) {
                            validationFailed = true;
                            validationMessageKey = "bank.range.must.be.5000.to.5999";
                        }
                    }
                    case CUSTOMER -> {

                        // CUSTOMER code can be 1000 or 2000 for many customers
                        if (partyCode != 1000 && partyCode != 2000) {
                            // CUSTOMER code can be between 7000 to 7999
                            if (partyCode < 7000 || partyCode >= 8000) {
                                validationFailed = true;
                                validationMessageKey = "customer.range.must.be.7000.to.7999";
                            }
                        }
                    }
                    case INDIVIDUAL -> {
                        // INDIVIDUAL code can be between 3000 to 3999
                        if (partyCode < 3000 || partyCode >= 4000) {
                            validationFailed = true;
                            validationMessageKey = "individual.range.must.be.3000.to.3999";
                        }
                    }
                    case MANUFACTURER -> {
                        // MANUFACTURER code can be between 6000 to 6999
                        if (partyCode < 6000 || partyCode >= 7000) {
                            validationFailed = true;
                            validationMessageKey = "manufacturer.range.must.be.6000.to.6999";
                        }
                    }
                }
            } catch (NumberFormatException e) {
                validationFailed = true;
                validationMessageKey = "must.be.integer";
            }
        }
        isFormDataValid = !validationFailed;
        String validationMessage = validationMessageKey != null ? message(validationMessageKey) : "";
        return ValidationResult.fromMessageIf(control, validationMessage, Severity.ERROR, validationFailed);
    }

    @Override
    public void postLoad() {
        super.postLoad();
        partyAutoCompleteCombobox = new ComboBoxAutoComplete<>(partyCombobox);
        goldDebitInput.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String text = goldDebitInput.getText();
                try {
                    double doubleValue = Double.parseDouble(text);
                    if (doubleValue > 0) {
                        goldCreditInput.setText("");
                        moneyDebitInput.requestFocus();
                    }
                } catch (NumberFormatException e) {
                    goldCreditInput.requestFocus();
                }
            }
        });
        goldCreditInput.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String text = goldCreditInput.getText();
                try {
                    double doubleValue = Double.parseDouble(text);
                    if (doubleValue > 0) {
                        goldDebitInput.setText("");
                    }
                } catch (NumberFormatException e) {
                    goldDebitInput.requestFocus();
                }
                moneyDebitInput.requestFocus();
            }
        });
        moneyDebitInput.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String text = moneyDebitInput.getText();
                try {
                    double doubleValue = Double.parseDouble(text);
                    if (doubleValue > 0) {
                        moneyCreditInput.setText("");
                        createBtn.requestFocus();
                    }
                } catch (NumberFormatException e) {
                    if (StringUtils.isNotEmpty(text)) {
                        moneyDebitInput.requestFocus();
                    } else {
                        moneyCreditInput.requestFocus();
                    }
                }
            }
        });
        moneyCreditInput.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String text = moneyCreditInput.getText();
                try {
                    double doubleValue = Double.parseDouble(text);
                    if (doubleValue > 0) {
                        moneyDebitInput.setText("");
                        createBtn.requestFocus();
                    }
                } catch (NumberFormatException e) {
                    if (StringUtils.isEmpty(text)) {
                        createBtn.requestFocus();
                    } else {
                        moneyCreditInput.requestFocus();
                    }
                }
            }
        });

    }

    @Override
    protected void setShortcuts() {
        super.setShortcuts();
        if (stage != null) {
            Scene scene = stage.getScene();
            scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
                if (keyEvent.isControlDown()) {
                    switch (keyEvent.getCode().getChar()) {
                        case "H" -> {
                            resetForm(null);
                            setPartyComboBoxData(null);
                            partyCombobox.requestFocus();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {

        validationSupport.registerValidator(partyCodeInput, this::validatePartyCode);
        validationSupport.registerValidator(firstMobileInput, this::validateMobileNumber);
        validationSupport.registerValidator(secondMobileInput, this::validateMobileNumber);
        validationSupport.registerValidator(firstPhoneNumberInput, this::validatePhoneNumber);
        validationSupport.registerValidator(secondPhoneNumberInput, this::validatePhoneNumber);
        validationSupport.registerValidator(moneyCreditInput, this::validateDouble);
        validationSupport.registerValidator(moneyDebitInput, this::validateDouble);
        validationSupport.registerValidator(goldCreditInput, this::validateDouble);
        validationSupport.registerValidator(goldDebitInput, this::validateDouble);


        coinBalances = null;
        currencyBalances = null;

        partyTypeCombobox.getItems().addAll(Party.PartyType.values());
        partyTypeCombobox.setConverter(new PartyTypeStringConverter());
        partyTypeCombobox.setOnAction(this::setPartyComboBoxData);
        partyTypeCombobox.getSelectionModel().selectFirst();
        setPartyComboBoxData(null);


        partyCombobox.setConverter(new PartyStringConverter(partiesByOffice));

        partyCombobox.setOnAction(event -> {
            try {
                Party.PartyType partyType = partyTypeCombobox.getSelectionModel().getSelectedItem();
                if (partyType != null) {
                    onPartySelect();
                    if (partyType == Party.PartyType.EXPENSE) {
                        disableFormInputsOnExpenseSelect(true);
                    } else if (partyType == Party.PartyType.BANK) {
                        disableFormInputsOnBankSelect(true);
                    }
                } else {
                    FxUtil.warning("please.select.party.type");
                }
            } catch (SQLException sqlException) {
                FxUtil.exceptionOccurred(sqlException);
            }
        });

        resetFocusNodes(false);

        partyCodeInput.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            /*
                partyCodeInput is not part of focus group and if it is inserted manually then after pressing enter
                the focus will be set on the next input.
             */
            if (event.getCode() == KeyCode.ENTER) {
                partyCombobox.requestFocus();
            }
        });

    }

    private void onPartySelect() throws SQLException {
        Party selectedParty = partyCombobox.getSelectionModel().getSelectedItem();
        if (selectedParty != null && selectedParty.getCode() != null) {
            Party retrievedParty = null;
            retrievedParty = partyService.getPartyByCode(selectedParty.getCode());
            partyCodeInput.setText(retrievedParty.getCode());
            firstPhoneNumberInput.setText(retrievedParty.getFirstPhoneNumber());
            secondPhoneNumberInput.setText(retrievedParty.getSecondPhoneNumber());
            firstMobileInput.setText(retrievedParty.getFirstMobileNumber());
            secondMobileInput.setText(retrievedParty.getSecondMobileNumber());
            if (retrievedParty.getPartyAddress() != null) {
                addressTextArea.setText(retrievedParty.getPartyAddress().getAddress());
            } else {
                addressTextArea.setText("");
            }
            resetFocusNodes(true);

        } else if (selectedParty != null && StringUtils.isNotEmpty(selectedParty.getPartyName())) {
            String partyCodeInputText = partyCodeInput.getText();
            Integer nextCode = null;
            if (StringUtils.isEmpty(partyCodeInputText)) {
                nextCode = Integer.valueOf(partyService.getNextAvailableCode(partyTypeCombobox.getSelectionModel().getSelectedItem()));
            } else {
                if (partyCodeInputText.equals("1000") || partyCodeInputText.equals("2000")) {
                    nextCode = Integer.valueOf(partyCodeInputText); // value 1000 and 2000 are always allowed for customer.
                } else {
                    Party partyByCode = partyService.getPartyByCode(partyCodeInputText);
                    if (partyByCode != null) {
                        FxUtil.error("party.code.already.exists");
                        partyCodeInput.requestFocus();
                        return;
                    } else {
                        nextCode = Integer.valueOf(partyCodeInputText);
                    }
                }
            }

            Party newParty = new Party();
            newParty.setCode(String.valueOf(nextCode));
            if (selectedParty != null) {
                newParty.setPartyName(selectedParty.getPartyName());
            }
            partyCodeInput.setText(String.valueOf(nextCode));
            resetFocusNodes(false);
        }
    }

    private void resetFocusNodes(boolean editMode) {
        if (editMode) {
            setFocusNodes(partyTypeCombobox, partyCombobox, firstMobileInput, secondMobileInput,
                    firstPhoneNumberInput, secondPhoneNumberInput, addressTextArea, editBtn);
            goldDebitInput.setDisable(true);
            moneyDebitInput.setDisable(true);
            goldCreditInput.setDisable(true);
            moneyCreditInput.setDisable(true);
            currencyBtn.setDisable(true);
            coinBtn.setDisable(true);
        } else {
            setFocusNodes(partyTypeCombobox, partyCombobox, firstMobileInput, secondMobileInput,
                    firstPhoneNumberInput, secondPhoneNumberInput, addressTextArea, goldDebitInput, moneyDebitInput,
                    goldCreditInput, moneyCreditInput, createBtn);
            goldDebitInput.setDisable(false);
            moneyDebitInput.setDisable(false);
            goldCreditInput.setDisable(false);
            moneyCreditInput.setDisable(false);
            currencyBtn.setDisable(false);
            coinBtn.setDisable(false);
        }
    }

    private void disableFormInputsOnExpenseSelect(boolean disable) {
        firstMobileInput.setDisable(disable);
        secondMobileInput.setDisable(disable);
        moneyCreditInput.setDisable(disable);
        moneyDebitInput.setDisable(disable);
        goldCreditInput.setDisable(disable);
        goldDebitInput.setDisable(disable);
        addressTextArea.setDisable(disable);
        coinBtn.setDisable(disable);
        currencyBtn.setDisable(disable);
        uploadPhotoBtn.setDisable(disable);
        removePhotoBtn.setDisable(disable);
        firstPhoneNumberInput.setDisable(disable);
        secondPhoneNumberInput.setDisable(disable);
    }

    private void disableFormInputsOnBankSelect(boolean disable) {
        goldCreditInput.setDisable(disable);
        goldDebitInput.setDisable(disable);
        moneyCreditInput.setDisable(disable);
        addressTextArea.setDisable(disable);
        coinBtn.setDisable(disable);
        currencyBtn.setDisable(disable);
        firstPhoneNumberInput.setDisable(disable);
        secondPhoneNumberInput.setDisable(disable);
    }

    private void setPartyComboBoxData(ActionEvent event) {
        UserSession userSession = ApplicationContext.getUserSession();
        if (userSession != null) {
            try {
                Party.PartyType selectedPartyType = partyTypeCombobox.getSelectionModel().getSelectedItem();
                if (selectedPartyType != null) {
                    partiesByOffice = partyService.getPartiesByOfficeAndPartyType(userSession.getCurrentOffice(), selectedPartyType);

                    if (selectedPartyType.equals(Party.PartyType.EXPENSE)) {
                        disableFormInputsOnExpenseSelect(true);
                    } else if(selectedPartyType.equals(Party.PartyType.BANK)) {
                        disableFormInputsOnBankSelect(true);
                    } else {
                        disableFormInputsOnExpenseSelect(false);
                    }

                }
            } catch (SQLException sqlException) {
                FxUtil.exceptionOccurred(sqlException);
            }
            partyCombobox.getItems().clear();
            partyCodeInput.clear();
            partyCombobox.getItems().addAll(partiesByOffice);
            /*
                each time we create a new partyAutoCompleteCombobox from partyCombobox to add data to partyCombobox
                because ComboBoxAutoComplete handleOnHiding will remove all items by setting combobox originalItems which are null.
             */
            partyAutoCompleteCombobox = new ComboBoxAutoComplete<>(partyCombobox);
        }
    }

    public void showCurrencyDialog(ActionEvent event) {
        showDialog("currencyPopup", 700, 650, Modality.APPLICATION_MODAL, controller -> {
            ((CurrencyPopupController) controller).setAccountBalanceList(currencyBalances);
            return null;
        });
    }

    public void showCoinDialog(ActionEvent event) {
        showDialog("coinPopup", 700, 650, Modality.APPLICATION_MODAL, controller -> {
            ((CoinPopupController) controller).setAccountBalanceList(coinBalances);
            return null;
        });
    }

    public void createParty(ActionEvent event) throws SQLException {

        if (!isFormDataValid) {
            FxUtil.error("input.data.invalid");
            return;
        }

        Party selectedParty = partyCombobox.getSelectionModel().getSelectedItem();
        if (selectedParty == null || StringUtils.isEmpty(partyCodeInput.getText()) || StringUtils.isEmpty(selectedParty.getPartyName())) {
            FxUtil.error("input.data.invalid");
            partyCombobox.requestFocus();
            return;
        }

        Party.PartyType partyType = partyTypeCombobox.getSelectionModel().getSelectedItem();
        if (partyType == null) {
            FxUtil.error("please.select.party.type");
            return;
        }

        if (partyType.equals(Party.PartyType.BANK)) {
            showChequeInitialDataDialog();
        }

        Address address = new Address();
        address.setAddress(addressTextArea.getText());

        Party party = new Party();
        party.setPartyName(selectedParty.getPartyName());
        party.setCode(partyCodeInput.getText());
        party.setPartyAddress(address);
        party.setPartyType(partyType.ordinal());
        party.setOffice(ApplicationContext.getUserSession().getCurrentOffice());
        party.setFirstMobileNumber(firstMobileInput.getText());
        party.setSecondMobileNumber(secondMobileInput.getText());
        party.setFirstPhoneNumber(firstPhoneNumberInput.getText());
        party.setSecondPhoneNumber(secondPhoneNumberInput.getText());

        Party createdParty = partyService.createParty(party);

        setInitialBalanceData(createdParty);
        FxUtil.info("party.create.success");
        resetForm(null);
        setPartyComboBoxData(null);
        partyCombobox.requestFocus();
    }

    private void showChequeInitialDataDialog() {
        Dialog chequeInitialDataDialog = new Dialog<>();
        chequeInitialDataDialog.setHeaderText(message("cheque.initial.data"));

        ButtonType confirmButtonType = new ButtonType(message("confirm"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(message("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        chequeInitialDataDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField bankPartyCode = new TextField();
        bankPartyCode.setPromptText(message("party.code"));
        bankPartyCode.setText(partyCodeInput.getText());
        bankPartyCode.setEditable(false);
        TextField bankName = new TextField();
        bankName.setPromptText(message("bank"));
        bankName.setText(partyCombobox.getSelectionModel().getSelectedItem().getPartyName());
        bankName.setEditable(false);

        TextField fromInput = new TextField();
        fromInput.setPromptText(message("from"));
        TextField toInput = new TextField();
        toInput.setPromptText(message("to"));

        grid.add(new Label(message("party.code")), 3, 0);
        grid.add(bankPartyCode, 2, 0);
        grid.add(new Label(message("bank")), 1, 0);
        grid.add(bankName, 0, 0);

        grid.add(new Label(message("from")), 3, 1);
        grid.add(fromInput, 2, 1);
        grid.add(new Label(message("to")), 1, 1);
        grid.add(toInput, 0, 1);

        chequeInitialDataDialog.getDialogPane().setContent(grid);

        chequeInitialDataDialog.getDialogPane().lookupButton(confirmButtonType).addEventFilter(ActionEvent.ACTION, event -> {
            try {
                String fromInputText = fromInput.getText();
                String toInputText = toInput.getText();
                if (StringUtils.isEmpty(fromInputText) && StringUtils.isEmpty(toInputText)) {
                    return;
                }
                if (StringUtils.isEmpty(fromInputText) || StringUtils.isEmpty(toInputText)) {
                    throw new NumberFormatException();
                }

                // TODO: persist in database
                double from = Double.parseDouble(fromInputText);
                double to = Double.parseDouble(toInputText);

            } catch (NumberFormatException e) {
                FxUtil.error("must.be.integer");
                event.consume();
            }
        });

        Optional result = chequeInitialDataDialog.showAndWait();
        if (result.isPresent()) {
            ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
            if (buttonData.equals(ButtonBar.ButtonData.OK_DONE)) {

            } else if (buttonData.equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {

            }
        }

    }

    /**
     * create balance for a newly created party.
     *
     * @param createdParty: the party to create balance for
     * @throws SQLException
     */
    private void setInitialBalanceData(Party createdParty) throws SQLException {

        List<AccountBalance> accountBalanceList = new ArrayList<>();
        List<AccountBalance> coinBalanceObjects = new ArrayList<>();
        List<AccountBalance> currencyBalanceObjects = new ArrayList<>();
        if (coinBalances != null) {
            coinBalanceObjects = createAccountObject(coinBalances, createdParty);
        }
        if (currencyBalances != null) {
            currencyBalanceObjects = createAccountObject(currencyBalances, createdParty);
        }
        accountBalanceList.addAll(coinBalanceObjects);
        accountBalanceList.addAll(currencyBalanceObjects);

        String goldDebitInputText = goldDebitInput.getText();
        String goldCreditInputText = goldCreditInput.getText();
        if (!StringUtils.isEmpty(goldDebitInputText) || !StringUtils.isEmpty(goldCreditInputText)) {
            Double goldDebit = 0.0;
            Double goldCredit = 0.0;
            try {
                goldDebit = Double.valueOf(goldDebitInputText);
            } catch (NumberFormatException ignored) {
            }
            try {
                goldCredit = Double.valueOf(goldCreditInputText);
            } catch (NumberFormatException ignored) {
            }

            Double amount = goldCredit - goldDebit;

            AccountBalance goldBalance = new AccountBalance();
            goldBalance.setAmount(amount);
            goldBalance.setParty(createdParty);
            // TODO: make sure MELTED_GOLD should be used here or MSC_GOLD
            Product goldProduct = productService.getProductByCode(BasicProductCode.MELTED_GOLD.value());
            goldBalance.setProduct(goldProduct);

            accountBalanceList.add(goldBalance);
        }


        String moneyDebitInputText = moneyDebitInput.getText();
        String moneyCreditInputText = moneyCreditInput.getText();
        double moneyDebit = 0;
        double moneyCredit = 0;
        try {
            moneyDebit = Double.parseDouble(moneyDebitInputText);
        } catch (NumberFormatException ignored) {
        }
        try {
            moneyCredit = Double.parseDouble(moneyCreditInputText);
        } catch (NumberFormatException ignored) {
        }

        Double amount = moneyCredit - moneyDebit;

        AccountBalance moneyBalance = new AccountBalance();
        moneyBalance.setCount(amount);
        moneyBalance.setParty(createdParty);
        Product moneyProduct = productService.getProductByCode(BasicProductCode.CASH.value());
        moneyBalance.setProduct(moneyProduct);

        accountBalanceList.add(moneyBalance);

        accountBalanceService.createOrUpdate(accountBalanceList);
    }

    private List<AccountBalance> createAccountObject(List<? extends BaseBalance> accountBalanceList, Party createdParty) {
        List<AccountBalance> result = new ArrayList<>();
        for (BaseBalance balanceObject : accountBalanceList) {
            AccountBalance accountBalance = new AccountBalance();
            accountBalance.setProduct(balanceObject.getProduct());
            Integer creditCount = balanceObject.getCreditCount() != null ? balanceObject.getCreditCount() : 0;
            Integer debitCount = balanceObject.getDebitCount() != null ? balanceObject.getDebitCount() : 0;
            double amount = creditCount - debitCount;
            accountBalance.setCount(amount); // setCount for coin balance
            accountBalance.setParty(createdParty);
            result.add(accountBalance);
        }
        return result;
    }


    public void editParty(ActionEvent event) throws SQLException {
        if (!isFormDataValid) {
            FxUtil.error("input.data.invalid");
            return;
        }

        Party party = partyService.getPartyByCode(partyCodeInput.getText());
        Address address = party.getPartyAddress();
        if (address == null) {
            address = new Address();
        }
        address.setAddress(addressTextArea.getText());

        party.setPartyAddress(address);
        party.setCode(partyCodeInput.getText());
        party.setFirstMobileNumber(firstMobileInput.getText());
        party.setSecondMobileNumber(secondMobileInput.getText());
        party.setFirstPhoneNumber(firstPhoneNumberInput.getText());
        party.setSecondPhoneNumber(secondPhoneNumberInput.getText());

        boolean confirmed = FxUtil.confirm("confirm.edit");
        if(confirmed) {
            partyService.updateParty(party);
            FxUtil.info("party.edit.success");
            resetForm(null);
            partyTypeCombobox.requestFocus();
        }
    }

    public void removeParty(ActionEvent event) {
        // TODO: should this be allowed?
    }

    @Subscribe
    public void onCurrencyEvent(BeginningOfCycleCurrencyEvent beginningOfCycleCurrencyEvent) {
        currencyBalances = beginningOfCycleCurrencyEvent.getCurrencyStocks();
    }

    @Subscribe
    public void onCoinEvent(BeginningOfCycleCoinEvent beginningOfCycleCoinEvent) {
        coinBalances = beginningOfCycleCoinEvent.getCoinStocks();
    }

    public void resetForm(ActionEvent event) {
        partyCodeInput.clear();
        partyCombobox.getSelectionModel().clearSelection();
        partyCombobox.getEditor().setText("");
        firstMobileInput.clear();
        secondMobileInput.clear();
        firstPhoneNumberInput.clear();
        secondPhoneNumberInput.clear();
        moneyDebitInput.clear();
        goldDebitInput.clear();
        moneyCreditInput.clear();
        goldCreditInput.clear();
        addressTextArea.clear();
        coinBalances = null;
        currencyBalances = null;
        resetFocusNodes(false);
    }

    public void uploadPhoto(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            // using primary stage instead of current stage to prevent current stage to be closed if ESCAPE key pressed.
            File selectedFile = fileChooser.showOpenDialog(ApplicationContext.getPrimaryStage());
            if (selectedFile != null) {
                Image image = new Image(new FileInputStream(selectedFile));
                partyImageView.setImage(image);
            }
        } catch (FileNotFoundException e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    public void removePhoto(ActionEvent event) {
        partyImageView.setImage(null);
    }
}
