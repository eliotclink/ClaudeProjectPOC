package com.example.claudeprojectpoc.controller;

import com.example.claudeprojectpoc.model.Pet;
import com.example.claudeprojectpoc.service.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(PetController.class)
@WithMockUser
class PetControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PetService petService;

    private Pet pet;

    @BeforeEach
    void setUp() {
        pet = new Pet();
        pet.setId("test-id");
        pet.setName("Buddy");
        pet.setSpecies("Dog");
        pet.setBreed("Labrador");
        pet.setAge(2);
        pet.setPrice(500.00);
    }

    @Test
    void addPet_returnsCreatedPet() {
        when(petService.addPet(any())).thenReturn(Mono.just(pet));

        webTestClient.mutateWith(csrf()).post().uri("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pet)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Pet.class)
                .isEqualTo(pet);
    }

    @Test
    void getAllPets_returnsList() {
        when(petService.getAllPets()).thenReturn(Flux.just(pet));

        webTestClient.get().uri("/api/pets")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Pet.class)
                .hasSize(1)
                .contains(pet);
    }

    @Test
    void getPetById_returnsFound() {
        when(petService.getPetById("test-id")).thenReturn(Mono.just(pet));

        webTestClient.get().uri("/api/pets/test-id")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Pet.class)
                .isEqualTo(pet);
    }

    @Test
    void getPetById_returnsNotFound() {
        when(petService.getPetById("unknown")).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/pets/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void patchPet_returnsUpdatedPet() {
        when(petService.patchPet(eq("test-id"), any())).thenReturn(Mono.just(pet));

        webTestClient.mutateWith(csrf()).patch().uri("/api/pets/test-id")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pet)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Pet.class)
                .isEqualTo(pet);
    }

    @Test
    void patchPet_returnsNotFound() {
        when(petService.patchPet(eq("unknown"), any()))
                .thenReturn(Mono.error(new NoSuchElementException("Pet not found: unknown")));

        webTestClient.mutateWith(csrf()).patch().uri("/api/pets/unknown")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pet)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePet_returnsNoContent() {
        when(petService.deletePet("test-id")).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf()).delete().uri("/api/pets/test-id")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deletePet_returnsNotFound() {
        when(petService.deletePet("unknown"))
                .thenReturn(Mono.error(new NoSuchElementException("Pet not found: unknown")));

        webTestClient.mutateWith(csrf()).delete().uri("/api/pets/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }
}
