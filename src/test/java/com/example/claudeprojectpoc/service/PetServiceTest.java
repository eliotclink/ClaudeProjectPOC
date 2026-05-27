package com.example.claudeprojectpoc.service;

import com.example.claudeprojectpoc.model.Pet;
import com.example.claudeprojectpoc.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository repository;

    @InjectMocks
    private PetService petService;

    private Pet existing;

    @BeforeEach
    void setUp() {
        existing = new Pet();
        existing.setId("test-id");
        existing.setName("Grinch");
        existing.setSpecies("Cat");
        existing.setBreed("Russian Blue");
        existing.setAge(21);
        existing.setPrice(1.00);
    }

    @Test
    void patchPet_updatesOnlySuppliedFields() {
        Pet patch = new Pet();
        patch.setPrice(500.00);

        when(repository.findById("test-id")).thenReturn(Mono.just(existing));
        when(repository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(petService.patchPet("test-id", patch))
                .assertNext(updated -> {
                    assert updated.getPrice() == 500.00;
                    assert "Grinch".equals(updated.getName());
                    assert "Cat".equals(updated.getSpecies());
                    assert "Russian Blue".equals(updated.getBreed());
                    assert updated.getAge() == 21;
                })
                .verifyComplete();
    }

    @Test
    void patchPet_returnsErrorForUnknownId() {
        when(repository.findById("unknown-id")).thenReturn(Mono.empty());

        StepVerifier.create(petService.patchPet("unknown-id", new Pet()))
                .expectErrorMatches(e -> e instanceof NoSuchElementException
                        && e.getMessage().equals("Pet not found: unknown-id"))
                .verify();
    }

    @Test
    void deletePet_returnsErrorForUnknownId() {
        when(repository.findById("unknown-id")).thenReturn(Mono.empty());

        StepVerifier.create(petService.deletePet("unknown-id"))
                .expectErrorMatches(e -> e instanceof NoSuchElementException
                        && e.getMessage().equals("Pet not found: unknown-id"))
                .verify();
    }
}
