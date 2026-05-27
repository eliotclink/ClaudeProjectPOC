package com.example.claudeprojectpoc.service;

import com.example.claudeprojectpoc.model.Pet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class PetServiceTest {

    private PetService petService;
    private String existingId;

    @BeforeEach
    void setUp() {
        petService = new PetService();
        Pet added = petService.addPet(new Pet("Grinch", "Cat", "Russian Blue", 21, 1.00)).block();
        existingId = added.getId();
    }

    @Test
    void patchPet_updatesOnlySuppliedFields() {
        Pet patch = new Pet();
        patch.setPrice(500.00);

        StepVerifier.create(petService.patchPet(existingId, patch))
                .assertNext(updated -> {
                    assert updated.getPrice() == 500.00;
                    assert updated.getName().equals("Grinch");
                    assert updated.getSpecies().equals("Cat");
                    assert updated.getBreed().equals("Russian Blue");
                    assert updated.getAge() == 21;
                })
                .verifyComplete();
    }

    @Test
    void patchPet_returnsErrorForUnknownId() {
        Pet patch = new Pet();
        patch.setPrice(500.00);

        StepVerifier.create(petService.patchPet("unknown-id", patch))
                .expectErrorMatches(e -> e instanceof java.util.NoSuchElementException
                        && e.getMessage().equals("Pet not found: unknown-id"))
                .verify();
    }

    @Test
    void deletePet_returnsErrorForUnknownId() {
        StepVerifier.create(petService.deletePet("unknown-id"))
                .expectErrorMatches(e -> e instanceof java.util.NoSuchElementException
                        && e.getMessage().equals("Pet not found: unknown-id"))
                .verify();
    }
}
