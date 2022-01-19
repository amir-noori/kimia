package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.PurityEvaluator;

import java.sql.SQLException;
import java.util.List;

public interface PurityEvaluatorDao extends BaseDao<PurityEvaluator, Integer> {

    public List<PurityEvaluator> getAll() throws SQLException;

}
