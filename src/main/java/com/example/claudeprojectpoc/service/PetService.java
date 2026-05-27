package com.example.claudeprojectpoc.service;

import com.example.claudeprojectpoc.model.Pet;
import com.example.claudeprojectpoc.repository.PetRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
public class PetService {

    private final PetRepository repository;

    public PetService(PetRepository repository) {
        this.repository = repository;
    }

    public Mono<Pet> addPet(Pet pet) {
        return repository.save(new Pet(pet.getName(), pet.getSpecies(), pet.getBreed(), pet.getAge(), pet.getPrice()));
    }

    public Flux<Pet> getAllPets() {
        return repository.findAll();
    }

    public Mono<Pet> getPetById(String id) {
        return repository.findById(id);
    }

    public Mono<Pet> patchPet(String id, Pet patch) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Pet not found: " + id)))
                .flatMap(existing -> {
                    if (patch.getName() != null) existing.setName(patch.getName());
                    if (patch.getSpecies() != null) existing.setSpecies(patch.getSpecies());
                    if (patch.getBreed() != null) existing.setBreed(patch.getBreed());
                    if (patch.getAge() != null) existing.setAge(patch.getAge());
                    if (patch.getPrice() != null) existing.setPrice(patch.getPrice());
                    return repository.save(existing);
                });
    }

    public Mono<Void> deletePet(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Pet not found: " + id)))
                .flatMap(repository::delete);
    }
}
