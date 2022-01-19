package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.ReportDao;
import ir.kimia.client.data.dto.PartyBalanceReportDto;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.data.model.Report;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ReportDaoImpl extends AbstractBaseDaoImpl<Report, Integer> implements ReportDao {

    public ReportDaoImpl(ConnectionSource connectionSource, Class<Report> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public List<PartyBalanceReportDto> getPartyBalanceReport(Party party, Date fromDate, Date toDate) throws SQLException {
        final List<PartyBalanceReportDto> results = fetchRawResults("select * from TBL_INVOICE;", (String[] record, String[] columnNames) -> {
            PartyBalanceReportDto partyBalanceReportDto = new PartyBalanceReportDto();
            final String documentNumber = record[getColumnIndex("DOCUMENTNUMBER", columnNames)];
            if (documentNumber != null) {
                partyBalanceReportDto.setDocumentNumber(Integer.valueOf(documentNumber));
            }
            return partyBalanceReportDto;
        });
        return results;
    }
}
