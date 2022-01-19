package ir.kimia.client.service.impl;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.common.ResultCodes;
import ir.kimia.client.data.dao.api.AddressDao;
import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.PartyDao;
import ir.kimia.client.data.model.Address;
import ir.kimia.client.data.model.Office;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.api.PartyService;
import ir.kimia.client.util.FxUtil;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class PartyServiceImpl extends BaseServiceImpl implements PartyService {

    private final PartyDao partyDao;
    private final AddressDao addressDao;

    @Inject
    public PartyServiceImpl(PartyDao partyDao, AddressDao addressDao) {
        this.partyDao = partyDao;
        this.addressDao = addressDao;
    }


    @Override
    public Party getPartyById(Integer id) throws SQLException {
        return partyDao.queryForId(id);
    }

    @Override
    public String getNextAvailableCode(Party.PartyType partyType) {
        try {
            String code = partyDao.getNextAvailableCode(partyType);

            int partyCode = Integer.parseInt(code);
            boolean isCodeValid = true;
            switch (partyType) {
                case BANK -> {
                    // BANK code can be between 5000 to 5999
                    if (partyCode < 5000 || partyCode >= 6000) {
                        isCodeValid = false;
                    }
                }
                case CUSTOMER -> {
                    // CUSTOMER code can be between 7000 to 7999
                    if (partyCode < 7000 || partyCode >= 8000) {
                        isCodeValid = false;
                    }
                }
                case INDIVIDUAL -> {
                    // INDIVIDUAL code can be between 3000 to 3999
                    if (partyCode < 3000 || partyCode >= 4000) {
                        isCodeValid = false;
                    }
                }
                case MANUFACTURER -> {
                    // MANUFACTURER code can be between 6000 to 6999
                    if (partyCode < 6000 || partyCode >= 7000) {
                        isCodeValid = false;
                    }
                }
            }

            if (!isCodeValid) {
                FxUtil.exceptionOccurred(new ApplicationException(ResultCodes.DATABASE_PARTY_CODE_MISMATCH_ERROR));
                return null;
            } else {
                return code;
            }
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Override
    public Party getPartyByCode(String code) throws SQLException {
        return partyDao.queryBuilder().where().eq("code", code).and().eq("OFFICE_ID", getOfficeId()).queryForFirst();
    }

    @Override
    protected BaseDao getDao() {
        return partyDao;
    }

    @Override
    public List<Party> getAllParties() throws SQLException {
        Integer officeId = ApplicationContext.getUserSession().getCurrentOffice().getId();
        return partyDao.queryBuilder().where().eq("OFFICE_ID", officeId).query();
    }

    @Override
    public Party createParty(Party party) throws SQLException {
        party.setOffice(ApplicationContext.getUserSession().getCurrentOffice());
        partyDao.create(party);
        return party;
    }

    @Override
    public Party updateParty(Party party) throws SQLException {
        Address partyAddress = party.getPartyAddress();
        addressDao.update(partyAddress);
        int id = partyDao.update(party);
        party.setId(id);
        return party;
    }

    @Override
    public int removeParty(Party party) throws SQLException {
        party.setOffice(ApplicationContext.getUserSession().getCurrentOffice());
        return partyDao.delete(party);
    }

    @Override
    public Party findPartyByName(String partyName) throws SQLException {
        Integer officeId = ApplicationContext.getUserSession().getCurrentOffice().getId();
        return partyDao.queryBuilder().where().eq("partyName", partyDao).and().eq("OFFICE_ID", officeId).queryForFirst();
    }

    @Override
    public List<Party> getPartiesByOffice(Office office) throws SQLException {
        return partyDao.queryForEq("OFFICE_ID", office);
    }

    @Override
    public List<Party> getPartiesByOfficeAndPartyType(Office office, Party.PartyType partyType) throws SQLException {
        return partyDao.queryBuilder().where().eq("OFFICE_ID", office).and().eq("partyType", partyType.ordinal()).query();
    }

}
