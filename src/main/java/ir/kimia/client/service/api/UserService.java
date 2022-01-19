package ir.kimia.client.service.api;

import ir.kimia.client.data.model.User;
import ir.kimia.client.exception.ApplicationException;

import java.sql.SQLException;
import java.util.List;

public interface UserService extends BaseService {

    public User getUserDetail(String username);

    public User getUserByPassword(String password) throws SQLException;

    public boolean isPasswordUsed(String password) throws SQLException;

    public List<User> getAllUsers() throws SQLException;

    public User createUser(User user) throws SQLException, ApplicationException;

    public User checkAdminUserExists() throws SQLException, ApplicationException;

    public User editUser(User user) throws SQLException, ApplicationException;

    public User changeUserPassword(User user) throws SQLException, ApplicationException;

    public int removeUser(User user) throws ApplicationException, SQLException;

    public boolean isAdmin(String username);
}
