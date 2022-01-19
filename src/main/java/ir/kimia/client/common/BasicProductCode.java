package ir.kimia.client.common;

public enum BasicProductCode {

    MELTED_GOLD("1"),
    MSC_GOLD("2"),
    CASH("3");
    private final String value;

    BasicProductCode(String s) {
        this.value = s;
    }

    public String value() {
        return this.value;
    }
}
