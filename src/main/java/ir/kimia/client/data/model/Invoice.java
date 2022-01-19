package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.Date;

@DatabaseTable(tableName = "TBL_INVOICE")
public class Invoice {

    @DatabaseField(generatedId = true)
    private Integer id;

    // this is a counter for each invoice.
    @DatabaseField(canBeNull = false)
    private Integer documentNumber;

    // this is a counter for each invoice for a particular party.
    @DatabaseField(canBeNull = false)
    private Integer invoiceNumber;

    @DatabaseField(canBeNull = false)
    private Date createTime;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "party_id")
    private Party party;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "office_id")
    private Office office;

    @ForeignCollectionField(eager = true)
    private Collection<InvoiceRecord> invoiceRecords;

    @DatabaseField
    private Boolean isFinalized;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getFinalized() {
        return isFinalized;
    }

    public void setFinalized(Boolean finalized) {
        isFinalized = finalized;
    }

    public Integer getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(Integer documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Collection<InvoiceRecord> getInvoiceRecords() {
        return invoiceRecords;
    }

    public void setInvoiceRecords(Collection<InvoiceRecord> invoiceRecords) {
        this.invoiceRecords = invoiceRecords;
    }

    public Invoice() {
    }


}
