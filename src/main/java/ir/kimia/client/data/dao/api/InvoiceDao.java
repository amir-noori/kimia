package ir.kimia.client.data.dao.api;

import ir.kimia.client.data.model.Invoice;
import ir.kimia.client.data.model.Party;

import java.sql.SQLException;

public interface InvoiceDao extends BaseDao<Invoice, Integer> {

    public Invoice findByDocumentNumber(Integer documentNumber) throws SQLException;

    public Invoice findByInvoiceNumber(Party party, Integer invoiceNumber) throws SQLException;

    public Integer getNextDocumentNumber() throws SQLException;

    public Integer getNextInvoiceNumber(Party party) throws SQLException;

}
