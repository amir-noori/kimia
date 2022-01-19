package ir.kimia.client.common;

public enum StoneCodes {

    PEARL(1),
    RUBY(2),
    BRILLIANT(3);


    private final Integer value;

    StoneCodes(Integer s) {
        this.value = s;
    }

    public Integer value() {
        return this.value;
    }
}
