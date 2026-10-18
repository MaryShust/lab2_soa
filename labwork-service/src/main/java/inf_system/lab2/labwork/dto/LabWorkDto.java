package inf_system.lab2.labwork.dto;

import inf_system.lab2.labwork.model.Difficulty;

import java.time.ZonedDateTime;

public class LabWorkDto {

    private Long id;
    private String name;
    private CoordinatesDto coordinates;
    private ZonedDateTime creationDate;
    private Double minimalPoint;
    private Double personalQualitiesMinimum;
    private Double averagePoint;
    private Difficulty difficulty;
    private PersonDto author;

    public LabWorkDto() {
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

    public CoordinatesDto getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(CoordinatesDto coordinates) {
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

    public PersonDto getAuthor() {
        return author;
    }

    public void setAuthor(PersonDto author) {
        this.author = author;
    }
}
