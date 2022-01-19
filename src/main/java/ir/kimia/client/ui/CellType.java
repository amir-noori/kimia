package ir.kimia.client.ui;

public enum CellType {

    UNDEFINED(0),
    TEXT(1),
    NUMBER(2),
    THOUSAND_SEPARATED_NUMBER(3);


    private int value;

    CellType(int i) {
            this.value = i;
        }

    public int value() {
        return this.value;
    }

    public static CellType getByValue(int value) {
        for (CellType cellType : CellType.values()) {
            if (value == cellType.value) {
                return cellType;
            }
        }
        return null;
    }


}
