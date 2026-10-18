package inf_system.lab2.labwork.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;

@Entity
@Table(name = "lab_work")
public class LabWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Coordinates coordinates;

    @Column(name = "creation_date", nullable = false)
    private ZonedDateTime creationDate;

    @Column(name = "minimal_point", nullable = false)
    private Double minimalPoint;

    @Column(name = "personal_qualities_minimum", nullable = false)
    private Double personalQualitiesMinimum;

    @Column(name = "average_point", nullable = false)
    private Double averagePoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "author_id")
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
