package ir.kimia.client.service.impl;

import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.ReportDao;
import ir.kimia.client.data.dto.PartyBalanceReportDto;
import ir.kimia.client.data.model.Party;
import ir.kimia.client.service.api.ReportService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ReportServiceImpl extends BaseServiceImpl implements ReportService {

    private static final Logger log = LogManager.getLogger(ReportServiceImpl.class);

    private final ReportDao reportDao;

    @Inject
    public ReportServiceImpl(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public List<PartyBalanceReportDto> getPartyBalanceReport(Party party, Date fromDate, Date toDate) throws SQLException {
        return reportDao.getPartyBalanceReport(party, fromDate, toDate);
    }

    @Override
    protected BaseDao getDao() {
        return reportDao;
    }

}
