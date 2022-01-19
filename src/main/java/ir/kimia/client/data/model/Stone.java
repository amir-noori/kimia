package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TBL_STONE")
public class Stone {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private Double weight;

    @DatabaseField()
    private Double fee;

    @DatabaseField()
    private Double price;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "office_id")
    private Office office;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "stone_type_id")
    private StoneType stoneType;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "stock_id")
    private Stock stock;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "invoice_record_id")
    private InvoiceRecord invoiceRecord;

    public Stone() {
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getWeight() {
        return weight;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public StoneType getStoneType() {
        return stoneType;
    }

    public void setStoneType(StoneType stoneType) {
        this.stoneType = stoneType;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public InvoiceRecord getInvoiceRecord() {
        return invoiceRecord;
    }

    public void setInvoiceRecord(InvoiceRecord invoiceRecord) {
        this.invoiceRecord = invoiceRecord;
    }
}
