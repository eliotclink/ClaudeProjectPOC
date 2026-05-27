package com.example.claudeprojectpoc.service;

import com.example.claudeprojectpoc.model.Pet;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PetService {

    private final ConcurrentHashMap<String, Pet> store = new ConcurrentHashMap<>();

    public Mono<Pet> addPet(Pet pet) {
        Pet newPet = new Pet(pet.getName(), pet.getSpecies(), pet.getBreed(), pet.getAge(), pet.getPrice());
        store.put(newPet.getId(), newPet);
        return Mono.just(newPet);
    }

    public Mono<Pet> getPetById(String id) {
        return Mono.justOrEmpty(store.get(id));
    }

    public Mono<Pet> patchPet(String id, Pet patch) {
        Pet existing = store.get(id);
        if (existing == null) {
            return Mono.error(new NoSuchElementException("Pet not found: " + id));
        }
        if (patch.getName() != null) existing.setName(patch.getName());
        if (patch.getSpecies() != null) existing.setSpecies(patch.getSpecies());
        if (patch.getBreed() != null) existing.setBreed(patch.getBreed());
        if (patch.getAge() != null) existing.setAge(patch.getAge());
        if (patch.getPrice() != null) existing.setPrice(patch.getPrice());
        return Mono.just(existing);
    }

    public Mono<Void> deletePet(String id) {
        if (store.remove(id) == null) {
            return Mono.error(new NoSuchElementException("Pet not found: " + id));
        }
        return Mono.empty();
    }

    public Flux<Pet> getAllPets() {
        return Flux.fromIterable(store.values());
    }
}
