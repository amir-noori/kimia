package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.common.BasicProductCode;
import ir.kimia.client.common.Constants;
import ir.kimia.client.data.dao.api.StockDao;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.data.model.Stock;

import java.sql.SQLException;
import java.util.List;
import java.util.function.BiFunction;

public class StockDaoImpl extends AbstractBaseDaoImpl<Stock, Integer> implements StockDao {

    public StockDaoImpl(ConnectionSource connectionSource, Class<Stock> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public Stock findByCode(String code) throws SQLException {
        return queryBuilder().where().eq("code", code).and().eq("OFFICE_ID", getOfficeId()).queryForFirst();
    }

    @Override
    public List<Stock> findByParty(Party party) throws SQLException {
        return queryBuilder().where()
                .eq("PARTY_ID", party.getId()).and()
                .eq("OFFICE_ID", getOfficeId())
                .query();
    }

    @Override
    public boolean stockExistsForProduct(int productId) throws SQLException {
        List<Stock> result = queryBuilder().where()
                .eq("PRODUCT_ID", productId).and()
                .eq("OFFICE_ID", getOfficeId())
                .query();
        return result != null && result.size() > 0;
    }

    @Override
    public List<Stock> getStocksByCategoryCode(String categoryCode) throws SQLException {
        String sql = "select s.* from TBL_STOCK s where s.OFFICE_ID = " + getOfficeId() + " " +
                "and s.PRODUCT_ID in (select p.ID from TBL_PRODUCT p where p.OFFICE_ID = " + getOfficeId() + " and " +
                "p.PRODUCTCATEGORY_ID in (select pc.id from TBL_PRODUCT_CATEGORY pc where pc.code = '" + categoryCode + "' and pc.OFFICE_ID = " + getOfficeId() + " ) );";
        return fetchResults(sql);
    }

    @Override
    public List<Stock> getStocksByPartyType(Party.PartyType partyType) throws SQLException {
        String sql = "select s.* from TBL_STOCK s where s.OFFICE_ID = " + getOfficeId() + " " +
                "and s.PARTY_ID  in (select p.ID from TBL_PARTY p where p.OFFICE_ID = " + getOfficeId() + " ";
        if (!partyType.equals(Party.PartyType.ALL)) {
            sql += " and p.PARTYTYPE = " + partyType.ordinal();
        }
        sql += " and p.CODE not in (1000, 2000));"; // 1000 and 2000 party codes are special types of parties

        return fetchResults(sql);
    }

    private List<Stock> fetchResults(String sql) throws SQLException {
        GenericRawResults<Stock> stocks = queryRaw(sql, getRawRowMapper());
        if (stocks != null) {
            return stocks.getResults();
        }
        return null;
    }

    @Override
    public List<Stock> getCashStocks() throws SQLException {
        String sql = "select s.* from TBL_STOCK s where \n" +
                " PRODUCT_ID = (select id from TBL_PRODUCT p where p.CODE = '" + BasicProductCode.CASH.value() + "' and OFFICE_ID = " + getOfficeId() + ") and\n" +
                " PARTY_ID is null and \n" +
                " OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<Stock> getOfficeManufacturedStocks() throws SQLException {
        String sql = "select s.* from TBL_STOCK s where PRODUCT_ID in " +
                "(select p.ID from TBL_PRODUCT p where p.PRODUCTCATEGORY_ID = " +
                "(select pc.id from TBL_PRODUCT_CATEGORY pc where pc.code = '" + BasicProductCategoryCode.MANUFACTURED.value() + "' and OFFICE_ID = " + getOfficeId() + ")) " +
                " and PARTY_ID is null " +
                " and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<Stock> getOfficeManufacturedAndWightedStocks() throws SQLException {
        String sql = "select s.* from TBL_STOCK s where PRODUCT_ID in " +
                "(select p.ID from TBL_PRODUCT p where p.PRODUCTCATEGORY_ID in (" +
                "(select pc.id from TBL_PRODUCT_CATEGORY pc where pc.countable = false and " +
                "pc.code not in ('" + BasicProductCategoryCode.STONE.value() + "', '" + BasicProductCategoryCode.MELTED.value() + "', '" + BasicProductCategoryCode.MSC.value() + "') and OFFICE_ID = " + getOfficeId() + "))) " +
                " and PARTY_ID is null " +
                " and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<Stock> getOfficeCoinStocks() throws SQLException {
        String sql = "select s.* from TBL_STOCK s where PRODUCT_ID in " +
                "(select p.ID from TBL_PRODUCT p where p.PRODUCTCATEGORY_ID = " +
                "(select pc.id from TBL_PRODUCT_CATEGORY pc where pc.code = '" + BasicProductCategoryCode.COIN.value() + "' and OFFICE_ID = " + getOfficeId() + ")) " +
                " and PARTY_ID is null " +
                " and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<Stock> getOfficeCurrencyStocks() throws SQLException {
        String sql = "select s.* from TBL_STOCK s where PRODUCT_ID in " +
                "(select p.ID from TBL_PRODUCT p where p.PRODUCTCATEGORY_ID = " +
                "(select pc.id from TBL_PRODUCT_CATEGORY pc where pc.code = '" + BasicProductCategoryCode.CURRENCY.value() + "' and OFFICE_ID = " + getOfficeId() + ")) " +
                " and PARTY_ID is null " +
                " and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<Stock> getOfficeStoneStocks() throws SQLException {
        String sql = "select s.* from TBL_STOCK s where PRODUCT_ID in " +
                "(select p.ID from TBL_PRODUCT p where p.PRODUCTCATEGORY_ID = " +
                "(select pc.id from TBL_PRODUCT_CATEGORY pc where pc.code = '" + BasicProductCategoryCode.STONE.value() + "' and OFFICE_ID = " + getOfficeId() + ")) " +
                " and PARTY_ID is null " +
                " and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<Stock> getOfficeMeltedStocks() throws SQLException {
        String sql = "select s.* from TBL_STOCK s where PRODUCT_ID in " +
                "(select p.ID from TBL_PRODUCT p where p.PRODUCTCATEGORY_ID = " +
                "(select pc.id from TBL_PRODUCT_CATEGORY pc where pc.code = '" + BasicProductCategoryCode.MELTED.value() + "' and OFFICE_ID = " + getOfficeId() + ")) " +
                " and PARTY_ID is null " +
                " and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public Double getMeltedWeightByScaleValue() throws SQLException {
        return getMeltedWeightValue(true);
    }

    @Override
    public Double getMeltedWeightByCaratValue() throws SQLException {
        return getMeltedWeightValue(false);
    }

    @Override
    public Double getMscWeightByScaleValue() throws SQLException {
        String sql = "select sum(WEIGHT) from TBL_STOCK where PRODUCT_ID = (select id from TBL_PRODUCT WHERE CODE = " +
                BasicProductCode.MSC_GOLD.value() + " and office_id = " + getOfficeId() + ") " +
                "and office_id = " + getOfficeId() + ";";
        return getDouble(sql);
    }

    @Override
    public Double getMscWeightByCaratValue() throws SQLException {
        String sql = "select sum(WEIGHT * CARAT / " + +Constants.BASE_CARAT + ") from TBL_STOCK where PRODUCT_ID = (select id from TBL_PRODUCT WHERE CODE = " +
                BasicProductCode.MSC_GOLD.value() + " and office_id = " + getOfficeId() + ") " +
                "and office_id = " + getOfficeId() + ";";
        return getDouble(sql);
    }

    private Double getMeltedWeightValue(boolean byScale) throws SQLException {
        String sql = "";
        if (byScale) {
            sql = "select sum(WEIGHT) from TBL_STOCK ";
        } else {
            sql = "select sum(WEIGHT * CARAT / " + Constants.BASE_CARAT + ") from TBL_STOCK ";
        }
        sql += "where PRODUCT_ID = (select id from TBL_PRODUCT WHERE CODE = " + BasicProductCode.MELTED_GOLD.value() + " and office_id = " + getOfficeId() + ") " +
                "and office_id = " + getOfficeId() + ";";
        return getDouble(sql);
    }

    @Override
    public Double getManufacturedWeightsValue() throws SQLException {
        String sql = "select sum(WEIGHT) from TBL_STOCK where " +
                "PRODUCT_ID in (select id from TBL_PRODUCT where " +
                "PRODUCTCATEGORY_ID in (select id from TBL_PRODUCT_CATEGORY pc where pc.countable = false and pc.code " +
                "not in(" + BasicProductCategoryCode.MSC.value() + ", " + BasicProductCategoryCode.MELTED.value() + ", " + BasicProductCategoryCode.STONE.value() + ") ) " +
                "and OFFICE_ID = " + getOfficeId() + ") " +
                " and office_id = " + getOfficeId() + ";";
        return getDouble(sql);
    }

    @Override
    public Double getManufacturedWagesPercentageValue() throws SQLException {
        String sql = "select sum(WAGEPERCENTAGE *  WEIGHT/ 100) from TBL_STOCK where " +
                "PRODUCT_ID in (select id from TBL_PRODUCT where " +
                "PRODUCTCATEGORY_ID in (select id from TBL_PRODUCT_CATEGORY pc where pc.countable = false and pc.code " +
                "not in(" + BasicProductCategoryCode.MSC.value() + ", " + BasicProductCategoryCode.MELTED.value() + ", " + BasicProductCategoryCode.STONE.value() + ") ) " +
                "and OFFICE_ID = " + getOfficeId() + ") " +
                " and office_id = " + getOfficeId() + ";";
        return getDouble(sql);
    }
}
