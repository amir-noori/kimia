package ir.kimia.client.service.impl;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.common.StoneCodes;
import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.StoneTypeDao;
import ir.kimia.client.data.model.StoneType;
import ir.kimia.client.service.api.StoneTypeService;
import ir.kimia.client.util.FxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class StoneTypeServiceImpl extends BaseServiceImpl implements StoneTypeService {

    private static final Logger log = LogManager.getLogger(StoneTypeServiceImpl.class);

    private final StoneTypeDao stoneTypeDao;

    @Inject
    public StoneTypeServiceImpl(StoneTypeDao stoneTypeDao) {
        this.stoneTypeDao = stoneTypeDao;
    }

    @Override
    protected BaseDao getDao() {
        return stoneTypeDao;
    }

    @Override
    public List<StoneType> getAllStoneTypes() throws SQLException {
        return stoneTypeDao.queryForAll();
    }

    @Override
    public StoneType getByCode(Integer code) throws SQLException {
        return stoneTypeDao.queryBuilder().where().eq("code", code).queryForFirst();
    }

    @Override
    public void initializeBasicStoneTypes() throws SQLException {
        List<StoneType> stoneTypes = stoneTypeDao.queryForAll();
        if(stoneTypes == null || stoneTypes.size() == 0) {
            StoneType pearl = new StoneType(StoneCodes.PEARL.value(), FxUtil.message("pearl"), 0.0);
            StoneType ruby = new StoneType(StoneCodes.PEARL.value(), FxUtil.message("ruby"), 0.0);
            StoneType brilliant = new StoneType(StoneCodes.PEARL.value(), FxUtil.message("brilliant"), 0.0);

            stoneTypeDao.create(pearl);
            stoneTypeDao.create(ruby);
            stoneTypeDao.create(brilliant);
        }
    }
}
