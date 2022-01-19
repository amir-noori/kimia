package ir.kimia.client.service.api;

import ir.kimia.client.data.dto.PartyBalanceReportDto;
import ir.kimia.client.data.model.Party;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface ReportService extends BaseService {

    public List<PartyBalanceReportDto> getPartyBalanceReport(Party party, Date fromDate, Date toDate) throws SQLException;

}
