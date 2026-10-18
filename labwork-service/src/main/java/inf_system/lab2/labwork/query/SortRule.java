package inf_system.lab2.labwork.query;

public class SortRule {

    private final SortField field;
    private final boolean ascending;

    public SortRule(SortField field, boolean ascending) {
        this.field = field;
        this.ascending = ascending;
    }

    public SortField getField() {
        return field;
    }

    public boolean isAscending() {
        return ascending;
    }
}
