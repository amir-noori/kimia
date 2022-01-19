package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.Bank;

import java.sql.SQLException;
import java.util.List;

public interface BankDao extends BaseDao<Bank, Integer> {

    public List<Bank> getAllBanks() throws SQLException;

}
