package ir.kimia.client.service.impl;

import com.j256.ormlite.dao.Dao;
import ir.kimia.client.common.ApplicationCache;
import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.common.CacheKeys;
import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.ProductCategoryDao;
import ir.kimia.client.data.dao.api.ProductDao;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.data.model.ProductCategory;
import ir.kimia.client.service.api.ProductService;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class ProductServiceImpl extends BaseServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final ProductCategoryDao productCategoryDao;

    @Inject
    public ProductServiceImpl(ProductDao productDao, ProductCategoryDao productCategoryDao) {
        super();
        this.productDao = productDao;
        this.productCategoryDao = productCategoryDao;
        cache.setProductService(this);
    }

    @Override
    public Product getProductByCode(String code) throws SQLException {
        return productDao.queryBuilder().where().eq("code", code).and().eq("OFFICE_ID", getOfficeId()).queryForFirst();
    }

    @Override
    public Product getProductByName(String name) throws SQLException {
        return productDao.findByProductName(name);
    }

    @Override
    public List<Product> getProductByCategoryCodes(String... codes) throws SQLException {
        return productDao.getProductByCategoryCodes(codes);
    }

    @Override
    public List<Product> getAllProducts() throws SQLException {
        return productDao.getAllProducts();
    }

    @Override
    public boolean isCodeAvailable(String code) throws SQLException {
        Product product = productDao.queryBuilder().where().eq("code", code).and().eq("OFFICE_ID", getOfficeId()).queryForFirst();
        return product == null || product.getCode() == null;
    }

    @Override
    public List<Product> getCategoryProducts(ProductCategory productCategory) throws SQLException {
        if (productCategory != null && productCategory.getId() != null) {
            return productDao.queryBuilder().where().eq("productCategory_id", productCategory.getId()).and().eq("OFFICE_ID", getOfficeId()).query();
        } else {
            return null;
        }
    }

    @Override
    public List<Product> getProductsByCategoryCode(String categoryCode) throws SQLException {
        ProductCategory productCategory = productCategoryDao.findByCode(categoryCode);
        return productDao.queryBuilder().where().eq("productCategory_id", productCategory.getId()).and().eq("OFFICE_ID", getOfficeId()).query();
    }

    @Override
    public Product createProduct(Product product) throws SQLException {
        product.setOffice(getOffice());
        productDao.create(product);
        updateCache(product);
        return product;
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdateProduct(Product product) throws SQLException {
        return productDao.createOrUpdate(product);
    }

    @Override
    public int removeProduct(Product product) throws SQLException {
        removeCache(product);
        return productDao.delete(product);
    }

    @Override
    public Product updateProduct(Product product) throws SQLException {
        productDao.update(product);
        updateCache(product);
        return product;
    }

    @Override
    public List<Product> getAllManufactured() throws SQLException {
        List<?> manufacturedCache = cache.get(ApplicationCache.PRODUCT_CATEGORY, CacheKeys.MANUFACTURED_PRODUCTS);
        if (manufacturedCache != null) {
            return (List<Product>) manufacturedCache;
        } else {
            List<Product> allManufactured = productDao.getAllManufactured();
            cache.set(ApplicationCache.PRODUCT_CATEGORY, CacheKeys.MANUFACTURED_PRODUCTS, allManufactured);
            return allManufactured;
        }
    }

    @Override
    public List<Product> getAllOfficeProductsWithoutStock() throws SQLException {
        return productDao.getAllOfficeProductsWithoutStock();
    }

    private void updateCache(Product product) throws SQLException {
        if (product != null) {
            String categoryCode = product.getProductCategory().getCode();
            Boolean countable = product.getProductCategory().getCountable();
            if (!countable && !categoryCode.equals(BasicProductCategoryCode.MSC.value()) && !categoryCode.equals(BasicProductCategoryCode.MELTED.value())) {
                // it means product is manufactured.
                List<?> objects = cache.get(ApplicationCache.PRODUCT_CATEGORY, CacheKeys.MANUFACTURED_PRODUCTS);
                if (objects == null) {
                    cache.set(ApplicationCache.PRODUCT_CATEGORY, CacheKeys.MANUFACTURED_PRODUCTS, getAllManufactured());
                } else {
                    cache.set(ApplicationCache.PRODUCT_CATEGORY, CacheKeys.MANUFACTURED_PRODUCTS, product);
                }
            }
        }
    }

    private void removeCache(Product product) {
        if (product != null) {
            String categoryCode = product.getProductCategory().getCode();
            Boolean countable = product.getProductCategory().getCountable();
            if (!countable && !categoryCode.equals(BasicProductCategoryCode.MSC.value()) && !categoryCode.equals(BasicProductCategoryCode.MELTED.value())) {
                // it means product is manufactured.
                cache.remove(ApplicationCache.PRODUCT_CATEGORY, CacheKeys.MANUFACTURED_PRODUCTS, product);
            }
        }
    }

    @Override
    protected BaseDao getDao() {
        return productDao;
    }
}
