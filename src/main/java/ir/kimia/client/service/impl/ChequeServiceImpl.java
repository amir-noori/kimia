package ir.kimia.client.service.impl;

import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.ChequeDao;
import ir.kimia.client.data.model.Cheque;
import ir.kimia.client.service.api.ChequeService;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class ChequeServiceImpl extends BaseServiceImpl implements ChequeService {

    private final ChequeDao chequeDao;

    @Inject
    public ChequeServiceImpl(ChequeDao chequeDao) {
        this.chequeDao = chequeDao;
    }

    @Override
    public List<Cheque> getAllCheques() throws SQLException {
        return chequeDao.findAll();
    }

    @Override
    public Cheque findByChequeNumber(String chequeNumber) throws SQLException {
        return chequeDao.findByChequeNumber(chequeNumber);
    }

    @Override
    public boolean doesChequeNumberExists(String chequeNumber) throws SQLException {
        Cheque byChequeNumber = chequeDao.findByChequeNumber(chequeNumber);
        return byChequeNumber != null;
    }

    @Override
    public Cheque createCheque(Cheque cheque) throws SQLException {
        cheque.setOffice(getOffice());
        chequeDao.create(cheque);
        return cheque;
    }

    @Override
    public int removeCheque(Cheque cheque) throws SQLException {
        return chequeDao.delete(cheque);
    }

    @Override
    public List<Cheque> getInOfficeInventoryChequeStocks() throws SQLException {
        return chequeDao.getInOfficeInventoryChequeStocks();
    }

    @Override
    public List<Cheque> getHoldByPartyChequeStocks() throws SQLException {
        return chequeDao.getHoldByPartyChequeStocks();
    }

    @Override
    protected BaseDao getDao() {
        return chequeDao;
    }
}
