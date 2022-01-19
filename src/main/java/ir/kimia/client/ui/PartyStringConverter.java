package ir.kimia.client.ui;

import ir.kimia.client.data.model.Party;
import javafx.util.StringConverter;

import java.util.List;

public class PartyStringConverter extends StringConverter<Party> {

    private List<Party> allParties;

    public PartyStringConverter(List<Party> allParties) {
        this.allParties = allParties;
    }

    @Override
    public String toString(Party object) {
        if (object == null || (object.getPartyName() == null && object.getCode() == null)) return "";
        if (object.getPartyName() == null && object.getCode() != null) {
            return String.valueOf(object.getCode());
        }
        return object.getPartyName();
    }

    @Override
    public Party fromString(String string) {
        Party result = new Party();
        if (allParties != null) {
            for (Party object : allParties) {
                if (object.getPartyName().equals(string)) {
                    result.setPartyName(object.getPartyName());
                    result.setId(object.getId());
                    result.setOffice(object.getOffice());
                    result.setPartyType(object.getPartyType());
                    result.setCode(object.getCode());
                    return result;
                }
            }
        }
        result.setPartyName(string);
        return result;
    }

}
