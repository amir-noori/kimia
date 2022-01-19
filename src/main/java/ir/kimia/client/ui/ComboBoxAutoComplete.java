package ir.kimia.client.ui;

import ir.kimia.client.util.StringUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This class used as a wrapper for a combox box which needs auto completion
 *
 * @param <T> the same as combobox parameter
 */
public class ComboBoxAutoComplete<T> {

    private static final Logger log = LogManager.getLogger(ComboBoxAutoComplete.class);

    private ComboBox<T> comboBox;
    String filter = "";
    private ObservableList<T> originalItems;


    public ComboBoxAutoComplete(ComboBox<T> targetComboBox) {
        this.comboBox = targetComboBox;
        originalItems = FXCollections.observableArrayList(targetComboBox.getItems());

        targetComboBox.addEventHandler(KeyEvent.KEY_RELEASED, this::handleOnKeyReleased);
        targetComboBox.setOnHidden(this::handleOnHiding);

        /*
            adding event filter to the combobox skin to support space key event.
            basically the space key on combobox will trigger select event which is not appropriate for searchable combobox.
         */
        Skin<?> skin = comboBox.getSkin();
        if (skin == null) {
            ComboBoxListViewSkin comboBoxListViewSkin = new ComboBoxListViewSkin(comboBox);
            comboBox.setSkin(comboBoxListViewSkin);
            skin = comboBoxListViewSkin;
        }
        if (skin instanceof ComboBoxListViewSkin) {
            ((ComboBoxListViewSkin) skin).getPopupContent().addEventFilter(KeyEvent.KEY_PRESSED, (event) -> {
                if (event.getCode() == KeyCode.SPACE) {
                    log.debug("white space filter");
                    handleOnKeyReleased(event);
                    event.consume();
                }
            });
        }
    }

    public void handleOnKeyReleased(KeyEvent e) {
        log.debug("handleOnKeyReleased -> id: " + comboBox.getId() + " - code: " + e.getCode());
        ObservableList<T> filteredList = FXCollections.observableArrayList();
        KeyCode code = e.getCode();

        if (code == KeyCode.ENTER) {
            comboBox.hide();
            return;
        }

        if (!e.isControlDown() || !StringUtils.isNotEmpty(e.getText()) || !e.getText().equals("v")) {
            if (e.isControlDown()) {
                return;
            } else if (code == KeyCode.CONTROL) {
                return;
            }
        }

        if (code.isLetterKey() || code.isDigitKey() || !StringUtils.isEmpty(e.getText())) {
            filter = comboBox.getEditor().getText();
        }

        if (code == KeyCode.DOWN || code == KeyCode.UP) {
            return;
        }

        if (code == KeyCode.BACK_SPACE && filter.length() > 0) {
            filter = filter.substring(0, filter.length() - 1);
            comboBox.getItems().setAll(originalItems);
        }
        if (code == KeyCode.ESCAPE) {
            filter = "";
        }
        if (filter.length() == 0) {
            filteredList = originalItems;
        } else {
            Stream<T> items = comboBox.getItems().stream();
            String txtUsr = StringUtil.replaceArabicCharactersWithPersian(filter.toLowerCase());
            List<T> collection = items.filter(el -> comboBox.getConverter().toString(el).toLowerCase().contains(txtUsr)).collect(Collectors.toList());
            if (collection.size() > 0) {
                filteredList.addAll(collection);
                comboBox.show();
            } else {
                comboBox.hide();
            }

        }
        comboBox.getItems().setAll(filteredList);
    }

    public void handleOnHiding(Event e) {
        log.debug("handleOnHiding -> id: " + comboBox.getId());
        filter = "";
        T selectedItem = comboBox.getSelectionModel().getSelectedItem();
        comboBox.getItems().setAll(originalItems);
        comboBox.getSelectionModel().select(selectedItem);
    }

}