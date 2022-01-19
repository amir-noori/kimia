package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TBL_ADDRESS")
public class Address {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = true)
    private String address;

    @DatabaseField(canBeNull = true)
    private Integer provinceCode;

    @DatabaseField(canBeNull = true)
    private Integer cityCode;


    public Address() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(Integer provinceCode) {
        this.provinceCode = provinceCode;
    }

    public Integer getCityCode() {
        return cityCode;
    }

    public void setCityCode(Integer cityCode) {
        this.cityCode = cityCode;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", provinceCode=" + provinceCode +
                ", cityCode=" + cityCode +
                '}';
    }
}
