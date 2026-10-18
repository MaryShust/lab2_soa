package inf_system.lab2.bars.client;

import inf_system.lab2.bars.model.LabWork;
import inf_system.lab2.bars.model.Person;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LabWorkQueryBuilder {

    public String build(LabWork labWork) {
        List<String> params = new ArrayList<>();
        add(params, "name", labWork.getName());
        if (labWork.getCoordinates() != null) {
            add(params, "x", format(labWork.getCoordinates().getX()));
            add(params, "y", format(labWork.getCoordinates().getY()));
        }
        add(params, "minimal-point", format(labWork.getMinimalPoint()));
        add(params, "personal-qualities-minimum", format(labWork.getPersonalQualitiesMinimum()));
        add(params, "average-point", format(labWork.getAveragePoint()));
        if (labWork.getDifficulty() != null) {
            add(params, "difficulty", labWork.getDifficulty().name());
        }
        Person author = labWork.getAuthor();
        if (author != null) {
            add(params, "author-name", author.getName());
            if (author.getBirthday() != null) {
                add(params, "author-birthday", author.getBirthday().toString());
            }
            add(params, "author-height", format(author.getHeight()));
            add(params, "author-weight", format(author.getWeight()));
            add(params, "author-passport-id", author.getPassportID());
        }
        return String.join("&", params);
    }

    private void add(List<String> params, String key, String value) {
        if (value == null) {
            return;
        }
        params.add(key + "=" + encode(value));
    }

    private String format(Double value) {
        return value == null ? null : String.valueOf(value);
    }

    private String format(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            return value;
        }
    }
}
