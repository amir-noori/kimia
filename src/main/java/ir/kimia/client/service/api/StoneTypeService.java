package ir.kimia.client.service.api;

import ir.kimia.client.data.model.StoneType;

import java.sql.SQLException;
import java.util.List;

public interface StoneTypeService extends BaseService {

    public List<StoneType> getAllStoneTypes() throws SQLException;
    public StoneType getByCode(Integer code) throws SQLException;
    public void initializeBasicStoneTypes() throws SQLException;

}
