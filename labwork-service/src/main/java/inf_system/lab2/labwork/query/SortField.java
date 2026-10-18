package inf_system.lab2.labwork.query;

import java.util.Arrays;

public enum SortField {
    ID("id", "id", null),
    NAME("name", "name", null),
    COORDINATES_X("coordinates.x", "coordinates", "x"),
    COORDINATES_Y("coordinates.y", "coordinates", "y"),
    CREATION_DATE("creationDate", "creationDate", null),
    MINIMAL_POINT("minimalPoint", "minimalPoint", null),
    PERSONAL_QUALITIES_MINIMUM("personalQualitiesMinimum", "personalQualitiesMinimum", null),
    AVERAGE_POINT("averagePoint", "averagePoint", null),
    DIFFICULTY("difficulty", "difficulty", null),
    AUTHOR_NAME("author.name", "author", "name"),
    AUTHOR_BIRTHDAY("author.birthday", "author", "birthday"),
    AUTHOR_HEIGHT("author.height", "author", "height"),
    AUTHOR_WEIGHT("author.weight", "author", "weight"),
    AUTHOR_PASSPORT_ID("author.passportID", "author", "passportID");

    private final String externalName;
    private final String rootAttribute;
    private final String nestedAttribute;

    SortField(String externalName, String rootAttribute, String nestedAttribute) {
        this.externalName = externalName;
        this.rootAttribute = rootAttribute;
        this.nestedAttribute = nestedAttribute;
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

    public boolean isNested() {
        return nestedAttribute != null;
    }

    public static SortField fromExternal(String value) {
        return Arrays.stream(values())
                .filter(f -> f.externalName.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(value));
    }
}
