package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.UserDao;
import ir.kimia.client.data.model.User;

import java.sql.SQLException;
import java.util.List;

public class UserDaoImpl extends AbstractBaseDaoImpl<User, Integer> implements UserDao {

    public UserDaoImpl(ConnectionSource connectionSource, Class<User> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public List<User> findByUsername(String username) throws SQLException {
        return queryForEq("username", username);
    }
}
