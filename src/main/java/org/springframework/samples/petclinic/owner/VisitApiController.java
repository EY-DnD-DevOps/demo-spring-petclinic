package org.springframework.samples.petclinic.owner;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.system.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for visit resources.
 */
@RestController
class VisitApiController {

	private final OwnerRepository owners;

	VisitApiController(OwnerRepository owners) {
		this.owners = owners;
	}

	@PostMapping("/api/owners/{ownerId}/pets/{petId}/visits")
	ResponseEntity<ApiResponse<Owner>> createVisit(@PathVariable int ownerId, @PathVariable int petId,
			@Valid @RequestBody Visit visit) {
		Owner owner = owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId + " for owner: " + ownerId);
		}
		owner.addVisit(petId, visit);
		Owner saved = owners.save(owner);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved));
	}

}
