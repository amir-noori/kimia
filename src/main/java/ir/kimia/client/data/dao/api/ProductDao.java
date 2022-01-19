package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.Product;

import java.sql.SQLException;
import java.util.List;

public interface ProductDao extends BaseDao<Product, Integer> {

    public Product findByProductName(String productName) throws SQLException;

    public List<Product> getProductByCategoryCodes(String... codes) throws SQLException;

    public List<Product> getAllProducts() throws SQLException;

    public Product findByProductCode(String code) throws SQLException;

    public List<Product> findByProductCategoryId(Integer categoryId) throws SQLException;

    public List<Product> findAllProducts() throws SQLException;

    public List<Product> getAllManufactured() throws SQLException;

    public List<Product> getAllOfficeProductsWithoutStock() throws SQLException;

}
