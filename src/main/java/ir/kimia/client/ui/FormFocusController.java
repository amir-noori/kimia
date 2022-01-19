package ir.kimia.client.ui;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;

public class FormFocusController {

    private List<Node> focusNodes;

    private int currentFocusStep;

    public FormFocusController(List<Node> focusNodes) {
        this.focusNodes = focusNodes;
        currentFocusStep = 0;
        initFocus();
    }

    /**
     * it should be called on a form to make sure of the form input focus order.
     */
    protected void initFocus() {
        if (focusNodes != null && focusNodes.size() > 0) {
            Node node0 = focusNodes.get(0);
            node0.requestFocus();
            // change the default behaviour of Enter key except the last one which is most probably a button
            for (int i = 0; i < focusNodes.size() ; i++) {
                Node node = focusNodes.get(i);

                node.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode().equals(KeyCode.ENTER)) {
                        if (event.isAltDown()) {
                            moveFocusBackward();
                        } else {
                            moveFocusForward();
                        }
                    }
                });

                // for radio buttons the default behaviour of Enter key should be changed to move focus on the next form input
                if (node instanceof RadioButton) {
                    node.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            if (event.isAltDown()) {
                                moveFocusBackward();
                            } else {
                                moveFocusForward();
                            }
                            event.consume();
                        }
                    });
                }

                if (node instanceof ComboBox) {
                    ComboBox<?> comboBox = ((ComboBox<?>) node);
                    comboBox.setOnShown(e -> {
                        ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) comboBox.getSkin();
                        ListView<?> list = (ListView<?>) skin.getPopupContent();
                        // this event filter is added to support ctrl+A behaviour
                        list.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
                            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A) {
                                comboBox.getEditor().selectAll();
                            }
                        });
                    });
                    node.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                        if (event.getCode().equals(KeyCode.CONTROL)) {
                            if (!comboBox.isShowing()) {
                                comboBox.show();
                            }
                        }
                    });
                }

                final int j = i;
                node.focusedProperty().addListener((observable, oldValue, newValue) -> currentFocusStep = j);
            }
        }
    }

    /**
     * focus on the next form input.
     */
    protected void moveFocusForward() {
        if (focusNodes != null && focusNodes.size() > 0) {
            int length = focusNodes.size();
            currentFocusStep++;
            if (!(currentFocusStep >= length)) {
                Node node = focusNodes.get(currentFocusStep);
                node.requestFocus();
                if (node.isDisable()) {
                    moveFocusForward();
                }
            }
        }
    }

    /**
     * focus on the previous form input.
     */
    protected void moveFocusBackward() {
        if (focusNodes != null && focusNodes.size() > 0) {
            int length = focusNodes.size();
            currentFocusStep--;
            if (!(currentFocusStep < 0)) {
                Node node = focusNodes.get(currentFocusStep);
                node.requestFocus();
                if (node.isDisable()) {
                    moveFocusBackward();
                }
            }
        }
    }

}
