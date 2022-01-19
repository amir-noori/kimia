package ir.kimia.client.service.impl;

import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.InvoiceDao;
import ir.kimia.client.data.dao.api.InvoiceRecordDao;
import ir.kimia.client.data.dao.api.StoneDao;
import ir.kimia.client.data.model.*;
import ir.kimia.client.service.api.AccountBalanceService;
import ir.kimia.client.service.api.InvoiceService;
import ir.kimia.client.service.api.ProductService;
import ir.kimia.client.service.api.StockService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Collection;

public class InvoiceServiceImpl extends BaseServiceImpl implements InvoiceService {

    private static final Logger log = LogManager.getLogger(InvoiceServiceImpl.class);

    private final InvoiceDao invoiceDao;
    private final InvoiceRecordDao invoiceRecordDao;
    private final StoneDao stoneDao;
    private StockService stockService;
    private AccountBalanceService accountBalanceService;
    private ProductService productService;

    @Inject
    public InvoiceServiceImpl(InvoiceDao invoiceDao, InvoiceRecordDao invoiceRecordDao, StoneDao stoneDao,
                              StockService stockService, AccountBalanceService accountBalanceService, ProductService productService) {
        this.invoiceDao = invoiceDao;
        this.invoiceRecordDao = invoiceRecordDao;
        this.stoneDao = stoneDao;
        this.stockService = stockService;
        this.accountBalanceService = accountBalanceService;
        this.productService = productService;
    }

    @Override
    public Invoice getByDocumentNumber(Integer documentNumber) throws SQLException {
        return invoiceDao.findByDocumentNumber(documentNumber);
    }

    @Override
    public Invoice getByInvoiceNumber(Party party, Integer invoiceNumber) throws SQLException {
        return invoiceDao.findByInvoiceNumber(party, invoiceNumber);
    }

    @Override
    public void removeInvoice(Invoice invoice) throws SQLException {
        invoice.setOffice(getOffice());
        Collection<InvoiceRecord> invoiceRecords = invoice.getInvoiceRecords();
        for (InvoiceRecord invoiceRecord : invoiceRecords) {
            invoiceRecordDao.delete(invoiceRecord);
        }
        invoiceDao.delete(invoice);
    }

    @Override
    public void createOrUpdateInvoice(Invoice invoice) throws SQLException {
        invoice.setOffice(getOffice());
        invoiceDao.createOrUpdate(invoice);
        Collection<InvoiceRecord> invoiceRecords = invoice.getInvoiceRecords();
        for (InvoiceRecord invoiceRecord : invoiceRecords) {
            invoiceRecord.setInvoice(invoice);
            invoiceRecordDao.createOrUpdate(invoiceRecord);
            final Collection<Stone> stones = invoiceRecord.getStones();
            if (stones != null && stones.size() > 0) {
                for (Stone stone : stones) {
                    stone.setOffice(getOffice());
                    stone.setInvoiceRecord(invoiceRecord);
                    stoneDao.createOrUpdate(stone);
                }
            }
            handleStockData(invoiceRecord);
            handleProductData(invoiceRecord);
            handleAccountBalanceData(invoiceRecord);
        }
    }

    private void handleAccountBalanceData(InvoiceRecord invoiceRecord) throws SQLException {
        final Product product = invoiceRecord.getProduct();
        final Party party = invoiceRecord.getInvoice().getParty();
        final Integer dealTypeValue = invoiceRecord.getDealType();
        if (dealTypeValue != null) {
            final InvoiceRecord.DealType dealType = InvoiceRecord.DealType.getByValue(dealTypeValue);
            if (dealType.equals(InvoiceRecord.DealType.PURCHASE_BY_CASH) || dealType.equals(InvoiceRecord.DealType.SELL_BY_CASH)) {
                AccountBalance accountBalance = new AccountBalance();
                accountBalance.setParty(party);
                accountBalance.setProduct(product);
                if (invoiceRecord.getCount() != null) {
                    if (dealType.equals(InvoiceRecord.DealType.PURCHASE_BY_CASH)) {
                        accountBalance.setCount((double) invoiceRecord.getCount());
                    } else if (dealType.equals(InvoiceRecord.DealType.SELL_BY_CASH)) {
                        accountBalance.setCount((double) -invoiceRecord.getCount());
                    }
                }
                if (invoiceRecord.getWeightByScale() != null) {
                    // wight is set as amount which is for gold only
                    if (dealType.equals(InvoiceRecord.DealType.PURCHASE_BY_CASH)) {
                        accountBalance.setAmount(invoiceRecord.getWeightByScale());
                    } else if (dealType.equals(InvoiceRecord.DealType.SELL_BY_CASH)) {
                        accountBalance.setAmount(-invoiceRecord.getWeightByScale());
                    }
                }

                accountBalanceService.createOrUpdate(accountBalance);
            }
        }
    }

    private void handleProductData(InvoiceRecord invoiceRecord) throws SQLException {
        final Product product = invoiceRecord.getProduct();
        if (product != null) {
            final Integer dealTypeValue = invoiceRecord.getDealType();
            InvoiceRecord.DealType dealType = null;
            if (dealTypeValue != null) {
                dealType = InvoiceRecord.DealType.getByValue(dealTypeValue);
                if (dealType != null) {
                    final Double wagePercentage = invoiceRecord.getWagePercentage();
                    final Double fee = invoiceRecord.getFee();
                    if (dealType.equals(InvoiceRecord.DealType.PAYMENT)) {
                        product.setPayedWageAmount(fee);
                        product.setPayedWagePercentage(wagePercentage);
                    } else if (dealType.equals(InvoiceRecord.DealType.PURCHASE)) {
                        product.setReceivedWageAmount(fee);
                        product.setReceivedWagePercentage(wagePercentage);
                    }
                    productService.createOrUpdateProduct(product);
                }
            }
        }
    }

    private void handleStockData(InvoiceRecord invoiceRecord) throws SQLException {
        final Product product = invoiceRecord.getProduct();
        final Party party = invoiceRecord.getInvoice().getParty();
        final Integer dealTypeValue = invoiceRecord.getDealType();
        final InvoiceRecord.InvoiceRecordType invoiceRecordType = invoiceRecord.getInvoiceRecordType();
        InvoiceRecord.DealType dealType = null;
        if (dealTypeValue != null) {
            dealType = InvoiceRecord.DealType.getByValue(dealTypeValue);
            if (dealType.equals(InvoiceRecord.DealType.PURCHASE_BY_CASH) || dealType.equals(InvoiceRecord.DealType.SELL_BY_CASH)) {
                // for these deal types only account balance is changed not stock
                return;
            }
        }
        if (product != null) {
            final Boolean countable = product.getProductCategory().getCountable();
            final Stock stockByProduct = stockService.getStockByProduct(product);
            if (stockByProduct == null) {
                // should create a stock for this party and product
                stockByProduct.setCarat(invoiceRecord.getCarat());
                stockByProduct.setWeight(invoiceRecord.getWeightByScale());

                if (invoiceRecord.getCount() != null) {
                    stockByProduct.setCount(Double.valueOf(invoiceRecord.getCount()));
                } else {
                    stockByProduct.setCount(0.0);
                }
                stockByProduct.setCode(stockService.getNextCode());
                stockByProduct.setProduct(product);
                stockByProduct.setParty(party);
            } else {
                // should update the stock for this party and product
                if (countable) {
                    Double originalCount = stockByProduct.getCount();
                    if (originalCount == null) {
                        originalCount = 0.0;
                    }
                    Double invoiceRecordCount = 0.0;
                    if (invoiceRecord.getCount() != null) {
                        invoiceRecordCount = Double.valueOf(invoiceRecord.getCount());
                    }

                    switch (invoiceRecordType) {
                        case CASH -> {
                            invoiceRecordCount = invoiceRecord.getAmount();
                        }
                    }
                    stockByProduct.setCount(originalCount + invoiceRecordCount);
                } else {
                    Double originalWeight = stockByProduct.getWeight();
                    if (originalWeight == null) {
                        originalWeight = 0.0;
                    }
                    Double recordWeightByScale = invoiceRecord.getWeightByScale();
                    if (recordWeightByScale == null) {
                        recordWeightByScale = 0.0;
                    }

                    stockByProduct.setWeight(originalWeight + recordWeightByScale);
                }
            }

            final Double wagePercentage = invoiceRecord.getWagePercentage();
            final Double fee = invoiceRecord.getFee();
            if (dealType != null) {
                if (dealType.equals(InvoiceRecord.DealType.PAYMENT) || dealType.equals(InvoiceRecord.DealType.PURCHASE)) {
                    if (fee != null) {
                        stockByProduct.setWageFee(fee);
                    }
                    if (wagePercentage != null) {
                        stockByProduct.setWagePercentage(wagePercentage);
                    }
                }
            }

            stockService.updateOrCreateStock(stockByProduct);
        }
    }

    @Override
    public Integer getNextDocumentNumber() throws SQLException {
        return invoiceDao.getNextDocumentNumber();
    }

    @Override
    public Integer getNextInvoiceNumber(Party party) throws SQLException {
        return invoiceDao.getNextInvoiceNumber(party);
    }

    @Override
    public boolean documentNumberExists(Integer documentNumber) throws SQLException {
        Invoice byDocumentNumber = invoiceDao.findByDocumentNumber(documentNumber);
        return byDocumentNumber != null;
    }

    @Override
    public boolean invoiceNumberExists(Party party, Integer invoiceNumber) throws SQLException {
        Invoice byInvoiceNumber = invoiceDao.findByInvoiceNumber(party, invoiceNumber);
        return byInvoiceNumber != null;
    }

    @Override
    protected BaseDao getDao() {
        return invoiceDao;
    }
}
