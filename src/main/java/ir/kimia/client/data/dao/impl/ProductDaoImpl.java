package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.common.BasicProductCategoryCode;
import ir.kimia.client.data.dao.api.ProductDao;
import ir.kimia.client.data.model.Product;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ProductDaoImpl extends AbstractBaseDaoImpl<Product, Integer> implements ProductDao {

    public ProductDaoImpl(ConnectionSource connectionSource, Class<Product> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public Product findByProductName(String productName) throws SQLException {
        List<Product> products = queryBuilder().where().eq("productName", productName).and().eq("OFFICE_ID", getOfficeId()).query();
        if (products != null && products.size() > 0) {
            return products.get(0);
        }
        return null;
    }

    @Override
    public List<Product> getProductByCategoryCodes(String... codes) throws SQLException {
        String codesAsString = "";
        for(String code: codes) {
            codesAsString += code + "," ;
        }
        codesAsString = codesAsString.substring(0, codesAsString.length() - 1);
        String sql = "SELECT * FROM TBL_PRODUCT WHERE OFFICE_ID = " + getOfficeId() + " AND PRODUCTCATEGORY_ID IN (SELECT ID FROM TBL_PRODUCT_CATEGORY WHERE CODE IN (" + codesAsString + "));";
        GenericRawResults<Product> products = queryRaw(sql, getRawRowMapper());
        if (products != null) {
            return products.getResults();
        }
        return null;
    }

    @Override
    public List<Product> getAllProducts() throws SQLException {
        return queryForAll();
    }

    @Override
    public Product findByProductCode(String code) throws SQLException {
        List<Product> products = queryBuilder().where().eq("code", code).and().eq("OFFICE_ID", getOfficeId()).query();
        if (products != null && products.size() > 0) {
            return products.get(0);
        }
        return null;
    }

    @Override
    public List<Product> findByProductCategoryId(Integer categoryId) throws SQLException {
        return queryBuilder().where().eq("PRODUCTCATEGORY_ID", categoryId).and().eq("OFFICE_ID", getOfficeId()).query();
    }

    @Override
    public List<Product> findAllProducts() throws SQLException {
        return queryBuilder().where().eq("OFFICE_ID", getOfficeId()).query();
    }

    @Override
    public List<Product> getAllManufactured() throws SQLException {
        String sql = "SELECT * FROM TBL_PRODUCT WHERE OFFICE_ID = " + getOfficeId() + " AND PRODUCTCATEGORY_ID IN " +
                "(SELECT ID FROM TBL_PRODUCT_CATEGORY WHERE COUNTABLE = false AND " +
                "CODE NOT IN ("
                + BasicProductCategoryCode.MSC.value() + ", "
                + BasicProductCategoryCode.STONE.value() + ", "
                + BasicProductCategoryCode.MELTED.value() + "));";
        GenericRawResults<Product> products = queryRaw(sql, getRawRowMapper());
        if (products != null) {
            return products.getResults();
        }
        return null;
    }

    @Override
    public List<Product> getAllOfficeProductsWithoutStock() throws SQLException {
        String sql = "select * from TBL_PRODUCT p where p.id not in (select PRODUCT_ID from TBL_STOCK s where s.OFFICE_ID = " + getOfficeId() + ") and p.OFFICE_ID = " + getOfficeId() + ";";
        GenericRawResults<Product> products = queryRaw(sql, getRawRowMapper());
        if (products != null) {
            return products.getResults();
        }
        return null;
    }

}
