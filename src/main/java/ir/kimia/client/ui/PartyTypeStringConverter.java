package ir.kimia.client.ui;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.data.model.Party;
import javafx.util.StringConverter;

public class PartyTypeStringConverter extends StringConverter<Party.PartyType> {

    @Override
    public String toString(Party.PartyType object) {
        int result = 0;
        if (object != null) {
            switch (object) {
                case ALL -> result = Party.PartyType.ALL.value();
                case BANK -> result = Party.PartyType.BANK.value();
                case CUSTOMER -> result = Party.PartyType.CUSTOMER.value();
                case INDIVIDUAL -> result = Party.PartyType.INDIVIDUAL.value();
                case MANUFACTURER -> result = Party.PartyType.MANUFACTURER.value();
                case EXPENSE -> result = Party.PartyType.EXPENSE.value();
            }
            return ApplicationContext.getResourceBundle().getString("party.type." + result);
        }
        return "";
    }

    @Override
    public Party.PartyType fromString(String string) {
        Party.PartyType result = null;
        switch (string) {
            case "-1" -> result = Party.PartyType.ALL;
            case "0" -> result = Party.PartyType.CUSTOMER;
            case "1" -> result = Party.PartyType.MANUFACTURER;
            case "2" -> result = Party.PartyType.BANK;
            case "3" -> result = Party.PartyType.INDIVIDUAL;
            case "4" -> result = Party.PartyType.EXPENSE;
        }
        return result;
    }
}
