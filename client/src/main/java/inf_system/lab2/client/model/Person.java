package inf_system.lab2.client.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class Person {

    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private Long height;
    private Double weight;
    private String passportID;

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
