package ir.kimia.client.service.api;

import ir.kimia.client.data.model.Invoice;
import ir.kimia.client.data.model.Party;

import java.sql.SQLException;

public interface InvoiceService extends BaseService {

    public Invoice getByDocumentNumber(Integer documentNumber) throws SQLException;

    public Invoice getByInvoiceNumber(Party party, Integer invoiceNumber) throws SQLException;

    public void removeInvoice(Invoice invoice) throws SQLException;

    public void createOrUpdateInvoice(Invoice invoice) throws SQLException;

    public Integer getNextDocumentNumber() throws SQLException;

    public Integer getNextInvoiceNumber(Party party) throws SQLException;

    public boolean documentNumberExists(Integer documentNumber) throws SQLException;

    public boolean invoiceNumberExists(Party party, Integer invoiceNumber) throws SQLException;

}
