package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TBL_ACCOUNT_BALANCE")
public class AccountBalance {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "party_id")
    private Party party;

    @DatabaseField()
    private Double amount; // positive means credit for us and negative means debit for us

    @DatabaseField()
    private Double count; // positive means credit for us and negative means debit for us

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "office_id")
    private Office office;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "product_id")
    private Product product;


    public AccountBalance() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "AccountBalance{" +
                "id=" + id +
                ", party=" + party +
                ", amount=" + amount +
                ", count=" + count +
                ", office=" + office +
                ", product=" + product +
                '}';
    }
}
