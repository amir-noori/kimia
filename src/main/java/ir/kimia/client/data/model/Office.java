package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "TBL_OFFICE")
public class Office {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String officeName;

    @DatabaseField(canBeNull = false)
    private Date createTime;

    @DatabaseField()
    private String address;

    @DatabaseField()
    private String phoneNumber;


    public Office() {
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + id;
        hash = 31 * hash + id.hashCode();
        hash = 31 * hash + (createTime == null ? 0 : createTime.hashCode());
        hash = 31 * hash + (officeName == null ? 0 : officeName.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (((Office) obj).getId() != null) {
            return ((Office) obj).getId().equals(id);
        } else if (((Office) obj).getOfficeName() != null) {
            return ((Office) obj).getOfficeName().equals(officeName);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return officeName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
