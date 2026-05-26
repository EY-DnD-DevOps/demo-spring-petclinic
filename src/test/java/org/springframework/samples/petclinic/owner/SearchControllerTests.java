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

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for {@link SearchController}.
 */
@WebMvcTest(SearchController.class)
@DisabledInNativeImage
@DisabledInAotMode
class SearchControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	private Owner makeOwner(int id, String firstName, String lastName) {
		Owner owner = new Owner();
		owner.setId(id);
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress("123 Main St");
		owner.setCity("Springfield");
		owner.setTelephone("6085551234");
		return owner;
	}

	private Pet makePet(int id, String name, Owner owner) {
		Pet pet = new Pet();
		pet.setName(name);
		PetType type = new PetType();
		type.setName("dog");
		pet.setType(type);
		pet.setBirthDate(LocalDate.of(2020, 1, 1));
		owner.addPet(pet);
		pet.setId(id);
		return pet;
	}

	@Test
	void emptyQueryReturnsEmptyResults() throws Exception {
		mockMvc.perform(get("/search").param("query", ""))
			.andExpect(status().isOk())
			.andExpect(view().name("search/searchResults"))
			.andExpect(model().attribute("ownerResults", empty()))
			.andExpect(model().attribute("petResults", empty()))
			.andExpect(model().attribute("query", ""));
	}

	@Test
	void blankQueryReturnsEmptyResults() throws Exception {
		mockMvc.perform(get("/search").param("query", "   "))
			.andExpect(status().isOk())
			.andExpect(view().name("search/searchResults"))
			.andExpect(model().attribute("ownerResults", empty()))
			.andExpect(model().attribute("petResults", empty()));
	}

	@Test
	void noQueryParamReturnsEmptyResults() throws Exception {
		mockMvc.perform(get("/search"))
			.andExpect(status().isOk())
			.andExpect(view().name("search/searchResults"))
			.andExpect(model().attribute("ownerResults", empty()))
			.andExpect(model().attribute("petResults", empty()));
	}

	@Test
	void validQueryReturnsMatchingOwners() throws Exception {
		Owner george = makeOwner(1, "George", "Franklin");
		given(owners.searchByName(anyString())).willReturn(List.of(george));
		given(owners.findOwnersByPetName(anyString())).willReturn(List.of());

		mockMvc.perform(get("/search").param("query", "George"))
			.andExpect(status().isOk())
			.andExpect(view().name("search/searchResults"))
			.andExpect(model().attribute("ownerResults", hasSize(1)))
			.andExpect(model().attribute("petResults", empty()))
			.andExpect(model().attribute("query", "George"));
	}

	@Test
	void validQueryReturnsMatchingPets() throws Exception {
		Owner george = makeOwner(1, "George", "Franklin");
		makePet(1, "Max", george);
		given(owners.searchByName(anyString())).willReturn(List.of());
		given(owners.findOwnersByPetName(anyString())).willReturn(List.of(george));

		mockMvc.perform(get("/search").param("query", "Max"))
			.andExpect(status().isOk())
			.andExpect(view().name("search/searchResults"))
			.andExpect(model().attribute("ownerResults", empty()))
			.andExpect(model().attribute("petResults", hasSize(1)));
	}

	@Test
	void validQueryReturnsNoResults() throws Exception {
		given(owners.searchByName(anyString())).willReturn(List.of());
		given(owners.findOwnersByPetName(anyString())).willReturn(List.of());

		mockMvc.perform(get("/search").param("query", "zzznomatch"))
			.andExpect(status().isOk())
			.andExpect(view().name("search/searchResults"))
			.andExpect(model().attribute("ownerResults", empty()))
			.andExpect(model().attribute("petResults", empty()));
	}

	@Test
	void wildcardCharactersInQueryAreEscaped() throws Exception {
		given(owners.searchByName(anyString())).willReturn(List.of());
		given(owners.findOwnersByPetName(anyString())).willReturn(List.of());

		// Query with SQL wildcards should not cause errors and should be handled
		// gracefully
		mockMvc.perform(get("/search").param("query", "%_test"))
			.andExpect(status().isOk())
			.andExpect(view().name("search/searchResults"));
	}

	@Test
	void ownerWithMultiplePetsReturnsAllPets() throws Exception {
		Owner george = makeOwner(1, "George", "Franklin");
		makePet(1, "Max", george);
		makePet(2, "Buddy", george);
		given(owners.searchByName(anyString())).willReturn(List.of());
		given(owners.findOwnersByPetName(anyString())).willReturn(List.of(george));

		mockMvc.perform(get("/search").param("query", "Max"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("petResults", hasSize(2)));
	}

	@Test
	void queryIsPreservedInModel() throws Exception {
		given(owners.searchByName(anyString())).willReturn(List.of());
		given(owners.findOwnersByPetName(anyString())).willReturn(List.of());

		mockMvc.perform(get("/search").param("query", "Franklin"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("query", is("Franklin")));
	}

}
