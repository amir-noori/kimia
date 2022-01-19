package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import ir.kimia.client.data.dao.api.OfficeDao;
import ir.kimia.client.data.model.Office;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class OfficeDaoImpl extends AbstractBaseDaoImpl<Office, Integer> implements OfficeDao {

    public OfficeDaoImpl(ConnectionSource connectionSource, Class<Office> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public List<Office> findByOfficeName(String officeName) throws SQLException {
        return queryForEq("officeName", officeName);
    }

    @Override
    public void removeAllOfficeData(Integer officeId) throws SQLException {
        TransactionManager.callInTransaction(connectionSource, () -> {
            if(officeId != null) {
                String sql =  "";
                sql += "DELETE FROM TBL_OFFICE WHERE ID = " + officeId + ";";
                sql += "DELETE FROM TBL_PRODUCT_CATEGORY WHERE OFFICE_ID = " + officeId + ";";
                sql += "DELETE FROM TBL_PRODUCT WHERE OFFICE_ID = " + officeId + ";";
                sql += "DELETE FROM TBL_PARTY WHERE OFFICE_ID = " + officeId + ";";
                sql += "DELETE FROM TBL_STOCK WHERE OFFICE_ID = " + officeId + ";";
                executeRaw(sql);
            }
            return null;
        });

    }
}
