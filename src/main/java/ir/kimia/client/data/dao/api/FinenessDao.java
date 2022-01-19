package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.Fineness;

import java.sql.SQLException;
import java.util.List;

public interface FinenessDao extends BaseDao<Fineness, Integer> {

    public List<Fineness> getAll() throws SQLException;

}
