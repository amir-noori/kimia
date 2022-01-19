package ir.kimia.client.service.api;

import ir.kimia.client.data.model.Bank;

import java.sql.SQLException;
import java.util.List;

public interface BankService extends BaseService {

    public List<Bank> getAllBanks() throws SQLException;

    public Bank createBank(Bank bank) throws SQLException;

    public void setupDefaultBanks() throws SQLException;

}
