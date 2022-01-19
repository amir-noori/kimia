package ir.kimia.client.service.api;

import com.j256.ormlite.dao.Dao;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.data.model.Stock;

import java.sql.SQLException;
import java.util.List;

public interface StockService extends BaseService {

    public Stock getStockById(Integer id) throws SQLException;

    public List<Stock> getOfficeStocks() throws SQLException;

    public List<Stock> getOfficeCurrentProductStocks() throws SQLException;

    public void initOfficeProductStocksWithZeroValues() throws SQLException;

    public Stock getOfficeCurrentStockByProduct(Product product) throws SQLException;

    public Stock getStockByPartyProduct(Party party, Product product) throws SQLException;

    public Stock getStockByProduct(Product product) throws SQLException;

    public Stock getStockByCode(String code) throws SQLException;

    public List<Stock> getStocksByCategoryCode(String categoryCode) throws SQLException;

    public boolean stockExistsForProduct(int productId) throws SQLException;

    public Stock createStock(Stock stock) throws SQLException;

    public int removeStock(Stock stock) throws SQLException;

    public Dao.CreateOrUpdateStatus updateOrCreateStock(Stock stock) throws SQLException;

    public void createStocks(List<Stock> stocks) throws SQLException;

    public List<Stock> findStockByParty(Party party) throws SQLException;

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
