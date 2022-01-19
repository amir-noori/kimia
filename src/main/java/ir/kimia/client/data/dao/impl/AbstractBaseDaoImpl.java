package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.model.Office;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractBaseDaoImpl<T, ID> extends BaseDaoImpl<T, ID> implements BaseDao<T, ID> {

    public AbstractBaseDaoImpl(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    protected Integer getOfficeId() {
        return ApplicationContext.getUserSession().getCurrentOffice().getId();
    }

    protected Office getOffice() {
        return ApplicationContext.getUserSession().getCurrentOffice();
    }

    protected <O> List<O> fetchRawResults(String sql, BiFunction<String[], String[], O> getObject) throws SQLException {
        List<O> results = new ArrayList<>();
        final GenericRawResults<String[]> queryRaw = queryRaw(sql);
        final String[] columnNames = queryRaw.getColumnNames();
        final List<String[]> queryRawResults = queryRaw.getResults();
        for (String[] record : queryRawResults) {
            final O object = getObject.apply(record, columnNames);
            results.add(object);
        }
        return results;
    }

    protected int getColumnIndex(String columnName, String[] columnNames) {
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].equals(columnName)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Double getDouble(String sql) throws SQLException {
        final GenericRawResults<String[]> rawResults = queryRaw(sql);
        if (rawResults != null) {
            final String[] firstResult = rawResults.getFirstResult();
            if (firstResult != null && firstResult.length > 0 && firstResult[0] != null) {
                return Double.parseDouble(firstResult[0]);
            }
        }
        return 0.0;
    }

    @Override
    public String getNextCode() throws SQLException {
        DatabaseTable databaseTableAnnotation = dataClass.getAnnotation(DatabaseTable.class);
        String tableName = databaseTableAnnotation.tableName();
        Integer officeId = ApplicationContext.getUserSession().getCurrentOffice().getId();
        GenericRawResults<String[]> results = queryRaw("select max(cast(code as int)) + 1 as max_id from " + tableName + " where office_id='" + officeId + "';");
        String[] firstResult = results.getFirstResult();
        if (firstResult[0] == null) {
            return "0";
        } else {
            return firstResult[0];
        }
    }

    @Override
    public String getNextAvailableCode() throws SQLException {
        DatabaseTable databaseTableAnnotation = dataClass.getAnnotation(DatabaseTable.class);
        String tableName = databaseTableAnnotation.tableName();
        Integer officeId = ApplicationContext.getUserSession().getCurrentOffice().getId();
        String sql =
                "select IFNULL(" +
                        "    select code + 1 from " + tableName + " where OFFICE_ID = '" + officeId + "'" +
                        "    and code  + 1 not in (select code from " + tableName + " where OFFICE_ID = '" + officeId + "' " +
                        "    union select max(code) + 1 from " + tableName + " where OFFICE_ID = '" + officeId + "') and rownum = 1," +
                        "    select max(code) + 1 from " + tableName + " where OFFICE_ID = '" + officeId + "'" +
                        ") as MAX_CODE;";
        GenericRawResults<String[]> results = queryRaw(sql);
        String[] firstResult = results.getFirstResult();
        if (firstResult[0] == null) {
            return "0";
        } else {
            return firstResult[0];
        }
    }
}
