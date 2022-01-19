package ir.kimia.client.service.impl;

import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.PurityEvaluatorDao;
import ir.kimia.client.data.model.PurityEvaluator;
import ir.kimia.client.service.api.PurityEvaluatorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class PurityEvaluatorServiceImpl extends BaseServiceImpl implements PurityEvaluatorService {

    private static final Logger log = LogManager.getLogger(PurityEvaluatorServiceImpl.class);

    private final PurityEvaluatorDao purityEvaluatorDao;

    @Inject
    public PurityEvaluatorServiceImpl(PurityEvaluatorDao purityEvaluatorDao) {
        this.purityEvaluatorDao = purityEvaluatorDao;
    }

    @Override
    protected BaseDao getDao() {
        return purityEvaluatorDao;
    }

    @Override
    public List<PurityEvaluator> getAll() throws SQLException {
        return purityEvaluatorDao.getAll();
    }

    @Override
    public void createOrUpdate(PurityEvaluator purityEvaluator) throws SQLException {
        purityEvaluatorDao.createOrUpdate(purityEvaluator);
    }

    @Override
    public void remove(PurityEvaluator purityEvaluator) throws SQLException {
        purityEvaluatorDao.delete(purityEvaluator);
    }
}
