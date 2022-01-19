package ir.kimia.client.ui;

import ir.kimia.client.data.model.Product;
import ir.kimia.client.data.model.ProductCategory;
import ir.kimia.client.data.model.Stock;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TableView;

import java.util.function.Predicate;

public abstract class StockFilterChangeListener<T> implements ChangeListener<String> {

    private FilteredList<T> allProductStocks;
    private TableView<T> tableView;

    public StockFilterChangeListener(FilteredList<T> allProductStocks, TableView<T> tableView) {
        this.allProductStocks = allProductStocks;
        this.tableView = tableView;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        allProductStocks.setPredicate(new Predicate<T>() {
            @Override
            public boolean test(T t) {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (t != null) {
                    Stock stock = getStock(t);
                    final Product product = stock.getProduct();
                    if (product != null) {
                        final ProductCategory productCategory = product.getProductCategory();
                        if (product.getProductName().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        } else if (productCategory != null && productCategory.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        });
        tableView.setItems(allProductStocks);
        tableView.refresh();
    }

    protected abstract Stock getStock(T t);
}
