package inf_system.lab2.bars.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public class LabWork {

    private Long id;
    private String name;
    private Coordinates coordinates;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ZonedDateTime creationDate;

    private Double minimalPoint;
    private Double personalQualitiesMinimum;
    private Double averagePoint;
    private Difficulty difficulty;
    private Person author;

    public LabWork() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
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

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Person getAuthor() {
        return author;
    }

    public void setAuthor(Person author) {
        this.author = author;
    }
}
