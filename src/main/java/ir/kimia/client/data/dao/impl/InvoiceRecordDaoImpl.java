package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.InvoiceRecordDao;
import ir.kimia.client.data.model.InvoiceRecord;
import ir.kimia.client.data.model.Stone;

import java.sql.SQLException;
import java.util.Collection;

public class InvoiceRecordDaoImpl extends AbstractBaseDaoImpl<InvoiceRecord, Integer> implements InvoiceRecordDao {


    public InvoiceRecordDaoImpl(ConnectionSource connectionSource, Class<InvoiceRecord> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

}
