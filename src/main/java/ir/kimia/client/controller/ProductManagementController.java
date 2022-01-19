package ir.kimia.client.controller;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.data.model.ProductCategory;
import ir.kimia.client.data.model.Stock;
import ir.kimia.client.service.api.ProductCategoryService;
import ir.kimia.client.service.api.ProductService;
import ir.kimia.client.service.api.StockService;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.ComboBoxAutoComplete;
import ir.kimia.client.util.FxUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
public class ProductManagementController extends BaseController {

    private static final Logger log = LogManager.getLogger(ProductManagementController.class);

    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private final StockService stockService;

    @FXML
    private TextField productCategoryCode;
    @FXML
    private Button createBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button resetBtn;
    @FXML
    private Button removeBtn;
    @FXML
    private TextField productCodeInput;
    @FXML
    private ComboBox<ProductCategory> productCategory;
    @FXML
    private ComboBox<Product> productComboBox;
    @FXML
    private TextField goldCarat;
    @FXML
    private TextField receivedWageAmount;
    @FXML
    private TextField payedWageAmount;
    @FXML
    private TextField receivedWagePercentage;
    @FXML
    private TextField payedWagePercentage;
    @FXML
    private TextField beginningOfCycleStockByWeight;
    @FXML
    private TextField beginningOfCycleStockByAmount;

    List<ProductCategory> allProductCategories;
    List<Product> allCategoryProducts;
    private ComboBoxAutoComplete currentAutoCompleteComboBox;

    @Inject
    public ProductManagementController(ProductService productService, ProductCategoryService productCategoryService, StockService stockService) {
        this.productService = productService;
        this.productCategoryService = productCategoryService;
        this.stockService = stockService;
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {
        initProductCategory();
        initFieldValidation();

        /*
            TODO:
             1- reference or office carat?
             2- how is it used in the calculation?
         */
        goldCarat.setText(String.valueOf(ApplicationContext.getOfficeCarat()));

        showTooltipOnFocus(productCategory, true);
        productComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Product object) {
                if (object == null || (object.getProductName() == null && object.getCode() == null)) return "";
                if (object.getProductName() == null && object.getCode() != null) {
                    productComboBox.hide();
                    return String.valueOf(object.getCode());
                }
                return object.getProductName();
            }

            @Override
            public Product fromString(String string) {
                Product result = new Product();
                if (allCategoryProducts != null) {
                    for (Product object : allCategoryProducts) {
                        String key = object.getProductName();
                        if (!StringUtils.isEmpty(key) && key.equals(string)) {
                            result.setProductName(object.getProductName());
                            result.setCode(object.getCode());
                            return result;
                        }
                    }
                }
                if (!StringUtils.isEmpty(string)) {
                    result.setProductName(string);
                }
                return result;
            }
        });

        productCodeInput.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                productComboBox.requestFocus();
            }
        });


        productComboBox.setOnAction(event -> {
            log.debug("productComboBox on Action. selected item: " + productComboBox.getSelectionModel().getSelectedItem());

            try {
                Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
                if (selectedProduct != null && selectedProduct.getCode() == null) { // new product
                    setFocusNodesBasedOnMode(false);
                    String productCodeText = productCodeInput.getText();
                    if (StringUtils.isNotEmpty(productCodeText)) { // product code is inserted manually
                        if (!productService.isCodeAvailable(productCodeText)) {
                            FxUtil.error("product.code.is.taken");
                            productCodeInput.requestFocus();
                            return;
                        }
                    }
                    String nextCode;
                    if (StringUtils.isNotEmpty(productCodeText)) {
                        nextCode = productCodeText;
                    } else {
                        nextCode = productService.getNextCode();
                    }

                    productCodeInput.setText(String.valueOf(nextCode));
                    payedWagePercentage.clear();
                    receivedWagePercentage.clear();
                    payedWageAmount.clear();
                    receivedWageAmount.clear();
                } else if (selectedProduct != null && selectedProduct.getCode() != null) { // existing product
                    Product productByCode;

                    productByCode = productService.getProductByCode(selectedProduct.getCode());
                    productCodeInput.setText(String.valueOf(productByCode.getCode()));
                    productCodeInput.setDisable(true);
                    payedWagePercentage.setText(productByCode.getPayedWagePercentage() != null ? String.valueOf(productByCode.getPayedWagePercentage()) : "");
                    receivedWagePercentage.setText(productByCode.getReceivedWagePercentage() != null ? String.valueOf(productByCode.getReceivedWagePercentage()) : "");
                    payedWageAmount.setText(productByCode.getPayedWageAmount() != null ? String.valueOf(productByCode.getPayedWageAmount()) : "");
                    receivedWageAmount.setText(productByCode.getReceivedWageAmount() != null ? String.valueOf(productByCode.getReceivedWageAmount()) : "");
                    setFocusNodesBasedOnMode(true);
                }
            } catch (SQLException sqlException) {
                FxUtil.exceptionOccurred(sqlException);
            }
        });

    }

    private void initFieldValidation() {
        validationSupport.registerValidator(goldCarat, Validator.combine(this::validateDouble, Validator.createEmptyValidator(message("required"))));
//        validationSupport.registerValidator(productName, Validator.createEmptyValidator(message("required")));
        validationSupport.registerValidator(payedWagePercentage, true, (Validator<String>) this::validatePercentage);
        validationSupport.registerValidator(receivedWagePercentage, true, (Validator<String>) this::validatePercentage);
        validationSupport.registerValidator(receivedWageAmount, true, (Validator<String>) this::validateInteger);
        validationSupport.registerValidator(payedWageAmount, true, (Validator<String>) this::validateInteger);
        validationSupport.registerValidator(beginningOfCycleStockByAmount, true, (Validator<String>) this::validateInteger);
        validationSupport.registerValidator(beginningOfCycleStockByWeight, true, (Validator<String>) this::validateDouble);
    }

    private void initProductData() throws SQLException {
        ProductCategory selectedProductCategory = productCategory.getSelectionModel().getSelectedItem();
        if (selectedProductCategory != null) {
            productCategoryCode.setText(selectedProductCategory.getCode());
            if (selectedProductCategory.getCountable() != null) {
                if (selectedProductCategory.getCountable()) {
                    beginningOfCycleStockByWeight.clear();
                    beginningOfCycleStockByWeight.setDisable(true);
                    beginningOfCycleStockByAmount.setDisable(false);
                    // for countable products carat is not used so it should be disabled.
                    goldCarat.setDisable(true);
                } else {
                    beginningOfCycleStockByAmount.clear();
                    beginningOfCycleStockByWeight.setDisable(false);
                    beginningOfCycleStockByAmount.setDisable(true);
                    goldCarat.setDisable(false);
                }
                if (selectedProductCategory.getCode() != null) {
                    allCategoryProducts = productService.getCategoryProducts(selectedProductCategory);
                    productComboBox.getItems().clear();
                    productComboBox.getItems().addAll(allCategoryProducts);
                }
            }


            currentAutoCompleteComboBox = null;
            currentAutoCompleteComboBox = new ComboBoxAutoComplete<>(productComboBox);

        }
    }

    private void initProductCategory() throws SQLException {
        allProductCategories = productCategoryService.getAllProductCategories(false);
        productCategory.setConverter(new StringConverter<>() {

            @Override
            public String toString(ProductCategory productCategory) {
                if (productCategory == null || productCategory.getTitle() == null || productCategory.getCode() == null)
                    return "";
                return productCategory.getTitle();
            }

            @Override
            public ProductCategory fromString(String string) {
                ProductCategory result = new ProductCategory();
                for (ProductCategory productCategory : allProductCategories) {
                    String key = productCategory.getTitle();
                    if (!StringUtils.isEmpty(key) && key.equals(string)) {
                        result.setTitle(productCategory.getTitle());
                        result.setCode(productCategory.getCode());
                        result.setId(productCategory.getId());
                        result.setCountable(productCategory.getCountable());
                        result.setModifiable(productCategory.getModifiable());
                        return result;
                    }
                }
                return result;
            }
        });
        productCategory.getItems().addAll(allProductCategories);

        productCategory.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    try {
                        initProductData();
                    } catch (SQLException sqlException) {
                        FxUtil.exceptionOccurred(sqlException);
                    }
                }
            }
        });

        productCategory.setCellFactory(element -> {
            ListCell<ProductCategory> cell = new ListCell<ProductCategory>() {
                @Override
                protected void updateItem(ProductCategory item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getTitle());
                }
            };
            cell.setOnMousePressed(e -> {
                if (!cell.isEmpty()) {
                    try {
                        initProductData();
                    } catch (SQLException sqlException) {
                        FxUtil.exceptionOccurred(sqlException);
                    }
                }
            });
            return cell;
        });
        new ComboBoxAutoComplete<>(productCategory);
    }

    @Override
    public void postLoad() {
        setFocusNodesBasedOnMode(false);
        super.postLoad();
    }

    private void setFocusNodesBasedOnMode(boolean editMode) {
        if (editMode) {
            setFocusNodes(productCategory, productComboBox, goldCarat,
                    receivedWageAmount, receivedWagePercentage, payedWageAmount, payedWagePercentage,
                    beginningOfCycleStockByWeight, beginningOfCycleStockByAmount,
                    editBtn);
        } else {
            setFocusNodes(productCategory, productComboBox, goldCarat,
                    receivedWageAmount, receivedWagePercentage, payedWageAmount, payedWagePercentage,
                    beginningOfCycleStockByWeight, beginningOfCycleStockByAmount,
                    createBtn);
        }
    }

    @FXML
    private void createProduct(ActionEvent event) throws SQLException {

        if (!isFormDataValid) {
            FxUtil.error("input.data.invalid");
            return;
        }

        Product productToBeCreated = setupProductDataForCreateOrUpdate();

        productService.createProduct(productToBeCreated);

        createInitialStock(productToBeCreated);

        FxUtil.info("product.create.success");
        resetFormData(true);
        productComboBox.requestFocus();
    }

    private void createInitialStock(Product product) throws SQLException {
        if (product != null) {
            final ProductCategory productCategory = product.getProductCategory();
            if (productCategory != null) {
                Stock beginningOfCycleStock = new Stock();
                beginningOfCycleStock.setProduct(product);
                beginningOfCycleStock.setCarat(product.getCarat());
                if (productCategory.getCountable()) {
                    final String beginningOfCycleStockByAmountText = beginningOfCycleStockByAmount.getText();
                    if (StringUtils.isNotEmpty(beginningOfCycleStockByAmountText)) {
                        beginningOfCycleStock.setCount(Double.valueOf(beginningOfCycleStockByAmountText));
                    } else {
                        beginningOfCycleStock.setCount(0.0);
                    }
                } else {
                    final String beginningOfCycleStockByWeightText = beginningOfCycleStockByWeight.getText();
                    if (StringUtils.isNotEmpty(beginningOfCycleStockByWeightText)) {
                        beginningOfCycleStock.setWeight(Double.valueOf(beginningOfCycleStockByWeightText));
                    } else {
                        beginningOfCycleStock.setWeight(0.0);
                    }
                }

                stockService.createStock(beginningOfCycleStock);
            }
        }
    }

    @FXML
    private void editProduct(ActionEvent event) throws SQLException {
        if (!isFormDataValid) {
            FxUtil.error("input.data.invalid");
            return;
        }
        boolean confirmed = FxUtil.confirm("confirm.edit");
        if (confirmed) {
            Product productToBeUpdated = setupProductDataForCreateOrUpdate();
            productService.updateProduct(productToBeUpdated);

            FxUtil.info("product.edit.success");
            resetFormData(true);
            productComboBox.requestFocus();
        }
    }

    private Product setupProductDataForCreateOrUpdate() {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
        ProductCategory selectedProductCategory = productCategory.getSelectionModel().getSelectedItem();
        Product productTobeCreatedOrUpdated = null;
        if (selectedProduct != null && selectedProductCategory != null) {
            if (selectedProduct.getId() == null) { // this is a new product
                productTobeCreatedOrUpdated = new Product();
                productTobeCreatedOrUpdated.setCode(productCodeInput.getText());
            } else {
                productTobeCreatedOrUpdated = selectedProduct;
            }
            productTobeCreatedOrUpdated.setProductName(selectedProduct.getProductName());

            if (StringUtils.isNotEmpty(receivedWageAmount.getText())) {
                productTobeCreatedOrUpdated.setReceivedWageAmount(Double.valueOf(receivedWageAmount.getText()));
            }
            if (StringUtils.isNotEmpty(receivedWagePercentage.getText())) {
                productTobeCreatedOrUpdated.setReceivedWagePercentage(Double.valueOf(receivedWagePercentage.getText()));
            }
            if (StringUtils.isNotEmpty(payedWageAmount.getText())) {
                productTobeCreatedOrUpdated.setPayedWageAmount(Double.valueOf(payedWageAmount.getText()));
            }
            if (StringUtils.isNotEmpty(payedWagePercentage.getText())) {
                productTobeCreatedOrUpdated.setPayedWagePercentage(Double.valueOf(payedWagePercentage.getText()));
            }
            if (!selectedProductCategory.getCountable() && StringUtils.isNotEmpty(goldCarat.getText())) {
                productTobeCreatedOrUpdated.setCarat(Double.valueOf(goldCarat.getText()));
            }
            productTobeCreatedOrUpdated.setProductCategory(selectedProductCategory);
        }
        return productTobeCreatedOrUpdated;
    }

    @FXML
    private void resetForm(ActionEvent event) throws SQLException {
        resetFormData(true);
    }

    private void resetFormData(boolean useSameProductCategory) throws SQLException {
        productCodeInput.clear();
        productComboBox.getSelectionModel().clearSelection();
        productComboBox.getEditor().setText("");
        payedWagePercentage.setText("");
        receivedWagePercentage.setText("");
        beginningOfCycleStockByAmount.setText("");
        payedWageAmount.setText("");
        receivedWageAmount.setText("");
        beginningOfCycleStockByAmount.setText("");
        beginningOfCycleStockByWeight.setText("");

        if (!useSameProductCategory) {
            productCategory.getSelectionModel().clearSelection();
            productCategory.getEditor().setText("");
        } else {
            initProductData();
        }
    }

    public void removeProduct(ActionEvent event) {
        try {
            Product selectedItem = productComboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getCode() != null) {
                Product productByCode = productService.getProductByCode(selectedItem.getCode());
                if (productByCode != null) {
                    if (stockService.stockExistsForProduct(productByCode.getId())) {
                        FxUtil.error("product.has.stock");
                        return;
                    } else if (FxUtil.confirm("remove.confirm")) {
                        productService.removeProduct(productByCode);
                        FxUtil.info("product.remove.success");
                        resetFormData(true);
                    }
                } else {
                    FxUtil.error("no.such.product.in.database");
                }
            }
        } catch (Exception e) {
            FxUtil.exceptionOccurred(e);
        }

    }
}
