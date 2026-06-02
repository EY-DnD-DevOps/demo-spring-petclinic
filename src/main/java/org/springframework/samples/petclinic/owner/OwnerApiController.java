package org.springframework.samples.petclinic.owner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.system.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST API controller for owner resources.
 */
@RestController
@RequestMapping("/api/owners")
class OwnerApiController {

	private static final int PAGE_SIZE = 5;

	private final OwnerRepository owners;

	OwnerApiController(OwnerRepository owners) {
		this.owners = owners;
	}

	@GetMapping
	ApiResponse<Page<Owner>> listOwners(@RequestParam(defaultValue = "") String lastName,
			@RequestParam(defaultValue = "0") int page) {
		Pageable pageable = PageRequest.of(page, PAGE_SIZE);
		Page<Owner> result = owners.findByLastNameStartingWith(lastName, pageable);
		return ApiResponse.success(result);
	}

	@GetMapping("/{ownerId}")
	ApiResponse<Owner> getOwner(@PathVariable int ownerId) {
		Owner owner = owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		return ApiResponse.success(owner);
	}

	@PostMapping
	ResponseEntity<ApiResponse<Owner>> createOwner(@Valid @RequestBody Owner owner) {
		owner.setId(null);
		Owner saved = owners.save(owner);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved));
	}

	@PutMapping("/{ownerId}")
	ApiResponse<Owner> updateOwner(@PathVariable int ownerId, @Valid @RequestBody Owner owner) {
		owners.findById(ownerId).orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		owner.setId(ownerId);
		Owner saved = owners.save(owner);
		return ApiResponse.success(saved);
	}

}
