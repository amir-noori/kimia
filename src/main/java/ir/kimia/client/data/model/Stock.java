package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

@DatabaseTable(tableName = "TBL_STOCK")
public class Stock {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField
    private String code;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "party_id")
    private Party party;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_id")
    private Product product;

//    @DatabaseField
//    private Double amount; // positive value means credit and negative means debit

    @DatabaseField
    private Double weight; // positive value means credit and negative means debit

    @DatabaseField
    private Double count; // positive value means credit and negative means debit

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "office_id")
    private Office office;

    @DatabaseField()
    private Double wagePercentage;

    @DatabaseField()
    private Double wageFee;

    @DatabaseField()
    private Double carat;

    @ForeignCollectionField(eager = true)
    private Collection<Stone> stones;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "fineness_id")
    private Fineness fineness;


    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Stock() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Double getWagePercentage() {
        return wagePercentage;
    }

    public void setWagePercentage(Double wagePercentage) {
        this.wagePercentage = wagePercentage;
    }

    public Double getWageFee() {
        return wageFee;
    }

    public void setWageFee(Double wageFee) {
        this.wageFee = wageFee;
    }

    public Double getCarat() {
        return carat;
    }

    public void setCarat(Double carat) {
        this.carat = carat;
    }

    public Collection<Stone> getStones() {
        return stones;
    }

    public void setStones(Collection<Stone> stones) {
        this.stones = stones;
    }

    public Fineness getFineness() {
        return fineness;
    }

    public void setFineness(Fineness fineness) {
        this.fineness = fineness;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                "product=" + product +
                ", code='" + code + '\'' +
                '}';
    }
}
