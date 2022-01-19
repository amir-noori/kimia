package ir.kimia.client.service.impl;

import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.StoneDao;
import ir.kimia.client.data.model.Stone;
import ir.kimia.client.service.api.StoneService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.sql.SQLException;

public class StoneServiceImpl extends BaseServiceImpl implements StoneService {

    private static final Logger log = LogManager.getLogger(StoneServiceImpl.class);

    private final StoneDao stoneDao;

    @Inject
    public StoneServiceImpl(StoneDao stoneDao) {
        this.stoneDao = stoneDao;
    }


    @Override
    protected BaseDao getDao() {
        return stoneDao;
    }

    @Override
    public void create(Stone stone) throws SQLException {
        stone.setOffice(getOffice());
        stoneDao.create(stone);
    }

    @Override
    public int remove(Stone stone) throws SQLException {
        stone.setOffice(getOffice());
        return stoneDao.delete(stone);
    }
}
