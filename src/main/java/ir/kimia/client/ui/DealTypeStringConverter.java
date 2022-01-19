package ir.kimia.client.ui;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.data.model.InvoiceRecord.DealType;
import javafx.util.StringConverter;

import java.util.List;

public class DealTypeStringConverter extends StringConverter<DealType> {

    private List<DealType> allDealTypes;

    public DealTypeStringConverter(List<DealType> allDealTypes) {
        this.allDealTypes = allDealTypes;
    }

    @Override
    public String toString(DealType object) {
        return object != null ? ApplicationContext.getResourceBundle().getString("deal.type." + object.toString().toLowerCase()) : "";
    }

    @Override
    public DealType fromString(String string) {
        DealType result = null;
        if (allDealTypes != null) {
            for (DealType object : allDealTypes) {
                if (ApplicationContext.getResourceBundle().getString("deal.type." + object.toString().toLowerCase()).equals(string)) {
                    return object;
                }
            }
        }
        return result;
    }

}
