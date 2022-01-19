package ir.kimia.client.service.impl;

import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.InvoiceDao;
import ir.kimia.client.data.dao.api.InvoiceRecordDao;
import ir.kimia.client.data.model.InvoiceRecord;
import ir.kimia.client.service.api.BaseService;
import ir.kimia.client.service.api.InvoiceRecordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.sql.SQLException;

public class InvoiceRecordServiceImpl extends BaseServiceImpl implements InvoiceRecordService {

    private static final Logger log = LogManager.getLogger(InvoiceServiceImpl.class);

    private final InvoiceDao invoiceDao;
    private final InvoiceRecordDao invoiceRecordDao;

    @Inject
    public InvoiceRecordServiceImpl(InvoiceDao invoiceDao, InvoiceRecordDao invoiceRecordDao) {
        this.invoiceDao = invoiceDao;
        this.invoiceRecordDao = invoiceRecordDao;
    }

    @Override
    public void createOrUpdateInvoiceRecord(InvoiceRecord invoiceRecord) throws SQLException {
        invoiceRecordDao.createOrUpdate(invoiceRecord);
    }

    @Override
    protected BaseDao getDao() {
        return invoiceRecordDao;
    }
}
