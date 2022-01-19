package ir.kimia.client.service.api;

import ir.kimia.client.data.model.Cheque;

import java.sql.SQLException;
import java.util.List;

public interface ChequeService extends BaseService {

    public List<Cheque> getAllCheques() throws SQLException;

    public Cheque findByChequeNumber(String chequeNumber) throws SQLException;

    public boolean doesChequeNumberExists(String chequeNumber) throws SQLException;

    public Cheque createCheque(Cheque cheque) throws SQLException;

    public int removeCheque(Cheque cheque) throws SQLException;

    public List<Cheque> getInOfficeInventoryChequeStocks() throws SQLException;

    public List<Cheque> getHoldByPartyChequeStocks() throws SQLException;

}
