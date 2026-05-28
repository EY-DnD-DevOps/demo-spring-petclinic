/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.List;

import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the global search feature. Searches owners (by first/last name), pets
 * (by name), and vets (by first/last name) across the entire application.
 */
@Controller
class SearchController {

	record PetResult(Pet pet, Owner owner) {
	}

	private final OwnerRepository owners;

	private final VetRepository vets;

	private final SearchKeywordRecorder recorder;

	SearchController(OwnerRepository owners, VetRepository vets, SearchKeywordRecorder recorder) {
		this.owners = owners;
		this.vets = vets;
		this.recorder = recorder;
	}

	@GetMapping("/search")
	public String search(@RequestParam(defaultValue = "") String query, Model model) {
		List<Owner> ownerResults = List.of();
		List<PetResult> petResults = List.of();
		List<Vet> vetResults = List.of();

		if (!query.isBlank()) {
			recorder.record(query);
			String escapedQuery = escapeWildcards(query);
			ownerResults = owners.searchByName(escapedQuery);
			petResults = owners.findOwnersByPetName(escapedQuery)
				.stream()
				.flatMap(owner -> owner.getPets().stream().map(pet -> new PetResult(pet, owner)))
				.toList();
			vetResults = vets.searchByName(escapedQuery);
		}

		model.addAttribute("ownerResults", ownerResults);
		model.addAttribute("petResults", petResults);
		model.addAttribute("vetResults", vetResults);
		model.addAttribute("query", query);
		return "search/searchResults";
	}

	private static String escapeWildcards(String input) {
		return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

}
