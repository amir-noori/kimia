package ir.kimia.client.service.impl;

import ir.kimia.client.common.ResultCodes;
import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.UserDao;
import ir.kimia.client.data.model.User;
import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.api.UserService;
import ir.kimia.client.util.FxUtil;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class UserServiceImpl extends BaseServiceImpl implements UserService {

    private final UserDao userDao;

    @Inject
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User getUserDetail(String username) {
        try {
            return userDao.queryBuilder().where().eq("USERNAME", username).queryForFirst();
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Override
    public User getUserByPassword(String password) throws SQLException {
        return userDao.queryBuilder().where().eq("password", password).queryForFirst();
    }

    @Override
    public boolean isPasswordUsed(String password) throws SQLException {
        User userByPassword = getUserByPassword(password);
        return userByPassword != null;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        return userDao.queryForAll();
    }

    @Override
    public User createUser(User user) throws SQLException, ApplicationException {
        user.setAdmin(false);
        List<User> byUsername = userDao.findByUsername(user.getUsername());
        if (byUsername != null && byUsername.size() > 0) {
            throw new ApplicationException(ResultCodes.USERNAME_ALREADY_TAKEN);
        }
        if (isPasswordUsed(user.getPassword())) {
            throw new ApplicationException(ResultCodes.PASSWORD_ALREADY_TAKEN);
        }
        userDao.create(user);
        return user;
    }

    /**
     * creating admin user if it does not exists
     */
    @Override
    public User checkAdminUserExists() throws SQLException, ApplicationException {
        User admin = getUserDetail("admin");
        if (admin == null || StringUtils.isEmpty(admin.getUsername())) {
            User user = new User();
            user.setAdmin(true);
            user.setUsername("admin");
            user.setPassword("123");
            user = createUser(user);
            return user;
        }
        return admin;
    }

    @Override
    public User editUser(User user) throws SQLException, ApplicationException {
        List<User> byUsername = userDao.findByUsername(user.getUsername());
        if (byUsername != null && byUsername.size() > 0) {
            User retrievedUser = byUsername.get(0);
            if (byUsername.size() > 1 || !retrievedUser.getId().equals(user.getId())) {
                throw new ApplicationException(ResultCodes.USERNAME_ALREADY_TAKEN);
            }
            user.setId(retrievedUser.getId());
            user.setPassword(retrievedUser.getPassword());
            user.setAdmin(false);
            userDao.update(user);
        }
        return user;
    }

    @Override
    public User changeUserPassword(User user) throws SQLException, ApplicationException {
        User userByPassword = getUserByPassword(user.getPassword());
        if (userByPassword == null || userByPassword.getId().equals(user.getId()) || userByPassword.getUsername().equals(user.getUsername())) {
            userDao.update(user);
        } else {
            throw new ApplicationException(ResultCodes.PASSWORD_ALREADY_TAKEN);
        }
        return user;
    }

    @Override
    public int removeUser(User user) throws ApplicationException, SQLException {
        String username = user.getUsername();
        if (StringUtils.isNotEmpty(username)) {
            if (isAdmin(username)) {
                throw new ApplicationException(ResultCodes.CANNOT_REMOVE_ADMIN);
            } else {
                return userDao.delete(user);
            }
        }
        return 0;
    }

    @Override
    public boolean isAdmin(String username) {
        return !StringUtils.isEmpty(username) && username.toLowerCase().equals("admin");
    }

    @Override
    protected BaseDao getDao() {
        return userDao;
    }
}
