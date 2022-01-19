package ir.kimia.client.service.impl;

import com.j256.ormlite.stmt.Where;
import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.common.ResultCodes;
import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.ProductCategoryDao;
import ir.kimia.client.data.dao.api.ProductDao;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.data.model.ProductCategory;
import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.api.ProductCategoryService;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class ProductCategoryServiceImpl extends BaseServiceImpl implements ProductCategoryService {

    private final ProductCategoryDao productCategoryDao;
    private final ProductDao productDao;

    @Inject
    public ProductCategoryServiceImpl(ProductCategoryDao productCategoryDao, ProductDao productDao) {
        this.productCategoryDao = productCategoryDao;
        this.productDao = productDao;
    }

    @Override
    public ProductCategory getProductCategoryById(Integer id) throws SQLException {
        return productCategoryDao.queryForId(id);
    }

    @Override
    public List<ProductCategory> getProductCategoryByTitle(String title) throws SQLException {
        return productCategoryDao.queryForEq("TITLE", title);
    }

    @Override
    public List<ProductCategory> getAllProductCategories(boolean showHiddenCategories) throws SQLException {
        Integer officeId = ApplicationContext.getUserSession().getCurrentOffice().getId();
        Where<ProductCategory, Integer> whereClause = productCategoryDao.queryBuilder().where().eq("OFFICE_ID", officeId);
        if (!showHiddenCategories) {
            whereClause.and().not().eq("CODE", BasicProductCategoryCode.CASH.value());
            whereClause.and().not().eq("CODE", BasicProductCategoryCode.MELTED.value());
            whereClause.and().not().eq("CODE", BasicProductCategoryCode.MSC.value());
        }
        return whereClause.query();
    }

    @Override
    public ProductCategory createProductCategory(ProductCategory productCategory) throws SQLException {
        productCategory.setOffice(ApplicationContext.getUserSession().getCurrentOffice());
        productCategoryDao.create(productCategory);
        return productCategory;
    }

    @Override
    public ProductCategory updateProductCategory(ProductCategory productCategory) throws SQLException, ApplicationException {
        checkProductCategoryBeforeEditOrRemove(productCategory);
        productCategoryDao.update(productCategory);
        return productCategory;
    }

    @Override
    public void removeProductCategory(ProductCategory productCategory) throws SQLException, ApplicationException {
        checkProductCategoryBeforeEditOrRemove(productCategory);
        productCategoryDao.delete(productCategory);
    }

    private void checkProductCategoryBeforeEditOrRemove(ProductCategory productCategory) throws SQLException, ApplicationException {
        List<Product> byProductCategoryId = productDao.findByProductCategoryId(productCategory.getId());
        if (byProductCategoryId != null && byProductCategoryId.size() > 0) {
            throw new ApplicationException(ResultCodes.CANNOT_REMOVE_PRODUCT_CATEGORY_WHICH_HAS_PRODUCT);
        }
    }

    @Override
    public ProductCategory getProductCategoryByCode(String code) throws SQLException {
        return productCategoryDao.queryBuilder().where().eq("CODE", code).queryForFirst();
    }

    @Override
    protected BaseDao getDao() {
        return productCategoryDao;
    }
}
