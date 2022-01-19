package ir.kimia.client.ui;

import ir.kimia.client.data.model.PurityEvaluator;
import javafx.util.StringConverter;

import java.util.List;

public class PurityEvaluatorStringConverter extends StringConverter<PurityEvaluator> {

    private List<PurityEvaluator> allPurityEvaluators;

    public PurityEvaluatorStringConverter(List<PurityEvaluator> allPurityEvaluators) {
        this.allPurityEvaluators = allPurityEvaluators;
    }

    @Override
    public String toString(PurityEvaluator object) {
        if (object == null || object.getName() == null) return "";
        return object.getName();
    }

    @Override
    public PurityEvaluator fromString(String string) {
        PurityEvaluator result = new PurityEvaluator();
        if (allPurityEvaluators != null) {
            for (PurityEvaluator object : allPurityEvaluators) {
                if (object.getName().equals(string)) {
                    result.setId(object.getId());
                    result.setName(object.getName());
                    return result;
                }
            }
        }
        result.setName(string);
        return result;
    }

}
