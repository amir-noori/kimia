package ir.kimia.client.data.dao.impl;

import com.j256.ormlite.support.ConnectionSource;
import ir.kimia.client.data.dao.api.AddressDao;
import ir.kimia.client.data.model.Address;

import java.sql.SQLException;

public class AddressDaoImpl extends AbstractBaseDaoImpl<Address, Integer> implements AddressDao {

    public AddressDaoImpl(ConnectionSource connectionSource, Class<Address> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

}
