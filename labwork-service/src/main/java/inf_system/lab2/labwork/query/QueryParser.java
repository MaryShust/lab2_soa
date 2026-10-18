package inf_system.lab2.labwork.query;

import inf_system.lab2.labwork.exception.BadRequestException;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class QueryParser {

    private QueryParser() {
    }

    public static List<SortRule> parseSort(String sort) {
        List<SortRule> rules = new ArrayList<>();
        if (!StringUtils.hasText(sort)) {
            return rules;
        }
        for (String part : sort.split(",")) {
            String token = part.trim();
            if (token.isEmpty()) {
                continue;
            }
            String[] pieces = token.split(":");
            if (pieces.length != 2) {
                throw new BadRequestException("Некорректное правило сортировки: " + token);
            }
            String fieldName = pieces[0].trim();
            String direction = pieces[1].trim().toLowerCase();
            SortField field;
            try {
                field = SortField.fromExternal(fieldName);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Неизвестное поле сортировки: " + fieldName);
            }
            boolean ascending;
            if (direction.equals("asc")) {
                ascending = true;
            } else if (direction.equals("desc")) {
                ascending = false;
            } else {
                throw new BadRequestException("Некорректное направление сортировки: " + direction);
            }
            rules.add(new SortRule(field, ascending));
        }
        return rules;
    }

    public static List<FilterCondition> parseFilter(String filter) {
        List<FilterCondition> conditions = new ArrayList<>();
        if (!StringUtils.hasText(filter)) {
            return conditions;
        }
        for (String part : filter.split(",")) {
            String token = part.trim();
            if (token.isEmpty()) {
                continue;
            }
            String[] pieces = token.split(":", 3);
            if (pieces.length < 2) {
                throw new BadRequestException("Некорректное условие фильтрации: " + token);
            }
            String fieldName = pieces[0].trim();
            String operatorName = pieces[1].trim().toLowerCase();
            String value = pieces.length == 3 ? pieces[2] : null;

            FilterField field;
            try {
                field = FilterField.fromExternal(fieldName);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Неизвестное поле фильтрации: " + fieldName);
            }
            FilterOperator operator;
            try {
                operator = FilterOperator.fromExternal(operatorName);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Неизвестный оператор фильтрации: " + operatorName);
            }
            if (!FilterField.allowedOperators(field).contains(operator)) {
                throw new BadRequestException(
                        "Оператор " + operatorName + " не применим к полю " + fieldName);
            }
            if (operator == FilterOperator.ISNULL || operator == FilterOperator.NOTNULL) {
                value = null;
            } else if (value == null || value.isEmpty()) {
                throw new BadRequestException(
                        "Не указано значение для условия фильтрации: " + token);
            }
            conditions.add(new FilterCondition(field, operator, value));
        }
        return conditions;
    }
}
