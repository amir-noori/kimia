package ir.kimia.client.service.api;

import ir.kimia.client.data.model.Office;

import java.sql.SQLException;
import java.util.List;

public interface OfficeService extends BaseService {

    public Office getOfficeById(Integer id) throws SQLException;

    public List<Office> getAllOffices() throws SQLException;

    public Office createOffice(Office office) throws SQLException;

    public void removeOffice(Office office) throws SQLException;

    public Office findOfficeByName(String officeName) throws SQLException;

}
