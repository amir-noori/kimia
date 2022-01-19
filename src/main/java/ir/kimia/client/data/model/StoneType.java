package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TBL_STONE_TYPE")
public class StoneType {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private Integer code;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField()
    private Double stoneFee;

    public StoneType() {
    }

    public StoneType(Integer code, String name, Double stoneFee) {
        this.code = code;
        this.name = name;
        this.stoneFee = stoneFee;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getStoneFee() {
        return stoneFee;
    }

    public void setStoneFee(Double stoneFee) {
        this.stoneFee = stoneFee;
    }
}
