package ir.kimia.client.data.dao.api;

import com.j256.ormlite.dao.Dao;
import ir.kimia.client.data.model.Address;

import java.sql.SQLException;

public interface BaseDao<T, ID> extends Dao<T, ID> {

    public String getNextCode() throws SQLException;

    public String getNextAvailableCode() throws SQLException;

    public Double getDouble(String sql) throws SQLException;
}
