package ir.kimia.client.controller;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.controller.model.BaseBalance;
import ir.kimia.client.controller.model.CurrencyBalance;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.event.BeginningOfCycleCurrencyEvent;
import ir.kimia.client.service.api.PartyService;
import ir.kimia.client.service.api.ProductCategoryService;
import ir.kimia.client.service.api.ProductService;
import ir.kimia.client.service.api.StockService;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.util.FxUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

@FxAppScoped
public class CurrencyPopupController extends BaseBalancePopupController {


    protected final StockService stockService;
    protected final PartyService partyService;
    protected final ProductService productService;
    protected final ProductCategoryService productCategoryService;

    private List<CurrencyBalance> currencyBalances;


    @FXML
    private TableView<CurrencyBalance> currencyBalanceTableView;


    @Inject
    public CurrencyPopupController(StockService stockService, PartyService partyService, ProductService productService, ProductCategoryService productCategoryService) {
        this.partyService = partyService;
        this.stockService = stockService;
        this.productService = productService;
        this.productCategoryService = productCategoryService;
    }

    @Override
    protected TableView getBalanceTable() {
        return currencyBalanceTableView;
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {
        super.init(url, resourceBundle);
    }

    @Override
    public void postLoad() {
        super.postLoad();
        if (currencyBalances != null) {
            for (CurrencyBalance currencyBalance : currencyBalances) {
                selectCategoryProductsItem(currencyBalance.getProduct());
            }
            currencyBalanceTableView.getItems().addAll(currencyBalances);
        }
    }

    public void onConfirm(ActionEvent event) {
        BeginningOfCycleCurrencyEvent beginningOfCycleCoinEvent = new BeginningOfCycleCurrencyEvent(currencyBalanceTableView.getItems());
        if (currencyBalanceTableView.getEditingCell() != null) {
            FxUtil.warning("please.finish.editing");
            return;
        }
        ApplicationContext.getEventBus().post(beginningOfCycleCoinEvent);
        stage.close();
    }

    @FXML
    private void addNewRow(ActionEvent event) {
        CurrencyBalance currencyBalance = new CurrencyBalance();
        if (categoryProducts != null && categoryProducts.size() > 0) {
            Product product = categoryProducts.get(0);
            selectCategoryProductsItem(product);
            currencyBalance.setProduct(product);
            currencyBalanceTableView.getItems().add(currencyBalance);
        } else {
            FxUtil.error("no.currency.product.exists");
        }
    }

    public List<CurrencyBalance> getBalanceList() {
        return currencyBalances;
    }

    @Override
    protected void setAccountBalanceList(List<? extends BaseBalance> accountBalanceList) {
        this.currencyBalances = (List<CurrencyBalance>) accountBalanceList;
    }

    public void setCoinBalances(List<CurrencyBalance> currencyBalances) {
        this.currencyBalances = currencyBalances;
    }

    @Override
    protected ProductService getProductService() {
        return productService;
    }

    @Override
    protected String getProductCategoryCode() {
        return "5";
    }

}
