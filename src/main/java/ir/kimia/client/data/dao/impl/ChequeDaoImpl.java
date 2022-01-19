package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.ChequeDao;
import ir.kimia.client.data.model.Cheque;

import java.sql.SQLException;
import java.util.List;

public class ChequeDaoImpl extends AbstractBaseDaoImpl<Cheque, Integer> implements ChequeDao {

    public ChequeDaoImpl(ConnectionSource connectionSource, Class<Cheque> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }


    @Override
    public List<Cheque> findAll() throws SQLException {
        return queryBuilder().where().eq("OFFICE_ID", getOfficeId()).query();
    }

    @Override
    public Cheque findByChequeNumber(String chequeNumber) throws SQLException {
        return queryBuilder().where().eq("CHEQUENUMBER", chequeNumber).queryForFirst();
    }

    @Override
    public List<Cheque> getInOfficeInventoryChequeStocks() throws SQLException {
        String sql = "select * from TBL_CHEQUE where INOFFICEINVENTORY = true and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    @Override
    public List<Cheque> getHoldByPartyChequeStocks() throws SQLException {
        String sql = "select * from TBL_CHEQUE where INOFFICEINVENTORY = false and OFFICE_ID = " + getOfficeId() + ";";
        return fetchResults(sql);
    }

    private List<Cheque> fetchResults(String sql) throws SQLException {
        GenericRawResults<Cheque> cheques = queryRaw(sql, getRawRowMapper());
        if (cheques != null) {
            return cheques.getResults();
        }
        return null;
    }
}
