package com.example.claudeprojectpoc.controller;

import com.example.claudeprojectpoc.model.Pet;
import com.example.claudeprojectpoc.service.PetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Pet> addPet(@RequestBody Pet pet) {
        return petService.addPet(pet);
    }

    @GetMapping
    public Flux<Pet> getAllPets() {
        return petService.getAllPets();
    }

    @GetMapping("/{id}")
    public Mono<Pet> getPetById(@PathVariable String id) {
        return petService.getPetById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found")));
    }

    @PatchMapping("/{id}")
    public Mono<Pet> patchPet(@PathVariable String id, @RequestBody Pet patch) {
        return petService.patchPet(id, patch)
                .onErrorMap(NoSuchElementException.class, e ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deletePet(@PathVariable String id) {
        return petService.deletePet(id)
                .onErrorMap(NoSuchElementException.class, e ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }
}
