package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.StoneDao;
import ir.kimia.client.data.model.Stone;

import java.sql.SQLException;

public class StoneDaoImpl extends AbstractBaseDaoImpl<Stone, Integer> implements StoneDao {

    public StoneDaoImpl(ConnectionSource connectionSource, Class<Stone> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

}
