package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TBL_FINENESS")
public class Fineness {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField()
    private Double finenessNumber;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "office_id")
    private Office office;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "purity_evaluator_id")
    private PurityEvaluator purityEvaluator;

    @DatabaseField()
    private Boolean conditional;

    public Fineness() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getFinenessNumber() {
        return finenessNumber;
    }

    public void setFinenessNumber(Double finenessNumber) {
        this.finenessNumber = finenessNumber;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Boolean getConditional() {
        return conditional;
    }

    public void setConditional(Boolean conditional) {
        this.conditional = conditional;
    }

    public PurityEvaluator getPurityEvaluator() {
        return purityEvaluator;
    }

    public void setPurityEvaluator(PurityEvaluator purityEvaluator) {
        this.purityEvaluator = purityEvaluator;
    }
}
