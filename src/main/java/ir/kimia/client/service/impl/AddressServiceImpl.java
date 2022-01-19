package ir.kimia.client.service.impl;

import ir.kimia.client.data.dao.api.AddressDao;
import ir.kimia.client.data.dao.api.BaseDao;
import ir.kimia.client.data.dao.api.OfficeDao;
import ir.kimia.client.data.model.Address;
import ir.kimia.client.data.model.Office;
import ir.kimia.client.service.api.AddressService;
import ir.kimia.client.service.api.OfficeService;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class AddressServiceImpl extends BaseServiceImpl implements AddressService {

    private final AddressDao addressDao;

    @Inject
    public AddressServiceImpl(AddressDao addressDao) {
        this.addressDao = addressDao;
    }


    @Override
    public Address createAddress(Address address) throws SQLException {
        addressDao.create(address);
        return address;
    }

    @Override
    protected BaseDao getDao() {
        return addressDao;
    }
}
