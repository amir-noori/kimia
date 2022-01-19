package ir.kimia.client.service.api;

import ir.kimia.client.data.model.Office;
import ir.kimia.client.data.model.SystemParameter;
import ir.kimia.client.data.model.User;
import ir.kimia.client.exception.ApplicationException;

import java.sql.SQLException;
import java.util.List;

public interface SystemParameterService extends BaseService {

    public SystemParameter getParameter(String parameterName) throws SQLException;
    public void setParameter(String parameterName, String parameterValue) throws SQLException;
    public int removeParameter(String parameterName) throws SQLException;

    public SystemParameter getGlobalParameter(String parameterName) throws SQLException;
    public void setGlobalParameter(String parameterName, String parameterValue) throws SQLException;
    public int removeGlobalParameter(String parameterName) throws SQLException;

}
