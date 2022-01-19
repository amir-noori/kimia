package ir.kimia.client.service.di;

import dagger.Module;
import dagger.Provides;
import ir.kimia.client.data.dao.DaoModule;
import ir.kimia.client.data.dao.api.*;
import ir.kimia.client.service.api.*;
import ir.kimia.client.service.impl.*;

import javax.inject.Singleton;


@Module(
        includes = DaoModule.class
)
public class ServiceModule {

    @Provides
    @Singleton
    public UserService provideUserService(UserDao userDao) {
        return new UserServiceImpl(userDao);
    }

    @Provides
    @Singleton
    public ProductCategoryService provideProductCategoryService(ProductCategoryDao productCategoryDao, ProductDao productDao) {
        return new ProductCategoryServiceImpl(productCategoryDao, productDao);
    }

    @Provides
    @Singleton
    public OfficeService provideOfficeService(OfficeDao officeDao, ProductCategoryDao productCategoryDao, ProductDao productDao, StockDao stockDao, PartyDao partyDao) {
        return new OfficeServiceImpl(officeDao, productCategoryDao, productDao, stockDao, partyDao);
    }

    @Provides
    @Singleton
    public ProductService provideProductService(ProductDao productDao, ProductCategoryDao productCategoryDao) {
        return new ProductServiceImpl(productDao, productCategoryDao);
    }

    @Provides
    @Singleton
    public PartyService providePartyService(PartyDao partyDao, AddressDao addressDao) {
        return new PartyServiceImpl(partyDao, addressDao);
    }

    @Provides
    @Singleton
    public StockService provideStockService(StockDao stockDao, ProductDao productDao, StoneDao stoneDao, FinenessDao finenessDao) {
        return new StockServiceImpl(stockDao, productDao, stoneDao, finenessDao);
    }

    @Provides
    @Singleton
    public AddressService provideAddressService(AddressDao addressDao) {
        return new AddressServiceImpl(addressDao);
    }

    @Provides
    @Singleton
    public BankService provideBankService(BankDao bankDao) {
        return new BankServiceImpl(bankDao);
    }

    @Provides
    @Singleton
    public ChequeService provideChequeService(ChequeDao chequeDao) {
        return new ChequeServiceImpl(chequeDao);
    }

    @Provides
    @Singleton
    public SystemParameterService provideSystemParameterService(SystemParameterDao systemParameterDao) {
        return new SystemParameterServiceImpl(systemParameterDao);
    }

    @Provides
    @Singleton
    public StoneService provideStoneService(StoneDao stoneDao) {
        return new StoneServiceImpl(stoneDao);
    }

    @Provides
    @Singleton
    public StoneTypeService provideStoneTypeService(StoneTypeDao stoneTypeDao) {
        return new StoneTypeServiceImpl(stoneTypeDao);
    }

    @Provides
    @Singleton
    public FinenessService provideFinenessService(FinenessDao finenessDao) {
        return new FinenessServiceImpl(finenessDao);
    }

    @Provides
    @Singleton
    public PurityEvaluatorService providePurityEvaluatorService(PurityEvaluatorDao purityEvaluatorDao) {
        return new PurityEvaluatorServiceImpl(purityEvaluatorDao);
    }

    @Provides
    @Singleton
    public InvoiceService provideInvoiceService(InvoiceDao invoiceDao, InvoiceRecordDao invoiceRecordDao,
                                                StoneDao stoneDao, StockService stockService, AccountBalanceService accountBalanceService,
                                                ProductService productService) {
        return new InvoiceServiceImpl(invoiceDao, invoiceRecordDao, stoneDao, stockService, accountBalanceService, productService);
    }

    @Provides
    @Singleton
    public InvoiceRecordService provideInvoiceRecordService(InvoiceDao invoiceDao, InvoiceRecordDao invoiceRecordDao) {
        return new InvoiceRecordServiceImpl(invoiceDao, invoiceRecordDao);
    }


    @Provides
    @Singleton
    public AccountBalanceService provideAccountBalanceService(AccountBalanceDao accountBalanceDao) {
        return new AccountBalanceServiceImpl(accountBalanceDao);
    }

    @Provides
    @Singleton
    public ReportService provideReportService(ReportDao reportDao) {
        return new ReportServiceImpl(reportDao);
    }
}
