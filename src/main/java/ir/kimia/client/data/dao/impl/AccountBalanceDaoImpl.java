package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.common.BasicProductCode;
import ir.kimia.client.data.dao.api.AccountBalanceDao;
import ir.kimia.client.data.model.AccountBalance;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.data.model.Product;

import java.sql.SQLException;
import java.util.List;

public class AccountBalanceDaoImpl extends AbstractBaseDaoImpl<AccountBalance, Integer> implements AccountBalanceDao {

    public AccountBalanceDaoImpl(ConnectionSource connectionSource, Class<AccountBalance> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public List<AccountBalance> getAccountBalanceByParty(Party party) throws SQLException {
        return queryBuilder().where().eq("PARTY_ID", party.getId()).and().eq("OFFICE_ID", getOfficeId()).query();
    }

    @Override
    public AccountBalance getAccountBalanceByPartyAndProduct(Party party, Product product) throws SQLException {
        return queryBuilder().where().eq("PARTY_ID", party.getId())
                .and().eq("PRODUCT_ID", product.getId())
                .and().eq("OFFICE_ID", getOfficeId()).queryForFirst();
    }

    @Override
    public List<AccountBalance> getOfficeCoinDebtorsStocks() throws SQLException {
        String sql = "select s.* from TBL_ACCOUNT_BALANCE s where PRODUCT_ID in " +
                "(select p.ID from TBL_PRODUCT p where p.PRODUCTCATEGORY_ID = " +
                "(select pc.id from TBL_PRODUCT_CATEGORY pc where pc.code = '" + BasicProductCategoryCode.COIN.value() + "' and OFFICE_ID = " + getOfficeId() + ")) " +
                " and PARTY_ID is not null " +
                " and count > 0 " +  // office wants coin from party
                " and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<AccountBalance> getOfficeCoinCreditorsStocks() throws SQLException {
        String sql = "select s.* from TBL_ACCOUNT_BALANCE s where PRODUCT_ID in " +
                "(select p.ID from TBL_PRODUCT p where p.PRODUCTCATEGORY_ID = " +
                "(select pc.id from TBL_PRODUCT_CATEGORY pc where pc.code = '" + BasicProductCategoryCode.COIN.value() + "' and OFFICE_ID = " + getOfficeId() + ")) " +
                " and PARTY_ID is not null " +
                " and count < 0 " + // // office owes coin to party
                " and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<AccountBalance> getBalancesByPartyType(Party.PartyType partyType) throws SQLException {
        String sql = "select s.* from TBL_ACCOUNT_BALANCE s where s.OFFICE_ID = " + getOfficeId() + " " +
                "and s.PARTY_ID  in (select p.ID from TBL_PARTY p where p.OFFICE_ID = " + getOfficeId() + " ";
        if (!partyType.equals(Party.PartyType.ALL)) {
            sql += " and p.PARTYTYPE = " + partyType.ordinal();
        }
        sql += " and p.CODE not in (1000, 2000));"; // 1000 and 2000 party codes are special types of parties

        return fetchResults(sql);
    }

    @Override
    public List<AccountBalance> getCashDebtorsStocks() throws SQLException {
        String sql = "select s.* from TBL_ACCOUNT_BALANCE s where \n" +
                " PRODUCT_ID = (select id from TBL_PRODUCT p where p.CODE = '" + BasicProductCode.CASH.value() + "' and OFFICE_ID = " + getOfficeId() + ") and\n" +
                " COUNT > 0 and \n" +
                " PARTY_ID is not null and \n" +
                " OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<AccountBalance> getCashCreditorsStocks() throws SQLException {
        String sql = "select s.* from TBL_ACCOUNT_BALANCE s where \n" +
                " PRODUCT_ID = (select id from TBL_PRODUCT p where p.CODE = '" + BasicProductCode.CASH.value() + "' and OFFICE_ID = " + getOfficeId() + ") and\n" +
                " COUNT < 0 and \n" +
                " PARTY_ID is not null and \n" +
                " OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<AccountBalance> getOfficeCurrencyDebtorsStocks() throws SQLException {
        String sql = "select s.* from TBL_ACCOUNT_BALANCE s where PRODUCT_ID in " +
                "(select p.ID from TBL_PRODUCT p where p.PRODUCTCATEGORY_ID = " +
                "(select pc.id from TBL_PRODUCT_CATEGORY pc where pc.code = '" + BasicProductCategoryCode.CURRENCY.value() + "' and OFFICE_ID = " + getOfficeId() + ")) " +
                " and PARTY_ID is not null " +
                " and count > 0 " +
                " and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<AccountBalance> getOfficeCurrencyCreditorsStocks() throws SQLException {
        String sql = "select s.* from TBL_ACCOUNT_BALANCE s where PRODUCT_ID in " +
                "(select p.ID from TBL_PRODUCT p where p.PRODUCTCATEGORY_ID = " +
                "(select pc.id from TBL_PRODUCT_CATEGORY pc where pc.code = '" + BasicProductCategoryCode.CURRENCY.value() + "' and OFFICE_ID = " + getOfficeId() + ")) " +
                " and PARTY_ID is not null " +
                " and count < 0 " +
                " and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public Double getGoldDebtorsValue() throws SQLException {
        return getGoldDebtOrCredit(false);
    }

    @Override
    public Double getGoldCreditorsValue() throws SQLException {
        return getGoldDebtOrCredit(true);
    }

    private Double getGoldDebtOrCredit(boolean credit) throws SQLException {
        String sql = "select sum(AMOUNT) from TBL_ACCOUNT_BALANCE " +
                "where PRODUCT_ID = (select id from TBL_PRODUCT WHERE CODE = " + BasicProductCode.MELTED_GOLD.value() + " and office_id = " + getOfficeId() + ") " +
                "and office_id = " + getOfficeId() + " ";
        if (credit) {
            sql += "and AMOUNT < 0;";
        } else {
            sql += "and AMOUNT > 0;";
        }
        return getDouble(sql);
    }

    private List<AccountBalance> fetchResults(String sql) throws SQLException {
        GenericRawResults<AccountBalance> balances = queryRaw(sql, getRawRowMapper());
        if (balances != null) {
            return balances.getResults();
        }
        return null;
    }
}
