package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.AccountBalance;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.data.model.Product;

import java.sql.SQLException;
import java.util.List;

public interface AccountBalanceDao extends BaseDao<AccountBalance, Integer> {

    public List<AccountBalance> getAccountBalanceByParty(Party party) throws SQLException;

    public AccountBalance getAccountBalanceByPartyAndProduct(Party party, Product product) throws SQLException;

    public List<AccountBalance> getBalancesByPartyType(Party.PartyType partyType) throws SQLException;

    public List<AccountBalance> getCashDebtorsStocks() throws SQLException;

    public List<AccountBalance> getCashCreditorsStocks() throws SQLException;

    public List<AccountBalance> getOfficeCoinDebtorsStocks() throws SQLException;

    public List<AccountBalance> getOfficeCoinCreditorsStocks() throws SQLException;

    public List<AccountBalance> getOfficeCurrencyDebtorsStocks() throws SQLException;

    public List<AccountBalance> getOfficeCurrencyCreditorsStocks() throws SQLException;

    public Double getGoldDebtorsValue() throws SQLException;

    public Double getGoldCreditorsValue() throws SQLException;
}
