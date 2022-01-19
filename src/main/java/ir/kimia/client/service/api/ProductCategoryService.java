package ir.kimia.client.service.api;

import ir.kimia.client.data.model.ProductCategory;
import ir.kimia.client.exception.ApplicationException;

import java.sql.SQLException;
import java.util.List;

public interface ProductCategoryService extends BaseService {

    public ProductCategory getProductCategoryById(Integer id) throws SQLException;

    public List<ProductCategory> getProductCategoryByTitle(String title) throws SQLException;

    /*
        `Hidden categories are cash (Rial) for now
     */
    public List<ProductCategory> getAllProductCategories(boolean showHiddenCategories) throws SQLException;

    public ProductCategory createProductCategory(ProductCategory productCategory) throws SQLException;

    public ProductCategory updateProductCategory(ProductCategory productCategory) throws SQLException, ApplicationException;

    public void removeProductCategory(ProductCategory productCategory) throws SQLException, ApplicationException;

    public ProductCategory getProductCategoryByCode(String code) throws SQLException;

}
