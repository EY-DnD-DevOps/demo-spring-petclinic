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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the global search feature. Searches both owners (by first/last name) and
 * pets (by name) across the entire application.
 */
@Controller
class SearchController {

	record PetResult(Pet pet, Owner owner) {
	}

	private final OwnerRepository owners;

	SearchController(OwnerRepository owners) {
		this.owners = owners;
	}

	@GetMapping("/search")
	public String search(@RequestParam(defaultValue = "") String query, Model model) {
		List<Owner> ownerResults = List.of();
		List<PetResult> petResults = List.of();

		if (!query.isBlank()) {
			ownerResults = owners.searchByName(query);

			String lowerQuery = query.toLowerCase();
			petResults = owners.findOwnersByPetName(query)
				.stream()
				.flatMap(owner -> owner.getPets()
					.stream()
					.filter(pet -> pet.getName().toLowerCase().contains(lowerQuery))
					.map(pet -> new PetResult(pet, owner)))
				.toList();
		}

		model.addAttribute("ownerResults", ownerResults);
		model.addAttribute("petResults", petResults);
		model.addAttribute("query", query);
		return "search/searchResults";
	}

}
