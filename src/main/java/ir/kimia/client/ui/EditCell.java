package ir.kimia.client.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

public class EditCell<S, T> extends TextFieldTableCell<S, T> {

    private TextField textField;
    private boolean escapePressed = false;
    private TablePosition<S, ?> tablePos = null;
    private BiFunction<Object, EditCell, Void> afterCommit;
    private BiFunction<Object, EditCell, Void> onStartEdit;
    private Function<S, ObservableValue<Boolean>> editableExtractor;
    private EventHandler onCellEventFilter;
    private CellType cellType;


    public EditCell(final StringConverter<T> converter) {
        super(converter);
    }

    public EditCell(final StringConverter<T> converter, BiFunction<Object, EditCell, Void> afterCommit, BiFunction<Object, EditCell, Void> onStartEdit,
                    Function<S, ObservableValue<Boolean>> editableExtractor, EventHandler onCellKeyPressHandler, CellType cellType) {
        super(converter);
        this.afterCommit = afterCommit;
        this.onStartEdit = onStartEdit;
        this.editableExtractor = editableExtractor;
        this.onCellEventFilter = onCellKeyPressHandler;
        this.cellType = cellType;
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final StringConverter<T> converter) {
        return list -> new EditCell<S, T>(converter);
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final StringConverter<T> converter,
                                                                                     BiFunction<Object, EditCell, Void> afterCommit,
                                                                                     BiFunction<Object, EditCell, Void> onStartEdit) {
        return list -> new EditCell<S, T>(converter, afterCommit, onStartEdit, null, null, CellType.TEXT);
    }

    /**
     * @param converter         is the StringConverter which implements toString and fromString
     * @param afterCommit       this is invoked after data is committed
     * @param onStartEdit       this is invoked before cell is in editing mode
     * @param editableExtractor this function is invoked to decide whether cell is editable or not
     * @return callback on each cell
     */
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final StringConverter<T> converter,
                                                                                     BiFunction<Object, EditCell, Void> afterCommit,
                                                                                     BiFunction<Object, EditCell, Void> onStartEdit,
                                                                                     Function<S, ObservableValue<Boolean>> editableExtractor) {
        return list -> new EditCell<S, T>(converter, afterCommit, onStartEdit, editableExtractor, null, CellType.TEXT);
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final StringConverter<T> converter,
                                                                                     BiFunction<Object, EditCell, Void> afterCommit,
                                                                                     BiFunction<Object, EditCell, Void> onStartEdit,
                                                                                     Function<S, ObservableValue<Boolean>> editableExtractor,
                                                                                     EventHandler onCellKeyPressHandler) {
        return list -> new EditCell<S, T>(converter, afterCommit, onStartEdit, editableExtractor, onCellKeyPressHandler, CellType.TEXT);
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final StringConverter<T> converter,
                                                                                     BiFunction<Object, EditCell, Void> afterCommit,
                                                                                     BiFunction<Object, EditCell, Void> onStartEdit,
                                                                                     Function<S, ObservableValue<Boolean>> editableExtractor,
                                                                                     EventHandler onCellKeyPressHandler,
                                                                                     CellType cellType) {
        return list -> new EditCell<S, T>(converter, afterCommit, onStartEdit, editableExtractor, onCellKeyPressHandler, cellType);
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() ||
                !getTableColumn().isEditable()) {
            return;
        }
        super.startEdit();
        final TableView<S> table = getTableView();

        if (isEditing()) {
            if (table != null) {
                // make sure the editing cell is also the focused cell.
                getTableView().getFocusModel().focus(table.getEditingCell());
            }
            if (textField == null) {
                textField = getTextField();
                if (onCellEventFilter != null) {
                    textField.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
                        onCellEventFilter.handle(event);
                    });
                }
            }
            escapePressed = false;
            tablePos = table.getEditingCell();
            startEdit(textField);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitEdit(T newValue) {
        if (!isEditing())
            return;
        final TableView<S> table = getTableView();
        if (table != null) {
            // Inform the TableView of the edit being ready to be committed.
            TableColumn.CellEditEvent editEvent = new TableColumn.CellEditEvent(table, tablePos, TableColumn.editCommitEvent(), newValue);
            Event.fireEvent(getTableColumn(), editEvent);
        }
        // we need to setEditing(false):
        super.cancelEdit(); // this fires an invalid EditCancelEvent.
        // update the item within this cell, so that it represents the new value
        updateItem(newValue, false);
        if (table != null) {
            // reset the editing cell on the TableView
            table.edit(-1, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelEdit() {
//        if(getTableView().getEditingCell() == null) return;
        if (escapePressed) {
            // this is a cancel event after escape key
            super.cancelEdit();
            setText(getItemText()); // restore the original text in the view
        } else {
            // this is not a cancel event after escape key
            // we interpret it as commit.
            String newText = "";
            if (textField != null) {
                newText = textField.getText();
            }
            // commit the new text to the model
            this.commitEdit(getConverter().fromString(newText));
        }
        setGraphic(null); // stop editing with TextField
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            if (item instanceof String && StringUtils.isNotEmpty((CharSequence) item)) {
                final Tooltip tooltip = new Tooltip(item.toString());
                tooltip.setShowDelay(new Duration(100));
                setTooltip(tooltip);
            }
        }
        updateCellItem(item, empty);
    }

    @Override
    public void updateIndex(int i) {
        super.updateIndex(i);
        if (editableExtractor != null) {
            if (i == -1) {
                editableProperty().unbind();
            } else if (getTableRow() != null) {
                editableProperty().bind(editableExtractor.apply(getTableRow().getItem()));
            }
        }
    }

    private TextField getTextField() {

        TextField temporaryTextField = null;

        if (cellType != null) {
            if (cellType == CellType.TEXT) {
                temporaryTextField = new TextField(getItemText());
            } else if (cellType == CellType.THOUSAND_SEPARATED_NUMBER) {
                temporaryTextField = new FormattedDoubleTextField(getItemText());
            }
        } else {
            temporaryTextField = new TextField(getItemText());
        }

        final TextField textField = temporaryTextField;

        textField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // do nothing
            }
        });

        // Use onAction here rather than onKeyReleased (with check for Enter),
        TextField finalTextField = textField;
        textField.setOnAction(event -> {
            if (getConverter() == null) {
                throw new IllegalStateException("StringConverter is null.");
            }
            this.commitEdit(getConverter().fromString(finalTextField.getText()));
            if (afterCommit != null) {
                afterCommit.apply(finalTextField, this);
            }
            event.consume();
        });

        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
//                    commitEdit(getConverter().fromString(textField.getText()));
                }
            }
        });

        textField.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ESCAPE)
                escapePressed = true;
            else
                escapePressed = false;
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                // TODO: do we really need this line?
//                throw new IllegalArgumentException("did not expect esc key releases here.");
            }
        });

        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                textField.setText(getConverter().toString(getItem()));
                cancelEdit();
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.TAB) {
                // TODO: do we need this?
//                getTableView().getSelectionModel().selectNext();
//                event.consume();
                textField.positionCaret(textField.getCaretPosition() + 1);
            } else if (event.getCode() == KeyCode.LEFT) {
                // TODO: do we need this?
//                getTableView().getSelectionModel().selectPrevious();
//                event.consume();
                textField.positionCaret(textField.getCaretPosition() - 1);
            } else if (event.getCode() == KeyCode.UP) {
                getTableView().getSelectionModel().selectAboveCell();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                getTableView().getSelectionModel().selectBelowCell();
                event.consume();
            }
        });

        return textField;
    }

    private String getItemText() {
        return getConverter() == null ?
                getItem() == null ? "" : getItem().toString() :
                getConverter().toString(getItem());
    }

    private void updateCellItem(T item, boolean empty) {
        if (isEmpty()) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getItemText());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getItemText());
                setGraphic(null);
            }
        }
    }

    private void startEdit(final TextField textField) {
        if (onStartEdit != null) {

            // TODO: VERY IMPORTANT, FIX THIS.
            // this means if startEdit is called in updateIndex chain then do not run onStartEdit
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            boolean canCallOnStartEdit = true;
            for (StackTraceElement stackTraceElement : stackTrace) {
                if (stackTraceElement.getMethodName().equals("updateIndex")) {
                    canCallOnStartEdit = false;
                    break;
                }
            }

            if (canCallOnStartEdit) {
                onStartEdit.apply(textField, this);
            }

        }
        if (textField != null) {
            textField.setText(getItemText());
        }
        setText(null);
        setGraphic(textField);
        textField.selectAll();
        // requesting focus so that key input can immediately go into the
        // TextField
        textField.requestFocus();
    }
}