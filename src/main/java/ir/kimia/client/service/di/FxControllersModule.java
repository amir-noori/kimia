package ir.kimia.client.service.di;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import ir.kimia.client.controller.*;

@Module
public abstract class FxControllersModule {

    @Binds
    @IntoMap
    @ClassKey(MainController.class)
    abstract Object bindMenuController(MainController ctrl);

    @Binds
    @IntoMap
    @ClassKey(LoginController.class)
    abstract Object bindLoginController(LoginController ctrl);

    @Binds
    @IntoMap
    @ClassKey(UserManagementController.class)
    abstract Object bindUserManagementController(UserManagementController ctrl);

    @Binds
    @IntoMap
    @ClassKey(ProductCategoryManagementController.class)
    abstract Object bindProductCategoryManagementController(ProductCategoryManagementController ctrl);

    @Binds
    @IntoMap
    @ClassKey(ProductManagementController.class)
    abstract Object bindProductManagementController(ProductManagementController ctrl);

    @Binds
    @IntoMap
    @ClassKey(PartyManagementController.class)
    abstract Object bindPartyManagementController(PartyManagementController ctrl);

    @Binds
    @IntoMap
    @ClassKey(AccountBalanceManagementController.class)
    abstract Object bindStockManagementController(AccountBalanceManagementController ctrl);

    @Binds
    @IntoMap
    @ClassKey(CoinPopupController.class)
    abstract Object bindCoinPopupController(CoinPopupController ctrl);

    @Binds
    @IntoMap
    @ClassKey(CurrencyPopupController.class)
    abstract Object bindCurrencyPopupController(CurrencyPopupController ctrl);

    @Binds
    @IntoMap
    @ClassKey(OfficeInitialDataPopupController.class)
    abstract Object bindOfficeInitialDataPopupController(OfficeInitialDataPopupController ctrl);

    @Binds
    @IntoMap
    @ClassKey(StockController.class)
    abstract Object bindStockController(StockController ctrl);

    @Binds
    @IntoMap
    @ClassKey(ChequeStockManagementController.class)
    abstract Object bindChequeStockManagementController(ChequeStockManagementController ctrl);

    @Binds
    @IntoMap
    @ClassKey(ProductStockManagementController.class)
    abstract Object bindProductStockManagementController(ProductStockManagementController ctrl);

    @Binds
    @IntoMap
    @ClassKey(OverallStockManagementController.class)
    abstract Object bindOverallStockManagementController(OverallStockManagementController ctrl);

    @Binds
    @IntoMap
    @ClassKey(InvoiceController.class)
    abstract Object bindInvoiceController(InvoiceController ctrl);

    @Binds
    @IntoMap
    @ClassKey(PartyAccountBalanceReportController.class)
    abstract Object bindPartyAccountBalanceReportController(PartyAccountBalanceReportController ctrl);

}
