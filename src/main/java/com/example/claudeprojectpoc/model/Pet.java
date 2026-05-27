package com.example.claudeprojectpoc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("pets")
public class Pet implements Persistable<String> {

    @Id
    private String id;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private Double price;

    @Transient
    private boolean newEntity;

    public Pet() {}

    public Pet(String name, String species, String breed, Integer age, Double price) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.price = price;
        this.newEntity = true;
    }

    @Override
    public String getId() { return id; }

    @Override
    @JsonIgnore
    public boolean isNew() { return newEntity; }

    public String getName() { return name; }
    public String getSpecies() { return species; }
    public String getBreed() { return breed; }
    public Integer getAge() { return age; }
    public Double getPrice() { return price; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSpecies(String species) { this.species = species; }
    public void setBreed(String breed) { this.breed = breed; }
    public void setAge(Integer age) { this.age = age; }
    public void setPrice(Double price) { this.price = price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pet other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
