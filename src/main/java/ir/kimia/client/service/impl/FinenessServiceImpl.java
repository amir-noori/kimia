package ir.kimia.client.service.impl;

import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.FinenessDao;
import ir.kimia.client.data.dao.api.PurityEvaluatorDao;
import ir.kimia.client.data.model.Fineness;
import ir.kimia.client.data.model.PurityEvaluator;
import ir.kimia.client.service.api.FinenessService;
import ir.kimia.client.service.api.PurityEvaluatorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class FinenessServiceImpl extends BaseServiceImpl implements FinenessService {

    private static final Logger log = LogManager.getLogger(FinenessServiceImpl.class);

    private final FinenessDao finenessDao;

    @Inject
    public FinenessServiceImpl(FinenessDao finenessDao) {
        this.finenessDao = finenessDao;
    }

    @Override
    protected BaseDao getDao() {
        return finenessDao;
    }


    @Override
    public List<Fineness> getAll() throws SQLException {
        return finenessDao.getAll();
    }

    @Override
    public void createOrUpdate(Fineness fineness) throws SQLException {
        fineness.setOffice(getOffice());
        finenessDao.createOrUpdate(fineness);
    }

    @Override
    public void remove(Fineness fineness) throws SQLException {
        finenessDao.delete(fineness);
    }
}
