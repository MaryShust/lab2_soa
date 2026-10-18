package inf_system.lab2.labwork.query;

import java.util.Arrays;

public enum FilterOperator {
    EQ("eq"),
    NE("ne"),
    GT("gt"),
    GE("ge"),
    LT("lt"),
    LE("le"),
    CONTAINS("contains"),
    ISNULL("isnull"),
    NOTNULL("notnull");

    private final String externalName;

    FilterOperator(String externalName) {
        this.externalName = externalName;
    }

    public String getExternalName() {
        return externalName;
    }

    public static FilterOperator fromExternal(String value) {
        return Arrays.stream(values())
                .filter(o -> o.externalName.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(value));
    }
}
