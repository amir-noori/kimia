package ir.kimia.client.service.impl;

import ir.kimia.client.data.dao.api.BankDao;
import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.model.Bank;
import ir.kimia.client.service.api.BankService;
import ir.kimia.client.util.FxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class BankServiceImpl extends BaseServiceImpl implements BankService {

    private static final Logger log = LogManager.getLogger(BankServiceImpl.class);

    private final BankDao bankDao;

    @Inject
    public BankServiceImpl(BankDao bankDao) {
        this.bankDao = bankDao;
    }


    @Override
    public List<Bank> getAllBanks() throws SQLException {
        return bankDao.getAllBanks();
    }

    @Override
    public Bank createBank(Bank bank) throws SQLException {
        bankDao.create(bank);
        return bank;
    }

    @Override
    public void setupDefaultBanks() throws SQLException {
        List<Bank> banks = bankDao.queryForAll();
        if (banks == null || banks.size() == 0) {
            log.debug("setting all default banks");
            Bank melliBank = new Bank(FxUtil.message("melli"), "1");
            Bank mellatBank = new Bank(FxUtil.message("mellat"), "2");
            Bank parsianBank = new Bank(FxUtil.message("parsian"), "3");
            Bank pasargadBank = new Bank(FxUtil.message("pasargad"), "4");
            Bank saderatBank = new Bank(FxUtil.message("saderat"), "5");
            Bank tejaratBank = new Bank(FxUtil.message("tejarat"), "6");
            Bank keshavarziBank = new Bank(FxUtil.message("keshavarzi"), "7");

            bankDao.create(melliBank);
            bankDao.create(mellatBank);
            bankDao.create(parsianBank);
            bankDao.create(pasargadBank);
            bankDao.create(saderatBank);
            bankDao.create(tejaratBank);
            bankDao.create(keshavarziBank);
        }
    }

    @Override
    protected BaseDao getDao() {
        return bankDao;
    }
}
