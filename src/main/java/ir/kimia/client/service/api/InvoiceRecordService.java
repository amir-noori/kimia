package ir.kimia.client.service.api;

import ir.kimia.client.data.model.InvoiceRecord;

import java.sql.SQLException;

public interface InvoiceRecordService extends BaseService {

    public void createOrUpdateInvoiceRecord(InvoiceRecord invoiceRecord) throws SQLException;


}
