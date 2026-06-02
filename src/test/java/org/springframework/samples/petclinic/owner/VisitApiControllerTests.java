package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link VisitApiController}.
 */
@WebMvcTest(VisitApiController.class)
@DisabledInNativeImage
@DisabledInAotMode
class VisitApiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OwnerRepository ownerRepository;

	private Owner testOwner;

	private Pet testPet;

	@BeforeEach
	void setup() {
		testPet = new Pet();
		testPet.setId(1);
		testPet.setName("Buddy");
		testPet.setBirthDate(LocalDate.of(2020, 1, 1));

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
	void should_createVisit_when_validDataProvided() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));
		given(ownerRepository.save(any())).willReturn(testOwner);

		Visit visit = new Visit();
		visit.setDate(LocalDate.of(2026, 6, 1));
		visit.setDescription("Annual checkup");

		mockMvc
			.perform(post("/api/owners/1/pets/1/visits").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(visit)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void should_return404_when_ownerNotFound() throws Exception {
		given(ownerRepository.findById(99)).willReturn(Optional.empty());

		Visit visit = new Visit();
		visit.setDescription("checkup");

		mockMvc
			.perform(post("/api/owners/99/pets/1/visits").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(visit)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void should_return404_when_petNotFoundInOwner() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));

		Visit visit = new Visit();
		visit.setDescription("checkup");

		mockMvc
			.perform(post("/api/owners/1/pets/99/visits").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(visit)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false));
	}

}
