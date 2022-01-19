package ir.kimia.client.controller;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.controller.model.BaseBalance;
import ir.kimia.client.controller.model.CoinBalance;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.event.BeginningOfCycleCoinEvent;
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
public class CoinPopupController extends BaseBalancePopupController {

    protected final StockService stockService;
    protected final PartyService partyService;
    protected final ProductService productService;
    protected final ProductCategoryService productCategoryService;

    private List<CoinBalance> coinBalanceList;

    @FXML
    private TableView<CoinBalance> coinBalanceTableView;


    @Inject
    public CoinPopupController(StockService stockService, PartyService partyService, ProductService productService, ProductCategoryService productCategoryService) {
        this.partyService = partyService;
        this.stockService = stockService;
        this.productService = productService;
        this.productCategoryService = productCategoryService;
    }

    @Override
    protected TableView getBalanceTable() {
        return coinBalanceTableView;
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {
        super.init(url, resourceBundle);
    }

    @Override
    public void postLoad() {
        super.postLoad();
        if (coinBalanceList != null) {
            for (CoinBalance coinBalance : coinBalanceList) {
                selectCategoryProductsItem(coinBalance.getProduct());
            }
            coinBalanceTableView.getItems().addAll(coinBalanceList);
        }
    }

    public void onConfirm(ActionEvent event) {
        BeginningOfCycleCoinEvent beginningOfCycleCoinEvent = new BeginningOfCycleCoinEvent(coinBalanceTableView.getItems());
        if (coinBalanceTableView.getEditingCell() != null) {
            FxUtil.warning("please.finish.editing");
            return;
        }
        ApplicationContext.getEventBus().post(beginningOfCycleCoinEvent);
        stage.close();
    }

    @FXML
    private void addNewRow(ActionEvent event) {
        CoinBalance coinBalance = new CoinBalance();
        if (categoryProducts != null && categoryProducts.size() > 0) {
            Product product = categoryProducts.get(0);
            selectCategoryProductsItem(product);
            coinBalance.setProduct(product);
            coinBalanceTableView.getItems().addAll(coinBalance);
        } else {
            FxUtil.error("no.coin.product.exists");
        }
    }

    public List<CoinBalance> getBalanceList() {
        return coinBalanceList;
    }

    @Override
    protected void setAccountBalanceList(List<? extends BaseBalance> accountBalanceList) {
        this.coinBalanceList = (List<CoinBalance>) accountBalanceList;
    }

    @Override
    protected ProductService getProductService() {
        return productService;
    }

    @Override
    protected String getProductCategoryCode() {
        return "6";
    }

    public void setCoinBalanceList(List<CoinBalance> coinBalanceList) {
        this.coinBalanceList = coinBalanceList;
    }
}
