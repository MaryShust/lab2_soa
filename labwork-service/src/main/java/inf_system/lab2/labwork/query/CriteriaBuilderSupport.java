package inf_system.lab2.labwork.query;

import inf_system.lab2.labwork.exception.BadRequestException;
import inf_system.lab2.labwork.model.Difficulty;
import inf_system.lab2.labwork.model.LabWork;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public final class CriteriaBuilderSupport {

    private CriteriaBuilderSupport() {
    }

    public static List<Order> buildOrders(CriteriaBuilder cb, Root<LabWork> root, List<SortRule> rules) {
        List<Order> orders = new ArrayList<>();
        for (SortRule rule : rules) {
            Expression<?> expression = resolvePath(root, rule.getField().getRootAttribute(),
                    rule.getField().getNestedAttribute());
            orders.add(rule.isAscending() ? cb.asc(expression) : cb.desc(expression));
        }
        return orders;
    }

    public static List<Predicate> buildPredicates(CriteriaBuilder cb, Root<LabWork> root,
                                                  List<FilterCondition> conditions) {
        List<Predicate> predicates = new ArrayList<>();
        for (FilterCondition condition : conditions) {
            predicates.add(buildPredicate(cb, root, condition));
        }
        return predicates;
    }

    private static Predicate buildPredicate(CriteriaBuilder cb, Root<LabWork> root,
                                            FilterCondition condition) {
        FilterField field = condition.getField();
        FilterOperator operator = condition.getOperator();
        Expression<?> path = resolvePath(root, field.getRootAttribute(), field.getNestedAttribute());

        if (operator == FilterOperator.ISNULL) {
            return cb.isNull(path);
        }
        if (operator == FilterOperator.NOTNULL) {
            return cb.isNotNull(path);
        }

        Object value = convertValue(cb, field, condition.getRawValue());

        switch (operator) {
            case EQ:
                return cb.equal(path, value);
            case NE:
                return cb.notEqual(path, value);
            case GT:
                return cb.greaterThan((Expression<? extends Comparable>) path, (Comparable) value);
            case GE:
                return cb.greaterThanOrEqualTo((Expression<? extends Comparable>) path, (Comparable) value);
            case LT:
                return cb.lessThan((Expression<? extends Comparable>) path, (Comparable) value);
            case LE:
                return cb.lessThanOrEqualTo((Expression<? extends Comparable>) path, (Comparable) value);
            case CONTAINS:
                if (field.getType() != FilterField.FieldType.STRING) {
                    throw new BadRequestException(
                            "Оператор contains не применим к полю " + field.getExternalName());
                }
                return cb.like(cb.lower((Expression<String>) path),
                        "%" + value.toString().toLowerCase() + "%");
            default:
                throw new BadRequestException("Неподдерживаемый оператор фильтрации");
        }
    }

    private static Expression<?> resolvePath(Root<LabWork> root, String attribute, String nested) {
        Path<?> path = root.get(attribute);
        if (nested != null) {
            path = path.get(nested);
        }
        return path;
    }

    private static Object convertValue(CriteriaBuilder cb, FilterField field, String rawValue) {
        try {
            switch (field.getType()) {
                case NUMBER:
                    return Double.parseDouble(rawValue.trim());
                case ENUM:
                    return Difficulty.valueOf(rawValue.trim());
                case DATE:
                    return parseDate(rawValue.trim());
                case STRING:
                case OBJECT:
                default:
                    return rawValue;
            }
        } catch (RuntimeException ex) {
            throw new BadRequestException(
                    "Некорректное значение для поля " + field.getExternalName() + ": " + rawValue);
        }
    }

    private static Object parseDate(String raw) {
        String value = raw;
        if (value.endsWith("Z")) {
            return ZonedDateTime.parse(value);
        }
        if (value.contains("T")) {
            try {
                return ZonedDateTime.parse(value);
            } catch (RuntimeException ex) {
                return OffsetDateTime.parse(value).toZonedDateTime();
            }
        }
        return LocalDate.parse(value).atStartOfDay(java.time.ZoneOffset.UTC);
    }
}
