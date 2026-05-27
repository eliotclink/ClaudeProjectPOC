package com.example.claudeprojectpoc.model;

import java.util.UUID;

public class Pet {

    private String id;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private Double price;

    public Pet() {}

    public Pet(String name, String species, String breed, Integer age, Double price) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.price = price;
    }

    public String getId() { return id; }
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
        return java.util.Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }
}
