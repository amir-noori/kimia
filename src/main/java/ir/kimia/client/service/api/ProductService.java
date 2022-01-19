package ir.kimia.client.service.api;

import com.j256.ormlite.dao.Dao;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.data.model.ProductCategory;

import java.sql.SQLException;
import java.util.List;

public interface ProductService extends BaseService {

    public Product getProductByCode(String code) throws SQLException;

    public Product getProductByName(String name) throws SQLException;

    public List<Product> getProductByCategoryCodes(String... codes) throws SQLException;

    public List<Product> getAllProducts() throws SQLException;

    public boolean isCodeAvailable(String code) throws SQLException;

    public List<Product> getCategoryProducts(ProductCategory productCategory) throws SQLException;

    public List<Product> getProductsByCategoryCode(String categoryCode) throws SQLException;

    public Product createProduct(Product product) throws SQLException;

    public Dao.CreateOrUpdateStatus createOrUpdateProduct(Product product) throws SQLException;

    public int removeProduct(Product product) throws SQLException;

    public Product updateProduct(Product product) throws SQLException;

    public List<Product> getAllManufactured() throws SQLException;

    public List<Product> getAllOfficeProductsWithoutStock() throws SQLException;

}
