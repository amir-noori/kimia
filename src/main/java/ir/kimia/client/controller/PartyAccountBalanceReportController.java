package ir.kimia.client.controller;

import ir.kimia.client.data.dto.PartyBalanceReportDto;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.ComboBoxAutoComplete;
import ir.kimia.client.ui.PartyStringConverter;
import ir.kimia.client.util.DateUtil;
import ir.kimia.client.util.FxUtil;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

@FxAppScoped
public class PartyAccountBalanceReportController extends BaseReportController {


    private static final Logger log = LogManager.getLogger(PartyAccountBalanceReportController.class);

    private static DecimalFormat decimalFormat = new DecimalFormat("0");

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button generateReportBtn;
    @FXML
    private ComboBox<Party> partyCombobox;
    @FXML
    private TextField fromDateTextField;
    @FXML
    private TextField toDateTextField;
    @FXML
    private TableView<PartyBalanceReportDto> reportTableView;

    private ComboBoxAutoComplete<Party> partyComboBoxAutoComplete;

    private Callback<TableColumn.CellDataFeatures<PartyBalanceReportDto, String>, ObservableValue<String>> getCellValueFactory(String cellName) {
        return param -> {
            final PartyBalanceReportDto partyBalanceReportDto = param.getValue();
            if (partyBalanceReportDto != null) {
                String result = "";
                switch (cellName) {
                    case "documentNumber" -> {
                        final Integer documentNumber = partyBalanceReportDto.getDocumentNumber();
                        if (documentNumber != null) {
                            result = decimalFormat.format(documentNumber);
                        }
                    }
                }
                final String finalResult = result;
                return new ObjectBinding<>() {

                    @Override
                    protected String computeValue() {
                        return finalResult;
                    }
                };
            }
            return null;
        };
    }

    @Inject
    public PartyAccountBalanceReportController() {
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException, ApplicationException {
        super.init(url, resourceBundle);

        decimalFormat.setMaximumFractionDigits(5);

        final String currentPersianDateString = DateUtil.getCurrentPersianDateString();
        fromDateTextField.setText(currentPersianDateString);
        toDateTextField.setText(currentPersianDateString);

        final List<Party> allParties = partyService.getAllParties();
        partyCombobox.setConverter(new PartyStringConverter(allParties));
        partyCombobox.setItems(FXCollections.observableArrayList(allParties));
        partyComboBoxAutoComplete = new ComboBoxAutoComplete<>(partyCombobox);
        setupReportTable();

        setFocusNodes(partyCombobox, fromDateTextField, toDateTextField, generateReportBtn);
    }

    private void setupReportTable() {
        var documentNumberColumn = new TableColumn<PartyBalanceReportDto, String>(message("document.number"));
        documentNumberColumn.setCellValueFactory(getCellValueFactory("documentNumber"));

        reportTableView.getColumns().addAll(documentNumberColumn);
        reportTableView.setPlaceholder(new Label(message("table.empty.placeholder")));

    }

    public void generateReport(ActionEvent event) {
        reportTableView.getItems().clear();
        try {
            final Party selectedParty = partyCombobox.getSelectionModel().getSelectedItem();
            if (selectedParty != null && StringUtils.isNotEmpty(selectedParty.getPartyName())) {

                final String fromDateTextFieldText = fromDateTextField.getText();
                final String toDateTextFieldText = toDateTextField.getText();
                Date fromDate;
                Date toDate;
                if (StringUtils.isNotEmpty(fromDateTextFieldText)) {
                    fromDate = DateUtil.convertPersianStringToDate(fromDateTextFieldText);
                } else {
                    FxUtil.warning("please.select.from.date");
                    return;
                }

                if (StringUtils.isNotEmpty(toDateTextFieldText)) {
                    toDate = DateUtil.convertPersianStringToDate(toDateTextFieldText);
                } else {
                    FxUtil.warning("please.select.to.date");
                    return;
                }


                generateReportBtn.setDisable(true);
                FxUtil.runInBackground(progressBar, unused -> {
                    List<PartyBalanceReportDto> partyBalanceReport = null;
                    try {
                        partyBalanceReport = reportService.getPartyBalanceReport(selectedParty, fromDate, toDate);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return partyBalanceReport;
                }, taskResult -> {
                    List<PartyBalanceReportDto> finalPartyBalanceReport = (List<PartyBalanceReportDto>) taskResult;
                    if (finalPartyBalanceReport != null) {
                        reportTableView.getItems().addAll(finalPartyBalanceReport);
                    }
                    generateReportBtn.setDisable(false);
                    return null;
                });

            } else {
                FxUtil.warning("please.select.party");
                return;
            }
        } catch (Exception e) {
            FxUtil.exceptionOccurred(e);
        }
        reportTableView.refresh();
    }

}
