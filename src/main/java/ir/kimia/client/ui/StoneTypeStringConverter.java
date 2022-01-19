package ir.kimia.client.ui;

import ir.kimia.client.data.model.StoneType;
import javafx.util.StringConverter;

import java.util.List;

public class StoneTypeStringConverter extends StringConverter<StoneType> {

    private List<StoneType> allStoneTypes;

    public StoneTypeStringConverter(List<StoneType> allStoneTypes) {
        this.allStoneTypes = allStoneTypes;
    }

    @Override
    public String toString(StoneType object) {
        if (object == null || (object.getName() == null && object.getCode() == null)) return "";
        if (object.getName() == null && object.getCode() != null) {
            return String.valueOf(object.getCode());
        }
        return object.getName();
    }

    @Override
    public StoneType fromString(String string) {
        StoneType result = new StoneType();
        if (allStoneTypes != null) {
            for (StoneType object : allStoneTypes) {
                if (object.getName().equals(string)) {
                    result.setCode(object.getCode());
                    result.setId(object.getId());
                    result.setName(object.getName());
                    result.setStoneFee(object.getStoneFee());
                    return result;
                }
            }
        }
        result.setName(string);
        return result;
    }

}
