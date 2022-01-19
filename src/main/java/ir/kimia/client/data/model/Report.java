package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Dummy entity for now.
 */
@DatabaseTable(tableName = "TBL_REPORT")
public class Report {

    @DatabaseField(generatedId = true)
    private Integer id;

    public Report() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
