package ir.kimia.client.controller;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.common.UserSession;
import ir.kimia.client.data.model.Office;
import ir.kimia.client.data.model.User;
import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.api.BankService;
import ir.kimia.client.service.api.OfficeService;
import ir.kimia.client.service.api.StoneTypeService;
import ir.kimia.client.service.api.UserService;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.ComboBoxAutoComplete;
import ir.kimia.client.util.FxUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@FxAppScoped
public class LoginController extends BaseController {

    @FXML
    private TextField officeCaratInput;

    @FXML
    private TextField weightUnitForFeeInput;

    @FXML
    private TextField referenceCaratInput;

    @FXML
    private ComboBox<Office> officeList;

    @FXML
    private PasswordField password;

    @FXML
    private ComboBox<String> goldOrSilver;

    @FXML
    private Button enterBtn;

    private final UserService userService;

    private final OfficeService officeService;

    private final BankService bankService;

    private final StoneTypeService stoneTypeService;

    List<Office> allOffices;

    @Inject
    public LoginController(UserService userService, OfficeService officeService, BankService bankService, StoneTypeService stoneTypeService) {
        this.userService = userService;
        this.officeService = officeService;
        this.bankService = bankService;
        this.stoneTypeService = stoneTypeService;
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException, ApplicationException {

        userService.checkAdminUserExists();
        bankService.setupDefaultBanks();
        stoneTypeService.initializeBasicStoneTypes();

        showTooltipOnFocus(officeList, true);
        showTooltipOnFocus(password, true);

        allOffices = officeService.getAllOffices();
        officeList.getItems().addAll(allOffices);
        goldOrSilver.getItems().addAll(message("gold"), message("silver"));
        goldOrSilver.getSelectionModel().select(0);

        officeCaratInput.setText(String.valueOf(ApplicationContext.getOfficeCarat()));
        referenceCaratInput.setText(String.valueOf(ApplicationContext.getReferenceCarat()));
        weightUnitForFeeInput.setText(String.valueOf(ApplicationContext.getWeightUnitForFee()));

        officeList.setOnAction(event -> {
            Office selectedOffice = officeList.getSelectionModel().getSelectedItem();
            // if office is not new then gold or silver should not be selectable
            if (selectedOffice != null && selectedOffice.getId() != null) {
                goldOrSilver.setDisable(true);
            } else {
                goldOrSilver.setDisable(false);
            }
        });

        officeList.setConverter(new StringConverter<>() {
            @Override
            public String toString(Office office) {
                if (office == null) return null;
                return office.getOfficeName();
            }

            @Override
            public Office fromString(String s) {
                Office office = new Office();
                for (Office object : allOffices) {
                    if (object.getOfficeName().equals(s)) {
                        office.setId(object.getId());
                        office.setOfficeName(object.getOfficeName());
                    }
                }
                if (StringUtils.isEmpty(office.getOfficeName())) {
                    office.setOfficeName(s);
                }
                return office;
            }
        });

        goldOrSilver.setConverter(new StringConverter<>() {
            @Override
            public String toString(String s) {
                return s;
            }

            @Override
            public String fromString(String s) {
                return s;
            }
        });

        new ComboBoxAutoComplete<>(officeList);
        setFocusNodes(officeList, goldOrSilver, password, enterBtn);
    }

    @FXML
    private void login(ActionEvent event) throws SQLException {
//        changeScene("/fxml/main.fxml", ApplicationContext.getPrimaryStage());

        String officeCaratInputText = officeCaratInput.getText();
        String referenceCaratInputText = referenceCaratInput.getText();
        String weightUnitForFeeInputText = weightUnitForFeeInput.getText();

        Double officeCarat = ApplicationContext.getOfficeCarat();
        Double referenceCarat = ApplicationContext.getReferenceCarat();
        Double weightUnitForFee = ApplicationContext.getWeightUnitForFee();

        boolean baseValueInputsValid = true;
        String baseValueValidationMessage = "";

        if (!StringUtils.isEmpty(officeCaratInputText)) {
            try {
                officeCarat = Double.valueOf(officeCaratInputText);
            } catch (NumberFormatException e) {
                baseValueInputsValid = false;
                baseValueValidationMessage = "office.carat.invalid";
            }
        } else {
            baseValueInputsValid = false;
            baseValueValidationMessage = "office.carat.invalid";
        }

        if (!StringUtils.isEmpty(referenceCaratInputText)) {
            try {
                referenceCarat = Double.valueOf(referenceCaratInputText);
            } catch (NumberFormatException e) {
                baseValueInputsValid = false;
                baseValueValidationMessage = "reference.carat.invalid";
            }
        } else {
            baseValueInputsValid = false;
            baseValueValidationMessage = "reference.carat.invalid";
        }

        if (!StringUtils.isEmpty(weightUnitForFeeInputText)) {
            try {
                weightUnitForFee = Double.valueOf(weightUnitForFeeInputText);
            } catch (NumberFormatException e) {
                baseValueInputsValid = false;
                baseValueValidationMessage = "weight.unit.for.fee.invalid";
            }
        } else {
            baseValueInputsValid = false;
            baseValueValidationMessage = "weight.unit.for.fee.invalid";
        }

        if (!baseValueInputsValid) {
            FxUtil.error(baseValueValidationMessage);
            return;
        } else {
            ApplicationContext.setOfficeCarat(officeCarat);
            ApplicationContext.setReferenceCarat(referenceCarat);
            ApplicationContext.setWeightUnitForFee(weightUnitForFee);
        }

        User user = checkUserExists();
        if (user == null) {
            return;
        }

        Office selectedOffice = officeList.getSelectionModel().getSelectedItem();
        String officeName = "";
        if (selectedOffice != null && !StringUtils.isEmpty(selectedOffice.getOfficeName()) && selectedOffice.getId() != null) { // login to office
            Office officeByName = officeService.findOfficeByName(selectedOffice.getOfficeName());
            if (officeByName != null) {
                officeName = officeByName.getOfficeName();
            }
        } else { // create and login to new office
            boolean confirmed = FxUtil.confirm("confirm.office.creation");
            if (confirmed) {
                selectedOffice = officeService.createOffice(selectedOffice); // TODO: also add basic product category and producs to this office
                officeName = selectedOffice.getOfficeName();
            } else {
                return;
            }
        }

        if (!StringUtils.isEmpty(officeName)) {
            // @TODO: check ACL for this user and office
        } else {
            FxUtil.error("please.select.office");
            officeList.requestFocus();
            return;
        }
        UserSession userSession = new UserSession();
        ApplicationContext.setUserSession(userSession);
        ApplicationContext.getUserSession().setCurrentUser(user);
        ApplicationContext.getUserSession().setCurrentOffice(selectedOffice);
        changeScene("/fxml/main.fxml", ApplicationContext.getPrimaryStage());

    }

    private User checkUserExists() throws SQLException {
        String passwordText = password.getText();
        User user;
        if (!StringUtils.isEmpty(passwordText)) {
            user = userService.getUserByPassword(passwordText);
            if (user == null) {
                FxUtil.error("wrong.password");
                password.requestFocus();
                return null;
            }
        } else {
            FxUtil.error("please.enter.password");
            password.requestFocus();
            return null;
        }
        return user;
    }

    public void removeOffice(ActionEvent event) {
        User user;
        try {
            user = checkUserExists();
            if (user != null) {
                Office officeToRemove = officeList.getSelectionModel().getSelectedItem();
                if (officeToRemove == null || officeToRemove.getId() == null) {
                    FxUtil.error("please.choose.existing.office");
                } else {
                    showRemoveOfficeConfirmDialog(officeToRemove);
                }
            }
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }

    }

    @SuppressWarnings("DuplicatedCode")
    private void showRemoveOfficeConfirmDialog(Office officeToRemove) throws SQLException {
        Dialog dialog = new Dialog<>();
        dialog.setHeaderText(message("answer.question.to.remove.office"));

        ButtonType confirmButtonType = new ButtonType(message("confirm"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(message("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);
        dialog.getDialogPane().getStylesheets().add("/css/popup-dialog.css");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        int number1 = (int) ((Math.random() * (100)));
        int number2 = (int) ((Math.random() * (100)));
        String question = MessageFormat.format(message("add.question"), number1, number2);
        TextField answerTextField = new TextField();
        answerTextField.setPromptText("?");
        grid.add(new Label(question), 0, 0);
        grid.add(answerTextField, 0, 1);

        dialog.getDialogPane().setContent(grid);

        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
            if (buttonData.equals(ButtonBar.ButtonData.OK_DONE)) {
                String answerText = answerTextField.getText();
                if (StringUtils.isNotEmpty(answerText)) {
                    try {
                        int answer = Integer.parseInt(answerText);
                        if (answer == number1 + number2) {
                            officeService.removeOffice(officeToRemove);

                            ObservableList<Office> items = officeList.getItems();
                            allOffices.remove(officeToRemove);
                            officeList.getItems().remove(officeToRemove);
                            officeList.getSelectionModel().clearSelection();
                            officeList.getEditor().setText("");
                            officeList.setValue(null);
                            FxUtil.info("office.remove.success");
                            return;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
                FxUtil.error("wrong.answer");
            }
        }


    }
}
