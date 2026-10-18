package inf_system.lab2.client.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class LabWorkForm {

    private String name;
    private Double x;
    private Double y;
    private Double minimalPoint;
    private Double personalQualitiesMinimum;
    private Double averagePoint;
    private String difficulty;
    private String authorName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate authorBirthday;

    private Long authorHeight;
    private Double authorWeight;
    private String authorPassportID;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getMinimalPoint() {
        return minimalPoint;
    }

    public void setMinimalPoint(Double minimalPoint) {
        this.minimalPoint = minimalPoint;
    }

    public Double getPersonalQualitiesMinimum() {
        return personalQualitiesMinimum;
    }

    public void setPersonalQualitiesMinimum(Double personalQualitiesMinimum) {
        this.personalQualitiesMinimum = personalQualitiesMinimum;
    }

    public Double getAveragePoint() {
        return averagePoint;
    }

    public void setAveragePoint(Double averagePoint) {
        this.averagePoint = averagePoint;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public LocalDate getAuthorBirthday() {
        return authorBirthday;
    }

    public void setAuthorBirthday(LocalDate authorBirthday) {
        this.authorBirthday = authorBirthday;
    }

    public Long getAuthorHeight() {
        return authorHeight;
    }

    public void setAuthorHeight(Long authorHeight) {
        this.authorHeight = authorHeight;
    }

    public Double getAuthorWeight() {
        return authorWeight;
    }

    public void setAuthorWeight(Double authorWeight) {
        this.authorWeight = authorWeight;
    }

    public String getAuthorPassportID() {
        return authorPassportID;
    }

    public void setAuthorPassportID(String authorPassportID) {
        this.authorPassportID = authorPassportID;
    }
}
