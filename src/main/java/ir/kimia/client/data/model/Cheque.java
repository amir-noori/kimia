package ir.kimia.client.data.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "TBL_CHEQUE")
public class Cheque {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String description;

    @DatabaseField()
    private String chequeNumber;

    @DatabaseField(dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
    private Date dueDate;

    @DatabaseField()
    private Boolean inOfficeInventory;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "party_id")
    private Party party;

    @DatabaseField()
    private Double amount;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "office_id")
    private Office office;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "bank_id", foreignAutoCreate = true)
    private Bank bank;

    public Cheque() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getInOfficeInventory() {
        return inOfficeInventory;
    }

    public void setInOfficeInventory(Boolean inOfficeInventory) {
        this.inOfficeInventory = inOfficeInventory;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }
}
