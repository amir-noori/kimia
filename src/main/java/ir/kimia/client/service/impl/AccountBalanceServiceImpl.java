package ir.kimia.client.service.impl;

import com.j256.ormlite.dao.Dao;
import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.data.dao.api.AccountBalanceDao;
import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.model.AccountBalance;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.data.model.Stock;
import ir.kimia.client.service.api.AccountBalanceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class AccountBalanceServiceImpl extends BaseServiceImpl implements AccountBalanceService {

    private static final Logger log = LogManager.getLogger(AccountBalanceServiceImpl.class);

    private AccountBalanceDao accountBalanceDao;

    @Inject
    public AccountBalanceServiceImpl(AccountBalanceDao accountBalanceDao) {
        this.accountBalanceDao = accountBalanceDao;
    }

    @Override
    protected BaseDao getDao() {
        return accountBalanceDao;
    }

    @Override
    public List<AccountBalance> getAccountBalanceByParty(Party party) throws SQLException {
        return accountBalanceDao.getAccountBalanceByParty(party);
    }

    @Override
    public AccountBalance getAccountBalanceByPartyAndProduct(Party party, Product product) throws SQLException {
        return accountBalanceDao.getAccountBalanceByPartyAndProduct(party, product);
    }

    @Override
    public List<AccountBalance> getOfficeCoinDebtorsStocks() throws SQLException {
        return accountBalanceDao.getOfficeCoinDebtorsStocks();
    }

    @Override
    public List<AccountBalance> getOfficeCoinCreditorsStocks() throws SQLException {
        return accountBalanceDao.getOfficeCoinCreditorsStocks();
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(AccountBalance accountBalance) throws SQLException {
        if (accountBalance != null) {
            final Party party = accountBalance.getParty();
            final Product product = accountBalance.getProduct();
            final boolean countable = product.getProductCategory().getCountable();
            final AccountBalance accountBalanceByPartyAndProduct = getAccountBalanceByPartyAndProduct(party, product);
            if (accountBalanceByPartyAndProduct != null) {

                if (!countable) {
                    Double originalAmount = accountBalanceByPartyAndProduct.getAmount();
                    if (originalAmount == null) {
                        originalAmount = 0.0;
                    }
                    Double newAmount = accountBalance.getAmount();
                    if (newAmount == null) {
                        newAmount = 0.0;
                    }
                    accountBalanceByPartyAndProduct.setAmount(originalAmount + newAmount);
                }

                if (countable) {
                    Double originalCount = accountBalanceByPartyAndProduct.getCount();
                    if (originalCount == null) {
                        originalCount = 0.0;
                    }
                    Double newCount = accountBalance.getCount();
                    if (newCount == null) {
                        newCount = 0.0;
                    }
                    if (product.getProductCategory().getCode().equals(BasicProductCategoryCode.CASH.value())) {
                        // for cash the count value is actually amount
                        newCount = accountBalance.getAmount();
                    }
                    accountBalanceByPartyAndProduct.setCount(newCount + originalCount);
                }
                accountBalanceByPartyAndProduct.setOffice(getOffice());
                return accountBalanceDao.createOrUpdate(accountBalanceByPartyAndProduct);
            } else {
                accountBalance.setOffice(getOffice());
                return accountBalanceDao.createOrUpdate(accountBalance);
            }
        }
        return null;
    }

    @Override
    public void createOrUpdate(List<AccountBalance> accountBalanceList) throws SQLException {
        if (accountBalanceList != null) {
            for (AccountBalance accountBalance : accountBalanceList) {
                createOrUpdate(accountBalance);
            }
        }
    }

    @Override
    public void remove(AccountBalance accountBalance) throws SQLException {
        accountBalanceDao.delete(accountBalance);
    }

    @Override
    public List<AccountBalance> getBalancesByPartyType(Party.PartyType partyType) throws SQLException {
        return accountBalanceDao.getBalancesByPartyType(partyType);
    }

    @Override
    public List<AccountBalance> getCashDebtorsStocks() throws SQLException {
        return accountBalanceDao.getCashDebtorsStocks();
    }

    @Override
    public List<AccountBalance> getCashCreditorsStocks() throws SQLException {
        return accountBalanceDao.getCashCreditorsStocks();
    }

    @Override
    public List<AccountBalance> getOfficeCurrencyDebtorsStocks() throws SQLException {
        return accountBalanceDao.getOfficeCurrencyDebtorsStocks();
    }

    @Override
    public List<AccountBalance> getOfficeCurrencyCreditorsStocks() throws SQLException {
        return accountBalanceDao.getOfficeCurrencyCreditorsStocks();
    }

    @Override
    public Double getGoldDebtorsValue() throws SQLException {
        return accountBalanceDao.getGoldDebtorsValue();
    }

    @Override
    public Double getGoldCreditorsValue() throws SQLException {
        return accountBalanceDao.getGoldCreditorsValue();
    }
}
