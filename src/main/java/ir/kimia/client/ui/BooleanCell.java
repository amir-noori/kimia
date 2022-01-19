package ir.kimia.client.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;

import java.util.function.BiFunction;

public class BooleanCell<S> extends TableCell<S, Boolean> {

    private CheckBox checkBox;

    public BooleanCell(BiFunction<TableRow, Boolean, Void> onChangeHandler) {
        checkBox = new CheckBox();
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (onChangeHandler != null) {
                    TableRow tableRow = (TableRow) checkBox.getParent().getParent();
                    onChangeHandler.apply(tableRow, newValue);
                }
                if (isEditing()) {
                    commitEdit(newValue == null ? false : newValue);
                }
            }
        });
        this.setGraphic(checkBox);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.setEditable(true);
    }

    public BooleanCell() {
        this(null);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (isEmpty()) {
            return;
        }
        checkBox.setDisable(false);
        checkBox.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        checkBox.setDisable(true);
    }

    public void commitEdit(Boolean value) {
        super.commitEdit(value);
        checkBox.setDisable(true);
    }

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!isEmpty() && item != null) {
            checkBox.setSelected(item);
        }
    }
}
