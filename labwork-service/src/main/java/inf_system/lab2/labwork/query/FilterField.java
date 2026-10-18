package inf_system.lab2.labwork.query;

import java.util.Arrays;
import java.util.Set;

public enum FilterField {
    ID("id", "id", null, FieldType.NUMBER, false),
    NAME("name", "name", null, FieldType.STRING, false),
    COORDINATES_X("coordinates.x", "coordinates", "x", FieldType.NUMBER, false),
    COORDINATES_Y("coordinates.y", "coordinates", "y", FieldType.NUMBER, false),
    CREATION_DATE("creationDate", "creationDate", null, FieldType.DATE, false),
    MINIMAL_POINT("minimalPoint", "minimalPoint", null, FieldType.NUMBER, false),
    PERSONAL_QUALITIES_MINIMUM("personalQualitiesMinimum", "personalQualitiesMinimum", null, FieldType.NUMBER, false),
    AVERAGE_POINT("averagePoint", "averagePoint", null, FieldType.NUMBER, false),
    DIFFICULTY("difficulty", "difficulty", null, FieldType.ENUM, false),
    AUTHOR("author", "author", null, FieldType.OBJECT, true),
    AUTHOR_NAME("author.name", "author", "name", FieldType.STRING, true),
    AUTHOR_BIRTHDAY("author.birthday", "author", "birthday", FieldType.DATE, true),
    AUTHOR_HEIGHT("author.height", "author", "height", FieldType.NUMBER, true),
    AUTHOR_WEIGHT("author.weight", "author", "weight", FieldType.NUMBER, true),
    AUTHOR_PASSPORT_ID("author.passportID", "author", "passportID", FieldType.STRING, true);

    public enum FieldType {
        NUMBER,
        STRING,
        DATE,
        ENUM,
        OBJECT
    }

    private final String externalName;
    private final String rootAttribute;
    private final String nestedAttribute;
    private final FieldType type;
    private final boolean nullable;

    FilterField(String externalName, String rootAttribute, String nestedAttribute, FieldType type, boolean nullable) {
        this.externalName = externalName;
        this.rootAttribute = rootAttribute;
        this.nestedAttribute = nestedAttribute;
        this.type = type;
        this.nullable = nullable;
    }

    public String getExternalName() {
        return externalName;
    }

    public String getRootAttribute() {
        return rootAttribute;
    }

    public String getNestedAttribute() {
        return nestedAttribute;
    }

    public FieldType getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isNested() {
        return nestedAttribute != null;
    }

    public static FilterField fromExternal(String value) {
        return Arrays.stream(values())
                .filter(f -> f.externalName.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(value));
    }

    public static Set<FilterOperator> allowedOperators(FilterField field) {
        if (field == AUTHOR) {
            return Set.of(FilterOperator.ISNULL, FilterOperator.NOTNULL);
        }
        return Set.of(FilterOperator.EQ, FilterOperator.NE, FilterOperator.GT, FilterOperator.GE,
                FilterOperator.LT, FilterOperator.LE, FilterOperator.CONTAINS);
    }
}
