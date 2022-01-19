package ir.kimia.client.service.api;

import ir.kimia.client.data.model.PurityEvaluator;

import java.sql.SQLException;
import java.util.List;

public interface PurityEvaluatorService extends BaseService {

    public List<PurityEvaluator> getAll() throws SQLException;
    public void createOrUpdate(PurityEvaluator purityEvaluator) throws SQLException;
    public void remove(PurityEvaluator purityEvaluator) throws SQLException;

}
