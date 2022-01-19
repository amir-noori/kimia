package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDao extends BaseDao<User, Integer> {

    public List<User> findByUsername(String username) throws SQLException;

}
