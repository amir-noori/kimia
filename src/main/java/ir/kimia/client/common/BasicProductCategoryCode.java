package ir.kimia.client.common;

public enum BasicProductCategoryCode {

    CASH("1"),
    MELTED("2"),
    MSC("3"),
    MANUFACTURED("4"),
    CURRENCY("5"),
    COIN("6"),
    STONE("7");

    private final String value;

    BasicProductCategoryCode(String s) {
        this.value = s;
    }

    public String value() {
        return this.value;
    }
}
