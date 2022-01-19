package ir.kimia.client.service.api;

import ir.kimia.client.data.model.Bank;
import ir.kimia.client.data.model.Stone;

import java.sql.SQLException;

public interface StoneService extends BaseService {

    public void create(Stone stone) throws SQLException;

    public int remove(Stone stone) throws SQLException;

}
