package ir.kimia.client.controller;


import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.common.UserSession;
import ir.kimia.client.data.model.Office;
import ir.kimia.client.event.CloseCommandEvent;
import ir.kimia.client.service.api.OfficeService;
import ir.kimia.client.service.api.UserService;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.util.FxUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Amir
 */
@FxAppScoped
public class MainController extends BaseController {

    private static final Logger log = LogManager.getLogger(MainController.class);

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu logoutMenu;

    @FXML
    private Menu switchOfficeMenu;

    private final UserService userService;
    private final OfficeService officeService;


    @Inject
    public MainController(UserService userService, OfficeService officeService) {
        this.userService = userService;
        this.officeService = officeService;
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {
        // since that setting images on menu did not work with FXML or CSS it is set programmatically.
        setMenuImages(logoutMenu, "/images/logout.png", this::logout);
        setMenuImages(switchOfficeMenu, "/images/switch-office.png", this::switchOffice);

        askForOfficeInitialData();
    }

    private void askForOfficeInitialData() {
        showDialog("officeInitialDataPopup", 300, 250, Modality.APPLICATION_MODAL);
    }

    private void setMenuImages(Menu menu, String imageUrl, EventHandler<? super MouseEvent> eventHandler) {
        Image menuIcon = new Image(getClass().getResourceAsStream(imageUrl));
        ImageView menuIconImage = new ImageView(menuIcon);
        menuIconImage.setFitWidth(15);
        menuIconImage.setFitHeight(15);
        Label menuLabel = new Label();
        menuLabel.setGraphic(menuIconImage);
        menuLabel.setOnMouseClicked(eventHandler);
        menu.setGraphic(menuLabel);
    }

    public void logout(MouseEvent event) {
        if (FxUtil.confirm("logout.confirm")) {
            ApplicationContext.getEventBus().post(new CloseCommandEvent());
            ApplicationContext.setUserSession(null);
            changeScene("/fxml/entrance.fxml", ApplicationContext.getPrimaryStage());
        }
    }

    public void switchOffice(MouseEvent event) {
        try {
            List<Office> allOffices = officeService.getAllOffices();
            ChoiceDialog<Office> officeChoiceDialog = new ChoiceDialog(allOffices.get(0), allOffices);
            officeChoiceDialog.setTitle(message("choose.office"));
            officeChoiceDialog.setHeaderText(message("choose.office"));
            officeChoiceDialog.showingProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserSession userSession = ApplicationContext.getUserSession();
                    if (userSession != null) {
                        if (!officeChoiceDialog.getSelectedItem().getId().equals(ApplicationContext.getUserSession().getCurrentOffice().getId())) {
                            ApplicationContext.getEventBus().post(new CloseCommandEvent());
                            userSession.setCurrentOffice(officeChoiceDialog.getSelectedItem());
                        }
                    }
                }
            });
            ComboBox<Office> comboBox = (ComboBox) officeChoiceDialog.getDialogPane().lookup(".combo-box");
            comboBox.getStyleClass().add("center-aligned");
            comboBox.setButtonCell(new ListCell<Office>() {
                @Override
                public void updateItem(Office item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getOfficeName());
                        setAlignment(Pos.CENTER_RIGHT);
                    }
                }
            });
            officeChoiceDialog.getDialogPane().getStylesheets().add("/css/popup-dialog.css");
            officeChoiceDialog.showAndWait();
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }

    }

    @FXML
    private void onMenuItemClick(ActionEvent event) {
        EventTarget eventTarget = event.getTarget();
        String menuId = ((MenuItem) eventTarget).getId();
        Screen screen = Screen.getPrimary();
        Rectangle2D visualBounds = screen.getVisualBounds();

        switch (menuId) {
            case "productCategoryManagement" -> showDialog(menuId, 650, 540, Modality.NONE);
            case "productManagement" -> showDialog(menuId, 850, 750, Modality.NONE);
            case "userManagement" -> showDialog(menuId, 700, 600, Modality.NONE);
            case "partyManagement" -> showDialog(menuId, 950, 750, Modality.NONE);
            case "stock" -> showDialog(menuId, visualBounds.getWidth() * 0.83, visualBounds.getHeight() * 0.75, Modality.NONE);
            case "invoice" -> {
                Stage invoiceDialog = showDialog(menuId, visualBounds.getWidth() * 0.83, visualBounds.getHeight() * 0.75, Modality.NONE);
                invoiceDialog.setMaximized(true);
            }
            case "partyAccountBalanceReport" -> showDialog(menuId, visualBounds.getWidth() * 0.80, visualBounds.getHeight() * 0.75, Modality.NONE);
        }
    }

}
