package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TBL_BANK")
public class Bank {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField()
    private String code;

    @DatabaseField(canBeNull = false)
    private String bankName;

    public Bank() {
    }

    public Bank(String bankName, String code) {
        this.code = code;
        this.bankName = bankName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
