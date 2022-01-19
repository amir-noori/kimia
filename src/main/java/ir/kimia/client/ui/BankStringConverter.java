package ir.kimia.client.ui;

import ir.kimia.client.data.model.Bank;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class BankStringConverter extends StringConverter<Bank> {

    private List<Bank> allBanks;

    public BankStringConverter(List<Bank> allBanks) {
        this.allBanks = allBanks;
    }

    @Override
    public String toString(Bank bank) {
        if (bank == null || StringUtils.isEmpty(bank.getBankName())) {
            return "";
        } else {
            return bank.getBankName();
        }
    }

    @Override
    public Bank fromString(String string) {
        Bank result = new Bank();
        if (allBanks != null) {
            for (Bank object : allBanks) {
                if (object.getBankName().equals(string)) {
                    result.setBankName(object.getBankName());
                    result.setId(object.getId());
                    return result;
                }
            }
        }
        return result;
    }

}
