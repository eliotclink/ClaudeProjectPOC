package com.example.claudeprojectpoc.repository;

import com.example.claudeprojectpoc.model.Pet;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PetRepository extends ReactiveCrudRepository<Pet, String> {
}
