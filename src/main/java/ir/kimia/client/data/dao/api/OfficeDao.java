package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.Office;

import java.sql.SQLException;
import java.util.List;

public interface OfficeDao extends BaseDao<Office, Integer> {

    public List<Office> findByOfficeName(String officeName) throws SQLException;

    public void removeAllOfficeData(Integer officeId) throws SQLException;

}
