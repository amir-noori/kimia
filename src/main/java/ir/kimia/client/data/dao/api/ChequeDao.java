package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.Cheque;

import java.sql.SQLException;
import java.util.List;

public interface ChequeDao extends BaseDao<Cheque, Integer> {

    public List<Cheque> findAll() throws SQLException;
    public Cheque findByChequeNumber(String chequeNumber) throws SQLException;
    public List<Cheque> getInOfficeInventoryChequeStocks() throws SQLException;
    public List<Cheque> getHoldByPartyChequeStocks() throws SQLException;

}
