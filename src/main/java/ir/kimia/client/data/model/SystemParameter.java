package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TBL_SYSTEM_PARAMETER")
public class SystemParameter {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField()
    private String name;

    @DatabaseField()
    private String value;

    /**
     * if the office is null then it means the parameter is general and not for an specific offices.
     */
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "office_id")
    private Office office;

    public SystemParameter() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }
}
