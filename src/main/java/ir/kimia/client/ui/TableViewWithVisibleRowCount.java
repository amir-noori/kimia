package ir.kimia.client.ui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * TableView with visibleRowCountProperty.
 *
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewWithVisibleRowCount<T> extends TableView<T> {

    private IntegerProperty visibleRowCount = new SimpleIntegerProperty(this, "visibleRowCount", 10);


    public IntegerProperty visibleRowCountProperty() {
        return visibleRowCount;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableViewSkinX<T>(this);
    }

    /**
     * Skin that respects table's visibleRowCount property.
     */
    public class TableViewSkinX<T> extends TableViewSkin<T> {

        public TableViewSkinX(TableViewWithVisibleRowCount<T> tableView) {
            super(tableView);
            registerChangeListener(tableView.visibleRowCountProperty(), e -> visibleRowCountChanged());
        }

        private void visibleRowCountChanged() {
            getSkinnable().requestLayout();
        }

        /**
         * Returns the visibleRowCount value of the table.
         */
        private int getVisibleRowCount() {
            return ((TableViewWithVisibleRowCount<T>) getSkinnable()).visibleRowCountProperty().get();
        }

        /**
         * Calculates and returns the pref height of the for the given number of
         * rows.
         */
        protected double getFlowPrefHeight(int rows) {
            double height = 0;
            for (int i = 0; i < rows && i < getItemCount(); i++) {
                height += invokeFlowCellLength(i);
            }
            return height + snappedTopInset() + snappedBottomInset();
        }

        /**
         * Overridden to compute the sum of the flow height and header prefHeight.
         */
        @Override
        protected double computePrefHeight(double width, double topInset,
                                           double rightInset, double bottomInset, double leftInset) {
            // super hard-codes to 400 .. doooh
            double prefHeight = getFlowPrefHeight(getVisibleRowCount());
            return prefHeight + getTableHeaderRow().prefHeight(width);
        }

        /**
         * Reflectively invokes protected getCellLength(i) of flow.
         * @param index the index of the cell.
         * @return the cell height of the cell at index.
         */
        protected double invokeFlowCellLength(int index) {
            // note: use your own utility method to reflectively access internal fields/methods
            return (double) FXUtils.invokeGetMethodValue(VirtualFlow.class, getVirtualFlow(),
                    "getCellLength", Integer.TYPE, index);
        }

    }

}