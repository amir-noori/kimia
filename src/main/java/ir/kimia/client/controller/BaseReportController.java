package ir.kimia.client.controller;


import ir.kimia.client.service.api.PartyService;
import ir.kimia.client.service.api.ReportService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

public abstract class BaseReportController extends BaseController {

    private static final Logger log = LogManager.getLogger(BaseReportController.class);


    protected ReportService reportService;
    protected PartyService partyService;


    @Inject
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Inject
    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }


}
