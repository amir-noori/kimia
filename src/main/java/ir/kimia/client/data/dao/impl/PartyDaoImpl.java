package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.data.dao.api.PartyDao;
import ir.kimia.client.data.model.Party;

import java.sql.SQLException;
import java.util.List;

public class PartyDaoImpl extends AbstractBaseDaoImpl<Party, Integer> implements PartyDao {

    public PartyDaoImpl(ConnectionSource connectionSource, Class<Party> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public List<Party> findByCode(String code) throws SQLException {
        return queryBuilder().where().eq("code", code).and().eq("OFFICE_ID", getOfficeId()).query();
    }

    @Override
    public String getNextAvailableCode(Party.PartyType partyType) throws SQLException {
        DatabaseTable databaseTableAnnotation = dataClass.getAnnotation(DatabaseTable.class);
        String tableName = databaseTableAnnotation.tableName();
        Integer officeId = getOfficeId();
        int partyTypeNumber = partyType.ordinal();
        String sql =
                "select IFNULL(" +
                "    select code + 1 from " + tableName + " where PARTYTYPE = '" + partyTypeNumber + "' AND OFFICE_ID = '" + officeId + "'" +
                "    and CODE not in ('1000', '2000') and code  + 1 not in (select code from " + tableName + " where PARTYTYPE = '" + partyTypeNumber + "' AND OFFICE_ID = '" + officeId + "' " +
                "    union select max(code) + 1 from " + tableName + " where PARTYTYPE = '" + partyTypeNumber + "' AND OFFICE_ID = '" + officeId + "') and rownum = 1," +
                "    select max(code) + 1 from " + tableName + " where PARTYTYPE = '" + partyTypeNumber + "' AND OFFICE_ID = '" + officeId + "'" +
                ") as MAX_CODE;";
        GenericRawResults<String[]> results = queryRaw(sql);
        String[] firstResult = results.getFirstResult();
        if(firstResult[0] == null) {
            String result = "0";
            switch (partyType) {
                case BANK -> result = "5000"; // bank code can be between 5000 to 5999
                case CUSTOMER -> result = "7000"; // customer code can be between 7000 to 7999
                case INDIVIDUAL -> result = "3000"; // individual code can be between 3000 to 3999
                case MANUFACTURER -> result = "6000"; // manufacturer code can be between 6000 to 6999
            }
            return result;
        } else {
            return firstResult[0];
        }
    }

    @Override
    public List<Party> findByName(String partyName) throws SQLException {
        return queryBuilder().where().eq("partyName", partyName).and().eq("OFFICE_ID", getOfficeId()).query();
    }

    @Override
    public List<Party> findByType(Integer partyType) throws SQLException {
        return queryBuilder().where().eq("partyType", partyType).and().eq("OFFICE_ID", getOfficeId()).query();
    }
}
