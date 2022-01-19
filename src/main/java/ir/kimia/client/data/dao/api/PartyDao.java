package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.Party;

import java.sql.SQLException;
import java.util.List;

public interface PartyDao extends BaseDao<Party, Integer> {

    public List<Party> findByCode(String code) throws SQLException;

    public String getNextAvailableCode(Party.PartyType partyType) throws SQLException;

    public List<Party> findByName(String partyName) throws SQLException;

    public List<Party> findByType(Integer partyType) throws SQLException;

}
