package inf_system.lab2.labwork.dto;

import java.time.LocalDate;

public class PersonDto {

    private String name;
    private LocalDate birthday;
    private Long height;
    private Double weight;
    private String passportID;

    public PersonDto() {
    }

    public PersonDto(String name, LocalDate birthday, Long height, Double weight, String passportID) {
        this.name = name;
        this.birthday = birthday;
        this.height = height;
        this.weight = weight;
        this.passportID = passportID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getPassportID() {
        return passportID;
    }

    public void setPassportID(String passportID) {
        this.passportID = passportID;
    }
}
