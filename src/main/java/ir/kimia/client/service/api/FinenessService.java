package ir.kimia.client.service.api;

import ir.kimia.client.data.model.Fineness;

import java.sql.SQLException;
import java.util.List;

public interface FinenessService extends BaseService {

    public List<Fineness> getAll() throws SQLException;
    public void createOrUpdate(Fineness fineness) throws SQLException;
    public void remove(Fineness fineness) throws SQLException;

}
