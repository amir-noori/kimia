package ir.kimia.client.service.api;

import ir.kimia.client.data.model.Address;
import ir.kimia.client.data.model.Office;

import java.sql.SQLException;
import java.util.List;

public interface AddressService extends BaseService {

    public Address createAddress(Address address) throws SQLException;

}
