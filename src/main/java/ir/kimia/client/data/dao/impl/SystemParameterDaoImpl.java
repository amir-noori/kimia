package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.SystemParameterDao;
import ir.kimia.client.data.dao.api.UserDao;
import ir.kimia.client.data.model.SystemParameter;
import ir.kimia.client.data.model.User;

import java.sql.SQLException;
import java.util.List;

public class SystemParameterDaoImpl extends AbstractBaseDaoImpl<SystemParameter, Integer> implements SystemParameterDao {

    public SystemParameterDaoImpl(ConnectionSource connectionSource, Class<SystemParameter> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public SystemParameter findByName(String parameterName, Integer officeId) throws SQLException {
        if(officeId == null) {
            return queryBuilder().where().eq("name", parameterName).and().isNull("OFFICE_ID").queryForFirst();
        } else {
            return queryBuilder().where().eq("name", parameterName).and().eq("OFFICE_ID", officeId).queryForFirst();
        }
    }
}
