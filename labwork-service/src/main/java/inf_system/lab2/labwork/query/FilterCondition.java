package inf_system.lab2.labwork.query;

public class FilterCondition {

    private final FilterField field;
    private final FilterOperator operator;
    private final String rawValue;

    public FilterCondition(FilterField field, FilterOperator operator, String rawValue) {
        this.field = field;
        this.operator = operator;
        this.rawValue = rawValue;
    }

    public FilterField getField() {
        return field;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public String getRawValue() {
        return rawValue;
    }
}
