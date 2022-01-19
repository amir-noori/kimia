package ir.kimia.client.ui;

import ir.kimia.client.common.ApplicationContext;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PopupDialog {

    private Dialog dialog;
    private VBox vBox;
    private GridPane grid;
    private List<Node> focusNodes;
    private FormFocusController formFocusController;
    private boolean useOkButton;
    // set includeSubmitInFocusNodes to false for those popups that might have multiple records like stone dialog.
    private boolean includeSubmitInFocusNodes = true;
    private Function<Object, Void> onAfterShown;

    public PopupDialog(int width) {
        useOkButton = false;
        this.dialog = getBasicDialog(width);
        setGridOnBasicDialog();
    }

    public PopupDialog(int width, boolean useOkButton) {
        this.useOkButton = useOkButton;
        this.dialog = getBasicDialog(width);
        setGridOnBasicDialog();
    }

    public void setFocusNodes(List<Node> focusNodes) {
        this.focusNodes = focusNodes;
    }

    public void setFocusNodes(Node... focusNodes) {
        List<Node> nodeList = new ArrayList<>();
        for (Node item : focusNodes) {
            nodeList.add(item);
        }
        if (includeSubmitInFocusNodes) {
            nodeList.add(getConfirmButton());
        }
        this.focusNodes = nodeList;
    }

    private Button getConfirmButton() {
        Button node = (Button) ((ButtonBar) dialog.getDialogPane().getChildren().get(2)).getButtons().get(0);
        return node;
    }

    public void fireConfirm() {
        getConfirmButton().fire();
    }

    protected Dialog getBasicDialog(int width) {
        Dialog dialog = new Dialog<>();
        ButtonType confirmButtonType = null;
        if (useOkButton) {
            confirmButtonType = new ButtonType(message("confirm"), ButtonBar.ButtonData.OK_DONE);
        } else {
            confirmButtonType = new ButtonType(message("confirm"), ButtonBar.ButtonData.APPLY);
        }
        ButtonType cancelButtonType = new ButtonType(message("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);
        dialog.getDialogPane().getStylesheets().add("/css/popup-dialog.css");
        dialog.setWidth(width);
        dialog.setOnShown((EventHandler<DialogEvent>) event -> {
            if (focusNodes != null && focusNodes.size() > 0) {
                formFocusController = new FormFocusController(focusNodes);
            }
            if (onAfterShown != null) {
                onAfterShown.apply(null);
            }

            dialog.getDialogPane().addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> {
                if (!useOkButton && keyEvent.getCode() == KeyCode.ENTER) {
                    // TODO: handle appropriately.
                    if (focusNodes != null && focusNodes.contains(keyEvent.getTarget())) {
                        // continue
                    } else {
                        keyEvent.consume();
                    }
                }
            });
        });

        return dialog;
    }

    protected void setGridOnBasicDialog() {
        vBox = new VBox();
        vBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(grid);
        dialog.getDialogPane().setContent(vBox);
    }

    public final Optional<?> showAndWait(Function<Object, Void> onAfterShown) {
        this.onAfterShown = onAfterShown;
        return dialog.showAndWait();
    }

    public final Optional<?> showAndWait() {
        return dialog.showAndWait();
    }

    private String message(String key) {
        return ApplicationContext.getResourceBundle().getString(key);
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public VBox getvBox() {
        return vBox;
    }

    public void setvBox(VBox vBox) {
        this.vBox = vBox;
    }

    public List<Node> getFocusNodes() {
        return focusNodes;
    }

    public FormFocusController getFormFocusController() {
        return formFocusController;
    }

    public void setFormFocusController(FormFocusController formFocusController) {
        this.formFocusController = formFocusController;
    }

    public GridPane getGrid() {
        return grid;
    }

    public void setGrid(GridPane grid) {
        this.grid = grid;
    }

    public boolean isIncludeSubmitInFocusNodes() {
        return includeSubmitInFocusNodes;
    }

    public void setIncludeSubmitInFocusNodes(boolean includeSubmitInFocusNodes) {
        this.includeSubmitInFocusNodes = includeSubmitInFocusNodes;
    }
}
