package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link PetApiController}.
 */
@WebMvcTest(PetApiController.class)
@DisabledInNativeImage
@DisabledInAotMode
class PetApiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OwnerRepository ownerRepository;

	@MockitoBean
	private PetTypeRepository petTypeRepository;

	private Owner testOwner;

	private Pet testPet;

	private PetType dogType;

	@BeforeEach
	void setup() {
		dogType = new PetType();
		dogType.setId(1);
		dogType.setName("dog");

		testPet = new Pet();
		testPet.setId(1);
		testPet.setName("Buddy");
		testPet.setBirthDate(LocalDate.of(2020, 1, 1));
		testPet.setType(dogType);

		testOwner = new Owner();
		testOwner.setId(1);
		testOwner.setFirstName("George");
		testOwner.setLastName("Franklin");
		testOwner.setAddress("110 W. Liberty St.");
		testOwner.setCity("Madison");
		testOwner.setTelephone("6085551023");
		testOwner.getPets().add(testPet);
	}

	@Test
	void should_returnPetTypes_when_requested() throws Exception {
		given(petTypeRepository.findPetTypes()).willReturn(List.of(dogType));

		mockMvc.perform(get("/api/pet-types"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].name").value("dog"));
	}

	@Test
	void should_createPet_when_validDataProvided() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));
		given(ownerRepository.save(any())).willReturn(testOwner);

		Pet newPet = new Pet();
		newPet.setName("Max");
		newPet.setBirthDate(LocalDate.of(2021, 5, 10));
		newPet.setType(dogType);

		mockMvc
			.perform(post("/api/owners/1/pets").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newPet)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void should_return404_when_ownerNotFoundOnCreatePet() throws Exception {
		given(ownerRepository.findById(99)).willReturn(Optional.empty());

		Pet newPet = new Pet();
		newPet.setName("Max");
		newPet.setBirthDate(LocalDate.of(2021, 5, 10));
		newPet.setType(dogType);

		mockMvc
			.perform(post("/api/owners/99/pets").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newPet)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void should_updatePet_when_validDataProvided() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));
		given(ownerRepository.save(any())).willReturn(testOwner);

		testPet.setName("Updated Buddy");

		mockMvc
			.perform(put("/api/owners/1/pets/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testPet)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}

}
