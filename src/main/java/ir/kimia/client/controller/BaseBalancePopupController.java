package ir.kimia.client.controller;

import ir.kimia.client.controller.model.BaseBalance;
import ir.kimia.client.controller.model.CoinBalance;
import ir.kimia.client.controller.model.CurrencyBalance;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.data.model.ProductCategory;
import ir.kimia.client.service.api.ProductService;
import ir.kimia.client.ui.EditCell;
import ir.kimia.client.ui.GenericCellFactory;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BaseBalancePopupController extends BaseController {


    protected List<Product> categoryProducts;
    protected List<Product> selectedCategoryProducts;

    @FXML
    protected Button addBtn;


    protected abstract TableView getBalanceTable();

    protected abstract List<? extends BaseBalance> getBalanceList();

    protected abstract void setAccountBalanceList(List<? extends BaseBalance> accountBalanceList);

    protected abstract ProductService getProductService();

    protected abstract String getProductCategoryCode();

    protected EventHandler<TableColumn.CellEditEvent> onIntegerCellCommitHandler(String cellName) {
        return new EventHandler<>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Object newValue = event.getNewValue();
                try {
                    Integer newValueAsInteger = Integer.valueOf(String.valueOf(newValue));
                    BaseBalance baseBalance = (BaseBalance) event.getTableView().getItems().get(event.getTablePosition().getRow());
                    switch (cellName) {
                        case "purchaseFee" -> baseBalance.setPurchaseFee(newValueAsInteger);
                        case "creditCount" -> baseBalance.setCreditCount(newValueAsInteger);
                        case "debitCount" -> baseBalance.setDebitCount(newValueAsInteger);
                        case "sellFee" -> baseBalance.setSellFee(newValueAsInteger);
                    }
                    getBalanceTable().refresh();
                } catch (NumberFormatException e) {
                    event.consume();
                }
            }
        };
    }

    private Callback<TableColumn.CellDataFeatures, ObservableValue> getWageFactory(String wageType) {
        return new Callback<>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures param) {
                Object paramValue = param.getValue();
                Product product = null;

                if (paramValue instanceof CoinBalance) {
                    CoinBalance value = (CoinBalance) param.getValue();
                    product = value.getProduct();
                } else if (paramValue instanceof CurrencyBalance) {
                    CurrencyBalance value = (CurrencyBalance) param.getValue();
                    product = value.getProduct();
                }
                Product finalProduct = product;
                ObservableValue<Integer> productObservableValue = new ObjectBinding<Integer>() {
                    @Override
                    protected Integer computeValue() {
                        if (finalProduct != null) {
                            switch (wageType) {
                                case "received" -> {
                                    Double receivedWageAmount = finalProduct.getReceivedWageAmount();
                                    if (receivedWageAmount != null) {
                                        return Math.toIntExact(Math.round(receivedWageAmount));
                                    } else {
                                        return 0;
                                    }
                                }
                                case "payed" -> {
                                    Double payedWageAmount = finalProduct.getPayedWageAmount();
                                    if (payedWageAmount != null) {
                                        return Math.toIntExact(Math.round(payedWageAmount));
                                    } else {
                                        return 0;
                                    }
                                }
                            }

                        }
                        return 0;
                    }
                };
                return productObservableValue;

            }
        };
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {
        Image addImage = new Image(getClass().getResourceAsStream("/images/add.png"));
        ImageView addImageView = new ImageView(addImage);
        addImageView.setFitHeight(25);
        addImageView.setFitWidth(25);
        addBtn.setGraphic(addImageView);

        var intCellFactory = EditCell.<BaseBalance, Integer>forTableColumn(new StringConverter<>() {
            @Override
            public String toString(Integer object) {
                return object != null ? object.toString() : "";
            }

            @Override
            public Integer fromString(String string) {
                if (StringUtils.isEmpty(string)) {
                    return null;
                }
                return Integer.valueOf(string);
            }
        });

        TableColumn idColumn = new TableColumn<>(message("row.number"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<BaseBalance, Product> productColumn = new TableColumn<>(message("product"));
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCode(getProductCategoryCode());
        categoryProducts = getProductService().getProductsByCategoryCode(productCategory.getCode());
        selectedCategoryProducts = new ArrayList<>();
        productColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<Product>() {

            @Override
            public String toString(Product object) {
                if (object != null && object.getProductName() != null) return object.getProductName();
                if (categoryProducts != null && categoryProducts.size() > 0) {
                    return categoryProducts.get(0).getProductName();
                }
                return "";
            }

            @Override
            public Product fromString(String string) {
                for (Product categoryProduct : categoryProducts) {
                    if (categoryProduct.getProductName() != null && categoryProduct.getProductName().equals(string)) {
                        return categoryProduct;
                    }
                }
                if (categoryProducts != null && categoryProducts.size() > 0) {
                    return categoryProducts.get(0);
                }
                return new Product();
            }
        }, FXCollections.observableList(categoryProducts)));
        productColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<BaseBalance, Product>, ObservableValue<Product>>() {
            @Override
            public ObservableValue<Product> call(TableColumn.CellDataFeatures<BaseBalance, Product> cell) {
                ObservableValue<Product> productObservableValue = new ObjectBinding<Product>() {
                    @Override
                    protected Product computeValue() {
                        return cell.getValue().getProduct();
                    }
                };
                return productObservableValue;
            }
        });
        productColumn.setEditable(true);
        productColumn.setOnEditCommit(new EventHandler<>() {
            @Override
            public void handle(TableColumn.CellEditEvent<BaseBalance, Product> event) {
                BaseBalance baseBalance = event.getTableView().getItems().get(event.getTablePosition().getRow());
                Product newValue = event.getNewValue();
                Product oldValue = event.getOldValue();
                if (newValue != null) {
                    selectCategoryProductsItem(newValue);
                    removeCategoryProductsItem(oldValue);
                    baseBalance.setProduct(newValue);
                } else if (oldValue != null) {
                    baseBalance.setProduct(oldValue);
                }
            }
        });
        productColumn.setPrefWidth(120);

        TableColumn purchaseFee = new TableColumn<>(message("purchase.fee"));
        purchaseFee.setCellValueFactory(getWageFactory("received"));
        purchaseFee.setEditable(true);
        purchaseFee.setCellFactory(intCellFactory);
        purchaseFee.setOnEditCommit(onIntegerCellCommitHandler("purchaseFee"));

        TableColumn sellFee = new TableColumn<>(message("sell.fee"));
        sellFee.setCellValueFactory(getWageFactory("payed"));
        sellFee.setEditable(true);
        sellFee.setCellFactory(intCellFactory);
        sellFee.setOnEditCommit(onIntegerCellCommitHandler("sellFee"));

        TableColumn debitCount = new TableColumn<>(message("debit"));
        debitCount.setCellValueFactory(new PropertyValueFactory<>("debitCount"));
        debitCount.setEditable(true);
        debitCount.setCellFactory(intCellFactory);
        debitCount.setOnEditCommit(onIntegerCellCommitHandler("debitCount"));

        TableColumn creditCount = new TableColumn<>(message("credit"));
        creditCount.setCellValueFactory(new PropertyValueFactory<>("creditCount"));
        creditCount.setEditable(true);
        creditCount.setCellFactory(intCellFactory);
        creditCount.setOnEditCommit(onIntegerCellCommitHandler("creditCount"));

        TableColumn<BaseBalance, Button> deleteColumn = new TableColumn<>(message("remove"));
        deleteColumn.setCellValueFactory(new PropertyValueFactory<>(null));

        TableView balanceTable = getBalanceTable();
        deleteColumn.setCellFactory(new GenericCellFactory(new EventHandler() {
            @Override
            public void handle(Event event) {
                BaseBalance selectedItem = (BaseBalance) balanceTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    removeCategoryProductsItem(selectedItem.getProduct());
                    balanceTable.getItems().remove(selectedItem);
                }
            }
        }, "/images/remove.png"));

        balanceTable.getColumns().addAll(idColumn, productColumn, purchaseFee, sellFee, debitCount, creditCount, deleteColumn);
        setEmptyEditableTableStyle(balanceTable);
        balanceTable.setFixedCellSize(31);

    }

    protected void selectCategoryProductsItem(Product product) {
        for (int i = 0; i < categoryProducts.size(); i++) {
            Product p = categoryProducts.get(i);
            if (p.getId().equals(product.getId())) {
                categoryProducts.remove(p);
                selectedCategoryProducts.add(p);
            }
        }
    }

    protected void removeCategoryProductsItem(Product product) {
        for (int i = 0; i < selectedCategoryProducts.size(); i++) {
            Product p = selectedCategoryProducts.get(i);
            if (p.getId().equals(product.getId())) {
                categoryProducts.add(p);
                selectedCategoryProducts.remove(p);
            }
        }
    }


}
