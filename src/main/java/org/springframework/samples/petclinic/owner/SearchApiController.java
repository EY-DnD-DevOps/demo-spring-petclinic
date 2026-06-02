package org.springframework.samples.petclinic.owner;

import java.util.List;

import org.springframework.samples.petclinic.system.ApiResponse;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for global search across owners, pets, and vets.
 */
@RestController
class SearchApiController {

	record SearchResult(List<Owner> owners, List<PetResult> pets, List<Vet> vets) {
	}

	record PetResult(Pet pet, Owner owner) {
	}

	private final OwnerRepository owners;

	private final VetRepository vets;

	private final SearchKeywordRecorder recorder;

	SearchApiController(OwnerRepository owners, VetRepository vets, SearchKeywordRecorder recorder) {
		this.owners = owners;
		this.vets = vets;
		this.recorder = recorder;
	}

	@GetMapping("/api/search")
	ApiResponse<SearchResult> search(@RequestParam(defaultValue = "") String query) {
		if (query.isBlank()) {
			return ApiResponse.success(new SearchResult(List.of(), List.of(), List.of()));
		}

		recorder.record(query);
		String escaped = query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");

		List<Owner> ownerResults = owners.searchByName(escaped);
		List<PetResult> petResults = owners.findOwnersByPetName(escaped)
			.stream()
			.flatMap(owner -> owner.getPets()
				.stream()
				.filter(pet -> pet.getName() != null && pet.getName().toLowerCase().contains(query.toLowerCase()))
				.map(pet -> new PetResult(pet, owner)))
			.toList();
		List<Vet> vetResults = vets.searchByName(escaped);

		return ApiResponse.success(new SearchResult(ownerResults, petResults, vetResults));
	}

}
