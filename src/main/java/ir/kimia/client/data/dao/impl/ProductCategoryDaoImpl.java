package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.ProductCategoryDao;
import ir.kimia.client.data.model.ProductCategory;

import java.sql.SQLException;
import java.util.List;

public class ProductCategoryDaoImpl extends AbstractBaseDaoImpl<ProductCategory, Integer> implements ProductCategoryDao {

    public ProductCategoryDaoImpl(ConnectionSource connectionSource, Class<ProductCategory> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public List<ProductCategory> findByTitle(String productCategoryTitle) throws SQLException {
        return queryBuilder().where().eq("title", productCategoryTitle).and().eq("OFFICE_ID", getOfficeId()).query();
    }

    @Override
    public ProductCategory findByCode(String code) throws SQLException {
        return queryBuilder().where().eq("code", code).and().eq("OFFICE_ID", getOfficeId()).queryForFirst();
    }
}
