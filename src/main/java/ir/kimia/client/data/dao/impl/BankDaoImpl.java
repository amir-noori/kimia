package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.BankDao;
import ir.kimia.client.data.model.Bank;

import java.sql.SQLException;
import java.util.List;

public class BankDaoImpl extends AbstractBaseDaoImpl<Bank, Integer> implements BankDao {

    public BankDaoImpl(ConnectionSource connectionSource, Class<Bank> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public List<Bank> getAllBanks() throws SQLException {
        return queryForAll();
    }
}
