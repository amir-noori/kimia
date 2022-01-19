package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.ProductCategory;

import java.sql.SQLException;
import java.util.List;

public interface ProductCategoryDao extends BaseDao<ProductCategory, Integer> {

    public List<ProductCategory> findByTitle(String productCategoryTitle) throws SQLException;
    public ProductCategory findByCode(String code) throws SQLException;

}
