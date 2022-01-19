package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.dto.PartyBalanceReportDto;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.data.model.Report;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface ReportDao extends BaseDao<Report, Integer> {

    public List<PartyBalanceReportDto> getPartyBalanceReport(Party party, Date fromDate, Date toDate) throws SQLException;

}
