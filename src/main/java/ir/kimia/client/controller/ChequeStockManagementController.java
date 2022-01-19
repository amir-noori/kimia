package ir.kimia.client.controller;

import ir.kimia.client.data.model.Bank;
import ir.kimia.client.data.model.Cheque;
import ir.kimia.client.data.model.Stock;
import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.api.BankService;
import ir.kimia.client.service.api.ChequeService;
import ir.kimia.client.service.api.PartyService;
import ir.kimia.client.service.api.StockService;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.ActionButtonTableCell;
import ir.kimia.client.ui.BankStringConverter;
import ir.kimia.client.ui.FormattedDoubleTextField;
import ir.kimia.client.util.DateUtil;
import ir.kimia.client.util.FxUtil;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

@FxAppScoped
public class ChequeStockManagementController extends BaseController {


    private final ChequeService chequeService;
    private final StockService stockService;
    private final BankService bankService;
    private final PartyService partyService;

    @FXML
    private TableView<Cheque> chequeTableView;
    @FXML
    private RadioButton partyChequeRadioBtn;
    @FXML
    private RadioButton officeInventoryChequeRadioBtn;
    @FXML
    private Button createChequeBtn;
    @FXML
    private ComboBox<Bank> bankCombobox;
    @FXML
    private TextField chequeNumberInput;
    @FXML
    private TextField dueDateInput;
    @FXML
    private FormattedDoubleTextField amountInput;
    @FXML
    private TextField descriptionInput;
    @FXML
    private TextField bankCodeInput;

    private List<Bank> allBanks;
    private List<Cheque> allCheques;


    private Callback<TableColumn.CellDataFeatures<Cheque, String>, ObservableValue<String>> getChequeCellValueFactory(String cellName) {
        return param -> {
            Cheque cheque = param.getValue();
            return new ObjectBinding<>() {
                @Override
                protected String computeValue() {
                    if (cheque != null) {
                        switch (cellName) {
                            case "chequeType" -> {
                                Boolean inOfficeInventory = cheque.getInOfficeInventory();
                                if (inOfficeInventory != null && inOfficeInventory) {
                                    return message("office.inventory.cheque");
                                } else {
                                    return message("party.cheque");
                                }
                            }
                            case "bankCode" -> {
                                return cheque.getBank().getCode();
                            }
                            case "bankName" -> {
                                return cheque.getBank().getBankName();
                            }
                            case "chequeNumber" -> {
                                return cheque.getChequeNumber();
                            }
                            case "dueDate" -> {
                                // TODO: return formatted date
                                Date dueDate = cheque.getDueDate();
                                if (dueDate != null) {
                                    return DateUtil.convertDateToPersianString(dueDate);
                                } else {
                                    return "";
                                }
                            }
                            case "description" -> {
                                return cheque.getDescription();
                            }
                            case "amount" -> {
                                Double amount = cheque.getAmount();
                                if (amount != null) {
                                    DecimalFormat df = new DecimalFormat("0");
                                    df.setMaximumFractionDigits(10);
                                    return df.format(amount);
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
    public ChequeStockManagementController(ChequeService chequeService, StockService stockService, BankService bankService, PartyService partyService) {
        this.chequeService = chequeService;
        this.stockService = stockService;
        this.bankService = bankService;
        this.partyService = partyService;
    }


    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException, ApplicationException {
        super.init(url, resourceBundle);

        allBanks = bankService.getAllBanks();
        bankCombobox.getItems().addAll(allBanks);

        bankCombobox.setConverter(new BankStringConverter(allBanks));

        bankCombobox.setOnAction(event -> {
            Bank selectedBank = bankCombobox.getSelectionModel().getSelectedItem();
            if (selectedBank != null) {
                bankCodeInput.setText(selectedBank.getCode());
            }
        });

        setupChequeTable();
        resetForm();
    }

    private void setupChequeTable() throws SQLException {

        var chequeTypeColumn = new TableColumn<Cheque, String>(message("chequeType"));
        chequeTypeColumn.setCellValueFactory(getChequeCellValueFactory("chequeType"));

        var bankCodeColumn = new TableColumn<Cheque, String>(message("bank.code"));
        bankCodeColumn.setCellValueFactory(getChequeCellValueFactory("bankCode"));

        var bankNameColumn = new TableColumn<Cheque, String>(message("bank"));
        bankNameColumn.setCellValueFactory(getChequeCellValueFactory("bankName"));

        var chequeNumberColumn = new TableColumn<Cheque, String>(message("cheque.number"));
        chequeNumberColumn.setCellValueFactory(getChequeCellValueFactory("chequeNumber"));

        var dueDateColumn = new TableColumn<Cheque, String>(message("due.date"));
        dueDateColumn.setCellValueFactory(getChequeCellValueFactory("dueDate"));

        var descriptionColumn = new TableColumn<Cheque, String>(message("regard"));
        descriptionColumn.setMinWidth(300);
        descriptionColumn.setCellValueFactory(getChequeCellValueFactory("description"));

        var amountColumn = new TableColumn<Cheque, String>(message("money.amount"));
        amountColumn.setMinWidth(150);
        amountColumn.setCellValueFactory(getChequeCellValueFactory("amount"));

        TableColumn<Cheque, Button> deleteColumn = new TableColumn<>(message("remove"));
        deleteColumn.setCellFactory(ActionButtonTableCell.<Cheque>forTableColumn(message("remove"), (Cheque targetCheque) -> {
            boolean confirmed = FxUtil.confirm("remove.confirm");
            if (confirmed) {
                removeCheque(targetCheque);
            }
            return targetCheque;
        }));

        chequeTableView.getColumns().addAll(chequeTypeColumn, bankCodeColumn, bankNameColumn, chequeNumberColumn, dueDateColumn, descriptionColumn, amountColumn, deleteColumn);
        allCheques = chequeService.getAllCheques();
        chequeTableView.getItems().addAll(allCheques);
    }

    private void removeCheque(Cheque cheque) {
        try {
            chequeService.removeCheque(cheque);
            chequeTableView.getItems().remove(cheque);
        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    @Override
    public void postLoad() {
        super.postLoad();
        setupSceneShortcuts();
    }

    private void resetForm() {
        bankCombobox.getSelectionModel().clearSelection();
        dueDateInput.setText(DateUtil.getCurrentPersianDateString());
        amountInput.clear();
        descriptionInput.clear();
        chequeNumberInput.clear();
        setFocusNodes(partyChequeRadioBtn, officeInventoryChequeRadioBtn, bankCombobox, chequeNumberInput, dueDateInput, amountInput, descriptionInput, createChequeBtn);
        bankCombobox.getSelectionModel().selectFirst();
        partyChequeRadioBtn.setSelected(true);
        partyChequeRadioBtn.requestFocus();
    }

    public void onCreateAction(ActionEvent event) {

        try {

            Double amount = 0.0;
            try {
                amount = Double.valueOf(amountInput.getValue());
            } catch (NumberFormatException e) {
                FxUtil.error("cheque.amount.is.invalid");
                amountInput.requestFocus();
                return;
            }

            String chequeNumber = chequeNumberInput.getText();
            try {
                if (chequeService.doesChequeNumberExists(chequeNumber)) {
                    FxUtil.error("cheque.number.already.exists");
                    return;
                }

                Integer.valueOf(chequeNumber);
            } catch (NumberFormatException e) {
                FxUtil.error("cheque.number.is.invalid");
                chequeNumberInput.requestFocus();
                return;
            }

            String description = descriptionInput.getText();
            if (StringUtils.isEmpty(description)) {
                FxUtil.error("cheque.description.required");
                descriptionInput.requestFocus();
                return;
            }

            String dueDateText = dueDateInput.getText();
            Date dueDate;
            if (StringUtils.isEmpty(dueDateText)) {
                FxUtil.error("cheque.due.date.required");
                dueDateInput.requestFocus();
                return;
            } else {
                try {
                    dueDate = DateUtil.convertPersianStringToDate(dueDateText);

                } catch (DateTimeParseException e) {
                    FxUtil.error("cheque.due.date.invalid");
                    dueDateInput.requestFocus();
                    return;
                }
            }


            Cheque chequeToBeSaved = new Cheque();
            chequeToBeSaved.setAmount(amount);
            chequeToBeSaved.setDueDate(dueDate);
            chequeToBeSaved.setBank(bankCombobox.getSelectionModel().getSelectedItem());
            chequeToBeSaved.setChequeNumber(chequeNumber);
            chequeToBeSaved.setInOfficeInventory(officeInventoryChequeRadioBtn.isSelected());
            chequeToBeSaved.setParty(null);
            chequeToBeSaved.setDescription(description);
            chequeService.createCheque(chequeToBeSaved);
            chequeTableView.getItems().add(chequeToBeSaved);
            resetForm();
        } catch (SQLException e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    private void setupSceneShortcuts() {
        chequeTableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.isControlDown()) {
                switch (keyEvent.getCode().getChar()) {
                    case "N" -> {
                        // add a new row
                    }
                    case "D" -> {
                        // delete a row
                        Cheque focusedItem = chequeTableView.getFocusModel().getFocusedItem();
                        boolean confirmed = FxUtil.confirm("remove.confirm");
                        if (confirmed) {
                            try {
                                chequeService.removeCheque(focusedItem);
                                chequeTableView.getItems().remove(focusedItem);
                            } catch (SQLException sqlException) {
                                FxUtil.exceptionOccurred(sqlException);
                            }
                        }
                    }

                }
            }
        });
    }
}
