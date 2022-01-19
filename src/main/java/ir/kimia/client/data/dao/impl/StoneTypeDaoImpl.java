package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.StoneTypeDao;
import ir.kimia.client.data.model.StoneType;

import java.sql.SQLException;

public class StoneTypeDaoImpl extends AbstractBaseDaoImpl<StoneType, Integer> implements StoneTypeDao {

    public StoneTypeDaoImpl(ConnectionSource connectionSource, Class<StoneType> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

}
