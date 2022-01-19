package ir.kimia.client.controller;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.util.FxUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;


/**
 * @author Amir
 */
@FxAppScoped
public class OfficeInitialDataPopupController extends BaseController {

    private static final Logger log = LogManager.getLogger(OfficeInitialDataPopupController.class);

    @FXML
    public Button confirmBtn;

    @FXML
    private TextField dollarQuote;

    @FXML
    private TextField ounceQuote;

    @FXML
    private TextField goldQuote;

    @Inject
    public OfficeInitialDataPopupController() {
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {
        validationSupport.registerValidator(dollarQuote, this::validateDouble);
        validationSupport.registerValidator(ounceQuote, this::validateDouble);
        validationSupport.registerValidator(goldQuote, this::validateDouble);

        setFocusNodes(dollarQuote, ounceQuote, goldQuote, confirmBtn);
    }

    @Override
    public void postLoad() {
        super.postLoad();
    }

    public void onCancel(ActionEvent event) {

    }

    public void onConfirm(ActionEvent event) {
        String dollarQuoteText = dollarQuote.getText();
        String ounceQuoteText = ounceQuote.getText();
        String goldQuoteText = goldQuote.getText();

        if(isFormDataValid) {
            try {
                ApplicationContext.setDollarQuote(Double.parseDouble(dollarQuoteText));
            } catch (NumberFormatException ignored) {}
            try {
                ApplicationContext.setOunceQuote(Double.parseDouble(ounceQuoteText));
            } catch (NumberFormatException ignored) {}
            try {
                ApplicationContext.setGoldQuote(Double.parseDouble(goldQuoteText));
            } catch (NumberFormatException ignored) {}
            stage.close();
        } else {
            FxUtil.error("input.data.invalid");
        }
    }
}
