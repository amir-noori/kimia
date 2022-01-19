package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TBL_PRODUCT")
public class Product {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String code;

    @DatabaseField(canBeNull = false)
    private String productName;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "office_id")
    private Office office;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "productCategory_id")
    private ProductCategory productCategory;

    @DatabaseField()
    private Double receivedWageAmount;

    @DatabaseField()
    private Double receivedWagePercentage;

    @DatabaseField()
    private Double payedWageAmount;

    @DatabaseField()
    private Double payedWagePercentage;

    @DatabaseField
    private Double carat;


    public Product() {
    }

    public Product(String code, String productName, Office office, ProductCategory productCategory) {
        this.code = code;
        this.productName = productName;
        this.office = office;
        this.productCategory = productCategory;
    }

    public Double getCarat() {
        return carat;
    }

    public void setCarat(Double carat) {
        this.carat = carat;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + id;
        hash = 31 * hash + id.hashCode();
        hash = 31 * hash + (code == null ? 0 : code.hashCode());
        hash = 31 * hash + (productName == null ? 0 : productName.hashCode());
        hash = 31 * hash + (office == null ? 0 : office.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (((Product) obj).getId() != null) {
            return ((Product) obj).getId().equals(id);
        } else if (((Product) obj).getCode() != null && ((Product) obj).getOffice() != null) {
            return ((Product) obj).getCode().equals(code) && ((Product) obj).getOffice().equals(office);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return "Product{" +
                "code=" + code +
                ", productName='" + productName + '\'' +
                ", productCategory=" + productCategory +
                '}';
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

    public Double getReceivedWageAmount() {
        return receivedWageAmount;
    }

    public void setReceivedWageAmount(Double receivedWageAmount) {
        this.receivedWageAmount = receivedWageAmount;
    }

    public Double getReceivedWagePercentage() {
        return receivedWagePercentage;
    }

    public void setReceivedWagePercentage(Double receivedWagePercentage) {
        this.receivedWagePercentage = receivedWagePercentage;
    }

    public Double getPayedWageAmount() {
        return payedWageAmount;
    }

    public void setPayedWageAmount(Double payedWageAmount) {
        this.payedWageAmount = payedWageAmount;
    }

    public Double getPayedWagePercentage() {
        return payedWagePercentage;
    }

    public void setPayedWagePercentage(Double payedWagePercentage) {
        this.payedWagePercentage = payedWagePercentage;
    }
}
