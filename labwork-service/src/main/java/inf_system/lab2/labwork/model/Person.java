package inf_system.lab2.labwork.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "person")
public class Person implements Comparable<Person> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private LocalDate birthday;

    @Column
    private Long height;

    @Column(nullable = false)
    private Double weight;

    @Column(name = "passport_id", length = 34)
    private String passportID;

    public Person() {
    }

    public Person(String name, LocalDate birthday, Long height, Double weight, String passportID) {
        this.name = name;
        this.birthday = birthday;
        this.height = height;
        this.weight = weight;
        this.passportID = passportID;
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

    @Override
    public int compareTo(Person other) {
        int result = nullsFirstString(this.name, other.name);
        if (result != 0) {
            return result;
        }
        result = nullsFirstComparable(this.birthday, other.birthday);
        if (result != 0) {
            return result;
        }
        result = nullsFirstComparable(this.height, other.height);
        if (result != 0) {
            return result;
        }
        result = nullsFirstComparable(this.weight, other.weight);
        if (result != 0) {
            return result;
        }
        return nullsFirstString(this.passportID, other.passportID);
    }

    private static <T extends Comparable<T>> int nullsFirstComparable(T left, T right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private static int nullsFirstString(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Person person)) {
            return false;
        }
        return Objects.equals(name, person.name)
                && Objects.equals(birthday, person.birthday)
                && Objects.equals(height, person.height)
                && Objects.equals(weight, person.weight)
                && Objects.equals(passportID, person.passportID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birthday, height, weight, passportID);
    }
}
