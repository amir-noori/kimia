package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.Party;
import ir.kimia.client.data.model.Stock;

import java.sql.SQLException;
import java.util.List;

public interface StockDao extends BaseDao<Stock, Integer> {

    public Stock findByCode(String code) throws SQLException;

    public List<Stock> findByParty(Party party) throws SQLException;

    public boolean stockExistsForProduct(int productCode) throws SQLException;

    public List<Stock> getStocksByCategoryCode(String categoryCode) throws SQLException;

    public List<Stock> getStocksByPartyType(Party.PartyType partyType) throws SQLException;

    public List<Stock> getCashStocks() throws SQLException;

    public List<Stock> getOfficeManufacturedStocks() throws SQLException;

    public List<Stock> getOfficeManufacturedAndWightedStocks() throws SQLException;

    public List<Stock> getOfficeCoinStocks() throws SQLException;

    public List<Stock> getOfficeCurrencyStocks() throws SQLException;

    public List<Stock> getOfficeStoneStocks() throws SQLException;

    public List<Stock> getOfficeMeltedStocks() throws SQLException;

    public Double getMeltedWeightByScaleValue() throws SQLException;

    public Double getMeltedWeightByCaratValue() throws SQLException;

    public Double getMscWeightByScaleValue() throws SQLException;

    public Double getMscWeightByCaratValue() throws SQLException;

    public Double getManufacturedWeightsValue() throws SQLException;

    public Double getManufacturedWagesPercentageValue() throws SQLException;

}
