package ir.kimia.client.service.impl;

import com.j256.ormlite.dao.Dao;
import ir.kimia.client.common.BasicProductCode;
import ir.kimia.client.common.Constants;
import ir.kimia.client.data.dao.api.*;
import ir.kimia.client.data.model.*;
import ir.kimia.client.service.api.StockService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class StockServiceImpl extends BaseServiceImpl implements StockService {

    private static final Logger log = LogManager.getLogger(BaseServiceImpl.class);


    private final StockDao stockDao;
    private final ProductDao productDao;
    private final StoneDao stoneDao;
    private final FinenessDao finenessDao;

    @Inject
    public StockServiceImpl(StockDao stockDao, ProductDao productDao, StoneDao stoneDao, FinenessDao finenessDao) {
        this.stockDao = stockDao;
        this.productDao = productDao;
        this.stoneDao = stoneDao;
        this.finenessDao = finenessDao;
    }

    @Override
    public Stock getStockById(Integer id) throws SQLException {
        return stockDao.queryForId(id);
    }

    @Override
    public List<Stock> getOfficeStocks() throws SQLException {
        return stockDao.queryForEq("OFFICE_ID", getOfficeId());
    }

    @Override
    public List<Stock> getOfficeCurrentProductStocks() throws SQLException {
        return stockDao.queryBuilder().where()
                .eq("OFFICE_ID", getOfficeId()).and()
                .isNull("PARTY_ID").query();
    }

    @Override
    public void initOfficeProductStocksWithZeroValues() throws SQLException {
        List<Product> allProducts = productDao.getAllOfficeProductsWithoutStock();
        // removing Gold product because there might be many Melted and Msc stocks for an office.
        allProducts = allProducts.stream().filter(product -> {
            String code = product.getCode();
            if (code.equals(BasicProductCode.MELTED_GOLD.value()) || code.equals(BasicProductCode.MSC_GOLD.value())) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList());
//        for (Product product : allProducts) {
//            Stock officeCurrentStockByProduct = getOfficeCurrentStockByProduct(product);
//            if (officeCurrentStockByProduct == null) {
//                Stock stock = new Stock();
//                stock.setOffice(getOffice());
//                stock.setParty(null); // null means the stock is in the office warehouse.
//                stock.setCount(0.0);
//                stock.setWeight(0.0);
//                stock.setProduct(product);
//                stock.setCode(stockDao.getNextAvailableCode());
//                log.debug("creating stock with zero value: " + stock);
//                stockDao.create(stock);
//            }
//        }

        int counter = 0;
        StringBuilder insertStatement = new StringBuilder("");
        String nextAvailableCodeString = stockDao.getNextAvailableCode();
        Integer nextAvailableCode = Integer.valueOf(nextAvailableCodeString);
        for (Product product : allProducts) {
            insertStatement.append("insert into TBL_STOCK (CODE, PARTY_ID, PRODUCT_ID, WEIGHT, COUNT, OFFICE_ID, WAGEPERCENTAGE, WAGEFEE, CARAT, FINENESS_ID)\n" +
                    "VALUES (" + (nextAvailableCode + counter) + ", null, " + product.getId() + ", 0, 0, " + getOfficeId() + ", 0, 0, " + Constants.BASE_CARAT + ", null);\n");
            counter++;
            if (counter % 1000 == 0) {
                stoneDao.executeRaw(insertStatement.toString());
                insertStatement.setLength(0);
            }
        }

        if (insertStatement.length() > 0) { // execute the remained.
            stoneDao.executeRaw(insertStatement.toString());
        }

    }

    @Override
    public Stock getOfficeCurrentStockByProduct(Product product) throws SQLException {
        return stockDao.queryBuilder().where()
                .eq("PRODUCT_ID", product.getId()).and()
                .eq("OFFICE_ID", getOfficeId()).and()
                .isNull("PARTY_ID").queryForFirst();
    }

    @Override
    public Stock getStockByPartyProduct(Party party, Product product) throws SQLException {
        return stockDao.queryBuilder().where()
                .eq("PRODUCT_ID", product.getId()).and()
                .eq("PARTY_ID", party.getId()).and()
                .eq("OFFICE_ID", getOfficeId())
                .queryForFirst();
    }

    @Override
    public Stock getStockByProduct(Product product) throws SQLException {
        return stockDao.queryBuilder().where()
                .eq("PRODUCT_ID", product.getId()).and()
                .eq("OFFICE_ID", getOfficeId())
                .queryForFirst();
    }

    @Override
    public Stock getStockByCode(String code) throws SQLException {
        return stockDao.queryBuilder().where().eq("code", code).and().eq("OFFICE_ID", getOfficeId()).queryForFirst();
    }

    @Override
    public List<Stock> getStocksByCategoryCode(String categoryCode) throws SQLException {
        return stockDao.getStocksByCategoryCode(categoryCode);
    }

    @Override
    public boolean stockExistsForProduct(int productId) throws SQLException {
        return stockDao.stockExistsForProduct(productId);
    }

    @Override
    public Stock createStock(Stock stock) throws SQLException {
        stock.setOffice(getOffice());
        if (stock.getCode() == null) {
            stock.setCode(getNextCode());
        }
        createOrUpdateStones(stock);
        createOrUpdateFineness(stock);
        stockDao.create(stock);
        return stock;
    }

    @Override
    public int removeStock(Stock stock) throws SQLException {
        removeStones(stock);
        removeFineness(stock);
        return stockDao.delete(stock);
    }

    @Override
    public Dao.CreateOrUpdateStatus updateOrCreateStock(Stock stock) throws SQLException {
        stock.setOffice(getOffice());
        createOrUpdateStones(stock);
        createOrUpdateFineness(stock);
        return stockDao.createOrUpdate(stock);
    }

    private void createOrUpdateFineness(Stock stock) throws SQLException {
        Fineness fineness = stock.getFineness();
        if (fineness != null) {
            fineness.setOffice(getOffice());
            finenessDao.createOrUpdate(fineness);
        }
    }

    private void createOrUpdateStones(Stock stock) throws SQLException {
        Collection<Stone> stones = stock.getStones();
        if (stones != null) {
            for (Stone stone : stones) {
                stone.setStock(stock);
                stone.setOffice(getOffice());
                stoneDao.createOrUpdate(stone);
            }
        }
    }

    private void removeFineness(Stock stock) throws SQLException {
        Fineness fineness = stock.getFineness();
        if (fineness != null) {
            finenessDao.delete(fineness);
        }
    }

    private void removeStones(Stock stock) throws SQLException {
        Collection<Stone> stones = stock.getStones();
        if (stones != null) {
            for (Stone stone : stones) {
                stoneDao.delete(stone);
            }
        }
    }

    @Override
    public void createStocks(List<Stock> stocks) throws SQLException {
        for (Stock stock : stocks) {
            stock.setOffice(getOffice());
            createOrUpdateStones(stock);
            stockDao.create(stock);
        }
    }

    @Override
    public List<Stock> findStockByParty(Party party) throws SQLException {
        return stockDao.findByParty(party);
    }

    @Override
    public List<Stock> getStocksByPartyType(Party.PartyType partyType) throws SQLException {
        return stockDao.getStocksByPartyType(partyType);
    }

    @Override
    public List<Stock> getCashStocks() throws SQLException {
        return stockDao.getCashStocks();
    }

    @Override
    public List<Stock> getOfficeManufacturedStocks() throws SQLException {
        return stockDao.getOfficeManufacturedStocks();
    }

    @Override
    public List<Stock> getOfficeManufacturedAndWightedStocks() throws SQLException {
        return stockDao.getOfficeManufacturedAndWightedStocks();
    }

    @Override
    public List<Stock> getOfficeCoinStocks() throws SQLException {
        return stockDao.getOfficeCoinStocks();
    }

    @Override
    public List<Stock> getOfficeCurrencyStocks() throws SQLException {
        return stockDao.getOfficeCurrencyStocks();
    }

    @Override
    public List<Stock> getOfficeStoneStocks() throws SQLException {
        return stockDao.getOfficeStoneStocks();
    }

    @Override
    public List<Stock> getOfficeMeltedStocks() throws SQLException {
        return stockDao.getOfficeMeltedStocks();
    }

    @Override
    public Double getMeltedWeightByScaleValue() throws SQLException {
        return stockDao.getMeltedWeightByScaleValue();
    }

    @Override
    public Double getMeltedWeightByCaratValue() throws SQLException {
        return stockDao.getMeltedWeightByCaratValue();
    }

    @Override
    public Double getMscWeightByScaleValue() throws SQLException {
        return stockDao.getMscWeightByScaleValue();
    }

    @Override
    public Double getMscWeightByCaratValue() throws SQLException {
        return stockDao.getMscWeightByCaratValue();
    }

    @Override
    public Double getManufacturedWeightsValue() throws SQLException {
        return stockDao.getManufacturedWeightsValue();
    }

    @Override
    public Double getManufacturedWagesPercentageValue() throws SQLException {
        return stockDao.getManufacturedWagesPercentageValue();
    }

    @Override
    protected BaseDao getDao() {
        return stockDao;
    }
}
