package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.BankDao;
import ir.kimia.client.data.dao.api.PurityEvaluatorDao;
import ir.kimia.client.data.model.Bank;
import ir.kimia.client.data.model.PurityEvaluator;

import java.sql.SQLException;
import java.util.List;

public class PurityEvaluatorDaoImpl extends AbstractBaseDaoImpl<PurityEvaluator, Integer> implements PurityEvaluatorDao {

    public PurityEvaluatorDaoImpl(ConnectionSource connectionSource, Class<PurityEvaluator> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public List<PurityEvaluator> getAll() throws SQLException {
        return queryForAll();
    }
}
