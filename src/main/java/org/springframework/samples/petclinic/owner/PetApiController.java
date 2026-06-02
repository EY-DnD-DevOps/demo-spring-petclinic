package org.springframework.samples.petclinic.owner;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.system.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for pet and pet-type resources.
 */
@RestController
class PetApiController {

	private final OwnerRepository owners;

	private final PetTypeRepository petTypes;

	PetApiController(OwnerRepository owners, PetTypeRepository petTypes) {
		this.owners = owners;
		this.petTypes = petTypes;
	}

	@GetMapping("/api/pet-types")
	ApiResponse<List<PetType>> listPetTypes() {
		return ApiResponse.success(petTypes.findPetTypes());
	}

	@PostMapping("/api/owners/{ownerId}/pets")
	ResponseEntity<ApiResponse<Owner>> createPet(@PathVariable int ownerId, @Valid @RequestBody Pet pet) {
		Owner owner = owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		pet.setId(null);
		owner.addPet(pet);
		Owner saved = owners.save(owner);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved));
	}

	@PutMapping("/api/owners/{ownerId}/pets/{petId}")
	ApiResponse<Owner> updatePet(@PathVariable int ownerId, @PathVariable int petId, @RequestBody Pet pet) {
		Owner owner = owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		Pet existing = owner.getPet(petId);
		if (existing == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId + " for owner: " + ownerId);
		}
		existing.setName(pet.getName());
		existing.setBirthDate(pet.getBirthDate());
		existing.setType(pet.getType());
		Owner saved = owners.save(owner);
		return ApiResponse.success(saved);
	}

}
