package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TBL_PRODUCT_CATEGORY")
public class ProductCategory {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String code;

    @DatabaseField(canBeNull = false)
    private String title;

    @DatabaseField()
    private Boolean modifiable;

    @DatabaseField(canBeNull = false)
    private Boolean countable;

    @DatabaseField()
    private String description;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "office_id")
    private Office office;

    public ProductCategory(String code, String title, Boolean modifiable, Boolean countable, String description, Office office) {
        this.code = code;
        this.title = title;
        this.modifiable = modifiable;
        this.countable = countable;
        this.description = description;
        this.office = office;
    }

    public ProductCategory() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getModifiable() {
        return modifiable;
    }

    public void setModifiable(Boolean modifiable) {
        this.modifiable = modifiable;
    }

    public Boolean getCountable() {
        return countable;
    }

    public void setCountable(Boolean countable) {
        this.countable = countable;
    }

    @Override
    public String toString() {
        return "ProductCategory{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
