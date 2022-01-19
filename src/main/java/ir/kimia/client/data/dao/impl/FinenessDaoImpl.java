package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.BankDao;
import ir.kimia.client.data.dao.api.FinenessDao;
import ir.kimia.client.data.model.Bank;
import ir.kimia.client.data.model.Fineness;

import java.sql.SQLException;
import java.util.List;

public class FinenessDaoImpl extends AbstractBaseDaoImpl<Fineness, Integer> implements FinenessDao {

    public FinenessDaoImpl(ConnectionSource connectionSource, Class<Fineness> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }


    @Override
    public List<Fineness> getAll() throws SQLException {
        return queryForAll();
    }
}
