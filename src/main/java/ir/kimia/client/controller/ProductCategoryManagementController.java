package ir.kimia.client.controller;

import ir.kimia.client.data.model.Product;
import ir.kimia.client.data.model.ProductCategory;
import ir.kimia.client.service.api.ProductCategoryService;
import ir.kimia.client.service.api.ProductService;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.ActionButtonTableCell;
import ir.kimia.client.ui.EditCell;
import ir.kimia.client.ui.SimpleStringCellFactory;
import ir.kimia.client.util.FxUtil;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.validation.Validator;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

@FxAppScoped
public class ProductCategoryManagementController extends BaseController {

    private static final Logger log = LogManager.getLogger(ProductCategoryManagementController.class);


    @FXML
    private TextField productCategoryTitle;

    @FXML
    private TextField productCategoryCode;

    @FXML
    private TableView<ProductCategory> productCategoryTable;

    @FXML
    private ToggleGroup typeRadioGroup;

    @FXML
    private RadioButton countableChoice;

    @FXML
    private RadioButton uncountableChoice;

    @FXML
    private Button createProductCategoryBtn;

    private final ProductCategoryService productCategoryService;
    private final ProductService productService;


    @Inject
    public ProductCategoryManagementController(ProductCategoryService productCategoryService, ProductService productService) {
        this.productCategoryService = productCategoryService;
        this.productService = productService;
    }

    protected EventHandler<TableColumn.CellEditEvent<ProductCategory, String>> onStringCellCommitHandler(String cellName) {
        return new EventHandler<>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Object newValue = event.getNewValue();
                Object oldValue = event.getOldValue();
                try {
                    ProductCategory productCategory = (ProductCategory) event.getTableView().getItems().get(event.getTablePosition().getRow());
                    switch (cellName) {
                        case "description" -> productCategory.setDescription((String) newValue);
                        case "title" -> {
                            if (onTitleChangeValidate(productCategory, (String) newValue)) {
                                productCategory.setTitle((String) newValue);
                            } else {
                                productCategory.setTitle((String) oldValue);
                            }
                        }
                    }
                    productCategoryTable.refresh();
                } catch (NumberFormatException e) {
                    event.consume();
                } catch (SQLException sqlException) {
                    FxUtil.exceptionOccurred(sqlException);
                }
            }
        };
    }

    private boolean onTitleChangeValidate(ProductCategory productCategory, String newTitle) throws SQLException {
        List<ProductCategory> productCategoriesByTitle = productCategoryService.getProductCategoryByTitle(newTitle);
        if (productCategoriesByTitle != null && productCategoriesByTitle.size() > 0) {
            for (ProductCategory productCategoryByTitle : productCategoriesByTitle) {
                if (!productCategoryByTitle.getId().equals(productCategory.getId())) {
                    FxUtil.error("product.category.name.already.taken");
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {
        validationSupport.registerValidator(productCategoryTitle, Validator.createEmptyValidator("required"));
        setFocusNodes(productCategoryTitle, productCategoryCode, countableChoice, uncountableChoice, createProductCategoryBtn);

        productCategoryCode.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                String nextCode = productCategoryService.getNextCode();
                productCategoryCode.setText(nextCode);
            }
        });

        List<ProductCategory> allProductCategories = productCategoryService.getAllProductCategories(false);

        var idColumn = new TableColumn<ProductCategory, String>(message("id"));
        idColumn.setCellValueFactory(param -> new ObjectBinding<>() {
            @Override
            protected String computeValue() {
                return param.getValue().getCode();
            }
        });

        var titleColumn = new TableColumn<ProductCategory, String>(message("title"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setEditable(true);
        titleColumn.setOnEditCommit(onStringCellCommitHandler("title"));
        titleColumn.setCellFactory(EditCell.<ProductCategory, String>forTableColumn(new SimpleStringCellFactory()));

        var countableColumn = new TableColumn<ProductCategory, Boolean>(message("type"));
        countableColumn.setEditable(true);

        String countableString = message("countable");
        String uncountableString = message("uncountable");
        countableColumn.setCellValueFactory(param -> new ObjectBinding<>() {
            @Override
            protected Boolean computeValue() {
                return param.getValue().getCountable();
            }
        });

        countableColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<Boolean>() {

            @Override
            public String toString(Boolean object) {
                if (object == null) return "";
                if (object) {
                    return countableString;
                } else {
                    return uncountableString;
                }
            }

            @Override
            public Boolean fromString(String string) {
                if (StringUtils.isNotEmpty(string)) {
                    if (string.equals(countableString)) {
                        return true;
                    } else if (string.equals(uncountableString)) {
                        return false;
                    }
                }
                return null;
            }
        }, true, false));
        countableColumn.setOnEditCommit(event -> {
            ProductCategory productCategory = event.getTableView().getItems().get(event.getTablePosition().getRow());
            if (event.getNewValue() != null) {
                productCategory.setCountable(event.getNewValue());
            } else if (event.getOldValue() != null) {
                productCategory.setCountable(event.getOldValue());
            }
        });


        var descriptionColumn = new TableColumn<ProductCategory, String>(message("description"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setEditable(true);
        descriptionColumn.setOnEditCommit(onStringCellCommitHandler("description"));
        descriptionColumn.setCellFactory(EditCell.<ProductCategory, String>forTableColumn(new SimpleStringCellFactory()));

        TableColumn<ProductCategory, Button> editColumn = new TableColumn<>(message("edit"));
        editColumn.setCellFactory(ActionButtonTableCell.<ProductCategory>forTableColumn(message("edit"), (ProductCategory productCategory) -> {
            editProductCategory(productCategory);
            return productCategory;
        }));

        TableColumn<ProductCategory, Button> removeColumn = new TableColumn<>(message("remove"));
        removeColumn.setCellFactory(ActionButtonTableCell.<ProductCategory>forTableColumn(message("remove"), (ProductCategory productCategory) -> {
            removeProductCategory(productCategory);
            return productCategory;
        }));

        productCategoryTable.getItems().removeAll();
        productCategoryTable.getColumns().clear();
        productCategoryTable.getColumns().addAll(idColumn, titleColumn, countableColumn, descriptionColumn, editColumn, removeColumn);
        productCategoryTable.setItems(FXCollections.observableList(allProductCategories));
        productCategoryTable.setEditable(true);
    }

    private void removeProductCategory(ProductCategory productCategory) {
        try {
            if (validateRemoveOrEditProductCategory(productCategory, true)) {
                boolean confirmed = FxUtil.confirm("remove.confirm");
                if (confirmed) {
                    productCategoryService.removeProductCategory(productCategory);
                    productCategoryTable.getItems().remove(productCategory);
                }
            }
        } catch (Exception e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    private void editProductCategory(ProductCategory productCategory) {
        try {
            if (validateRemoveOrEditProductCategory(productCategory, false)) {
                boolean confirmed = FxUtil.confirm("confirm.edit");
                if(confirmed) {
                    productCategoryService.updateProductCategory(productCategory);
                    FxUtil.info("product.category.edit.success");
                }
            }
        } catch (Exception e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    private boolean validateRemoveOrEditProductCategory(ProductCategory productCategory, boolean toDelete) throws SQLException {
        if (productCategory.getModifiable() != null && !productCategory.getModifiable()) {
            FxUtil.error("unmodifiable.product.category");
            return false;
        } else {
            List<Product> productsByCategoryCode = productService.getProductsByCategoryCode(productCategory.getCode());
            if (productsByCategoryCode != null && productsByCategoryCode.size() > 0 && toDelete) {
                FxUtil.error("product.category.has.product.cannot.remove");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void reset() {
//        super.reset();
        productCategoryTitle.setText("");
        productCategoryCode.setText("");
        countableChoice.selectedProperty().setValue(true);
        uncountableChoice.selectedProperty().setValue(false);
        productCategoryTitle.requestFocus();
    }

    @FXML
    private void createProductCategory(ActionEvent event) throws SQLException {
        String title = productCategoryTitle.getText();
        if (StringUtils.isEmpty(title)) {
            FxUtil.error("fill.category.title");
            initFocus();
            return;
        }
        Boolean countableChoiceValue = countableChoice.selectedProperty().getValue();
        ProductCategory productCategory = new ProductCategory();
        productCategory.setTitle(title);
        productCategory.setCode(productCategoryCode.getText());
        productCategory.setModifiable(true); // all new categories are modifiable
        productCategory.setCountable(countableChoiceValue);
        productCategoryService.createProductCategory(productCategory);
        productCategoryTable.getItems().add(productCategory);
        FxUtil.info("product.category.created.successfully");
        reset();
    }

}
