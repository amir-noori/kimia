package ir.kimia.client.service.impl;

import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.SystemParameterDao;
import ir.kimia.client.data.model.SystemParameter;
import ir.kimia.client.service.api.SystemParameterService;

import javax.inject.Inject;
import java.sql.SQLException;

public class SystemParameterServiceImpl extends BaseServiceImpl implements SystemParameterService {

    private final SystemParameterDao systemParameterDao;

    @Inject
    public SystemParameterServiceImpl(SystemParameterDao systemParameterDao) {
        this.systemParameterDao = systemParameterDao;
    }

    @Override
    protected BaseDao getDao() {
        return systemParameterDao;
    }

    @Override
    public SystemParameter getParameter(String parameterName) throws SQLException {
        return systemParameterDao.findByName(parameterName, getOfficeId());
    }

    @Override
    public void setParameter(String parameterName, String parameterValue) throws SQLException {
        SystemParameter retrievedParameter = systemParameterDao.findByName(parameterName, getOfficeId());
        if (retrievedParameter != null) {
            retrievedParameter.setValue(parameterValue);
        } else {
            retrievedParameter = new SystemParameter();
            retrievedParameter.setName(parameterName);
            retrievedParameter.setValue(parameterValue);
            retrievedParameter.setOffice(getOffice());
        }
        systemParameterDao.createOrUpdate(retrievedParameter);
    }

    @Override
    public int removeParameter(String parameterName) throws SQLException {
        SystemParameter retrievedParameter = systemParameterDao.findByName(parameterName, getOfficeId());
        return systemParameterDao.delete(retrievedParameter);
    }

    @Override
    public SystemParameter getGlobalParameter(String parameterName) throws SQLException {
        return systemParameterDao.findByName(parameterName, null);
    }

    @Override
    public void setGlobalParameter(String parameterName, String parameterValue) throws SQLException {
        SystemParameter retrievedParameter = systemParameterDao.findByName(parameterName, null);
        if (retrievedParameter == null) {
            retrievedParameter = new SystemParameter();
            retrievedParameter.setName(parameterName);
        }
        retrievedParameter.setValue(parameterValue);
        systemParameterDao.createOrUpdate(retrievedParameter);
    }

    @Override
    public int removeGlobalParameter(String parameterName) throws SQLException {
        SystemParameter retrievedParameter = systemParameterDao.findByName(parameterName, null);
        return systemParameterDao.delete(retrievedParameter);
    }
}
