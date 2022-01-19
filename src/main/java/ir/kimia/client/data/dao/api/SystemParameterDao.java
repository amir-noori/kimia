package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.SystemParameter;

import java.sql.SQLException;

public interface SystemParameterDao extends BaseDao<SystemParameter, Integer> {

    public SystemParameter findByName(String parameterName, Integer officeId) throws SQLException;

}
