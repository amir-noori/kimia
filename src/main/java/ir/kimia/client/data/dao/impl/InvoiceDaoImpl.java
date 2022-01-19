package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.InvoiceDao;
import ir.kimia.client.data.model.Invoice;
import ir.kimia.client.data.model.Party;

import java.sql.SQLException;

public class InvoiceDaoImpl extends AbstractBaseDaoImpl<Invoice, Integer> implements InvoiceDao {

    public InvoiceDaoImpl(ConnectionSource connectionSource, Class<Invoice> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }


    @Override
    public Invoice findByDocumentNumber(Integer documentNumber) throws SQLException {
        return queryBuilder().where().eq("documentNumber", documentNumber).and().eq("OFFICE_ID", getOfficeId()).queryForFirst();
    }

    @Override
    public Invoice findByInvoiceNumber(Party party, Integer invoiceNumber) throws SQLException {
        return queryBuilder().where().eq("invoiceNumber", invoiceNumber).and().eq("PARTY_ID" , party.getId()).
                and().eq("OFFICE_ID", getOfficeId()).queryForFirst();
    }

    @Override
    public Integer getNextDocumentNumber() throws SQLException {
        GenericRawResults<String[]> results = queryRaw("select max(cast(documentNumber as int)) + 1 as max_id from TBL_INVOICE where office_id='" + getOfficeId() + "';");
        String[] firstResult = results.getFirstResult();
        if(firstResult[0] == null) {
            return 0;
        } else {
            return Integer.valueOf(firstResult[0]);
        }
    }

    @Override
    public Integer getNextInvoiceNumber(Party party) throws SQLException {
        GenericRawResults<String[]> results = queryRaw("select max(cast(invoiceNumber as int)) + 1 as max_id from TBL_INVOICE where PARTY_ID = " + party.getId() + " and office_id='" + getOfficeId() + "';");
        String[] firstResult = results.getFirstResult();
        if(firstResult[0] == null) {
            return 0;
        } else {
            return Integer.valueOf(firstResult[0]);
        }
    }

}
