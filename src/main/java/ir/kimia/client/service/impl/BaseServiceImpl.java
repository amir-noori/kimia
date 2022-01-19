package ir.kimia.client.service.impl;

import ir.kimia.client.common.ApplicationCache;
import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.model.Office;
import ir.kimia.client.service.api.BaseService;
import ir.kimia.client.util.FxUtil;

import java.sql.SQLException;

public abstract class BaseServiceImpl implements BaseService {

    protected ApplicationCache cache;

    public BaseServiceImpl() {
        this.cache = new ApplicationCache();
    }

    protected abstract BaseDao getDao();

    protected Integer getOfficeId() {
        return ApplicationContext.getUserSession().getCurrentOffice().getId();
    }

    protected Office getOffice() {
        return ApplicationContext.getUserSession().getCurrentOffice();
    }

    @Override
    public String getNextCode() {
        try {
            return getDao().getNextCode();
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Override
    public String getNextAvailableCode() {
        try {
            return getDao().getNextAvailableCode();
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }
}
