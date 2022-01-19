package ir.kimia.client.service.impl;

import com.j256.ormlite.stmt.query.In;
import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.common.BasicProductCode;
import ir.kimia.client.data.dao.api.*;
import ir.kimia.client.data.model.Office;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.data.model.ProductCategory;
import ir.kimia.client.service.api.OfficeService;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class OfficeServiceImpl extends BaseServiceImpl implements OfficeService {

    private final OfficeDao officeDao;
    private final ProductDao productDao;
    private final ProductCategoryDao productCategoryDao;
    private final StockDao stockDao;
    private final PartyDao partyDao;

    @Inject
    public OfficeServiceImpl(OfficeDao officeDao, ProductCategoryDao productCategoryDao, ProductDao productDao, StockDao stockDao, PartyDao partyDao) {
        this.officeDao = officeDao;
        this.productCategoryDao = productCategoryDao;
        this.productDao = productDao;
        this.stockDao = stockDao;
        this.partyDao = partyDao;
    }


    @Override
    public Office getOfficeById(Integer id) throws SQLException {
        return officeDao.queryForId(id);
    }

    @Override
    public List<Office> getAllOffices() throws SQLException {
        return officeDao.queryForAll();
    }

    @Override
    public Office createOffice(Office office) throws SQLException {
        office.setCreateTime(new Date());
        officeDao.create(office);
        createOfficeInitialData(office);
        return office;
    }

    @Override
    public void removeOffice(Office office) throws SQLException {
        Integer officeId = office.getId();
        officeDao.removeAllOfficeData(officeId);
    }

    /**
        setting basic product related data for a newly crated office.
     */
    private void createOfficeInitialData(Office office) throws SQLException {
        ProductCategory cashCategory = new ProductCategory(BasicProductCategoryCode.CASH.value(), ApplicationContext.getResourceBundle().getString("cash"), false, true, null, office);
        ProductCategory meltedCategory = new ProductCategory(BasicProductCategoryCode.MELTED.value(), ApplicationContext.getResourceBundle().getString("melted"), false, false, null, office);
        ProductCategory mscCategory = new ProductCategory(BasicProductCategoryCode.MSC.value(), ApplicationContext.getResourceBundle().getString("msc"), false, false, null, office);
        ProductCategory manufacturedCategory = new ProductCategory(BasicProductCategoryCode.MANUFACTURED.value(), ApplicationContext.getResourceBundle().getString("manufactured"), false, false, null, office);
        ProductCategory currencyCategory = new ProductCategory(BasicProductCategoryCode.CURRENCY.value(), ApplicationContext.getResourceBundle().getString("currency"), false, true, null, office);
        ProductCategory coinCategory = new ProductCategory(BasicProductCategoryCode.COIN.value(), ApplicationContext.getResourceBundle().getString("coin"), false, true, null, office);
        ProductCategory stoneCategory = new ProductCategory(BasicProductCategoryCode.STONE.value(), ApplicationContext.getResourceBundle().getString("stone"), false, false, null, office);

        productCategoryDao.create(cashCategory);
        productCategoryDao.create(meltedCategory);
        productCategoryDao.create(mscCategory);
        productCategoryDao.create(manufacturedCategory);
        productCategoryDao.create(currencyCategory);
        productCategoryDao.create(coinCategory);
        productCategoryDao.create(stoneCategory);

        Product meltedGold = new Product(BasicProductCode.MELTED_GOLD.value(), ApplicationContext.getResourceBundle().getString("melted.gold"), office, meltedCategory);
        meltedGold.setCarat(ApplicationContext.getOfficeCarat());

        Product mscGold = new Product(BasicProductCode.MSC_GOLD.value(), ApplicationContext.getResourceBundle().getString("msc.gold"), office, mscCategory);
        mscGold.setCarat(ApplicationContext.getOfficeCarat());


        Product rial = new Product(BasicProductCode.CASH.value(), ApplicationContext.getResourceBundle().getString("cash"), office, cashCategory);
        productDao.create(rial);
        productDao.create(meltedGold);
        productDao.create(mscGold);
    }

    @Override
    public Office findOfficeByName(String officeName) throws SQLException {
        return officeDao.queryBuilder().where().eq("officeName", officeName).queryForFirst();
    }

    @Override
    protected BaseDao getDao() {
        return officeDao;
    }
}
