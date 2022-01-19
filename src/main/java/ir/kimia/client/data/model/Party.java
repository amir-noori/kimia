package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TBL_PARTY")
public class Party {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String code;

    @DatabaseField(canBeNull = false)
    private Integer partyType;

    @DatabaseField(canBeNull = false)
    private String partyName;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "office_id")
    private Office office;

    @DatabaseField
    private String firstMobileNumber;

    @DatabaseField
    private String secondMobileNumber;

    @DatabaseField
    private String firstPhoneNumber;

    @DatabaseField
    private String secondPhoneNumber;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "address_id", foreignAutoCreate = true)
    private Address partyAddress;

    public Party() {
    }

    public String getFirstMobileNumber() {
        return firstMobileNumber;
    }

    public void setFirstMobileNumber(String firstMobileNumber) {
        this.firstMobileNumber = firstMobileNumber;
    }

    public String getSecondMobileNumber() {
        return secondMobileNumber;
    }

    public void setSecondMobileNumber(String secondMobileNumber) {
        this.secondMobileNumber = secondMobileNumber;
    }

    public String getFirstPhoneNumber() {
        return firstPhoneNumber;
    }

    public void setFirstPhoneNumber(String firstPhoneNumber) {
        this.firstPhoneNumber = firstPhoneNumber;
    }

    public String getSecondPhoneNumber() {
        return secondPhoneNumber;
    }

    public void setSecondPhoneNumber(String secondPhoneNumber) {
        this.secondPhoneNumber = secondPhoneNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getPartyType() {
        return partyType;
    }

    public void setPartyType(Integer partyType) {
        this.partyType = partyType;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public Address getPartyAddress() {
        return partyAddress;
    }

    public void setPartyAddress(Address partyAddress) {
        this.partyAddress = partyAddress;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + id;
        hash = 31 * hash + id.hashCode();
        hash = 31 * hash + (code == null ? 0 : code.hashCode());
        hash = 31 * hash + (partyName == null ? 0 : partyName.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (((Party) obj).getId() != null) {
            return ((Party) obj).getId().equals(id);
        } else if (((Party) obj).getCode() != null && ((Party) obj).getOffice() != null) {
            return ((Party) obj).getCode().equals(code) && ((Party) obj).getOffice().equals(office);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return "Party{" +
                "id=" + id +
                ", code=" + code +
                ", partyType=" + partyType +
                ", partyName='" + partyName + '\'' +
                '}';
    }

    public enum PartyType {
        CUSTOMER(0),
        MANUFACTURER(1),
        BANK(2),
        INDIVIDUAL(3),
        EXPENSE(4),
        ALL(-1);


        private int value;

        PartyType(int i) {
            this.value = i;
        }

        public int value() {
            return this.value;
        }

    }

}
