package ir.kimia.client.service.api;

import ir.kimia.client.data.model.Office;
import ir.kimia.client.data.model.Party;

import java.sql.SQLException;
import java.util.List;

public interface PartyService extends BaseService {

    public Party getPartyById(Integer id) throws SQLException;

    public String getNextAvailableCode(Party.PartyType partyType);

    public Party getPartyByCode(String code) throws SQLException;

    public List<Party> getAllParties() throws SQLException;

    public Party createParty(Party party) throws SQLException;

    public Party updateParty(Party party) throws SQLException;

    public int removeParty(Party party) throws SQLException;

    public Party findPartyByName(String partyName) throws SQLException;

    public List<Party> getPartiesByOffice(Office office) throws SQLException;

    public List<Party> getPartiesByOfficeAndPartyType(Office office, Party.PartyType partyType) throws SQLException;

}
