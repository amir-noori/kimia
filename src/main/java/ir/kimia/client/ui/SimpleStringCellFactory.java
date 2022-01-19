package ir.kimia.client.ui;

import javafx.util.StringConverter;

public class SimpleStringCellFactory extends StringConverter<String> {
    @Override
    public String toString(String object) {
        if (object == null) return "";
        return object;
    }

    @Override
    public String fromString(String string) {
        return string;
    }
}
