package org.springframework.samples.petclinic.owner;

import java.util.List;
import java.util.Optional;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link OwnerApiController}.
 */
@WebMvcTest(OwnerApiController.class)
@DisabledInNativeImage
@DisabledInAotMode
class OwnerApiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OwnerRepository ownerRepository;

	private Owner testOwner;

	@BeforeEach
	void setup() {
		testOwner = new Owner();
		testOwner.setId(1);
		testOwner.setFirstName("George");
		testOwner.setLastName("Franklin");
		testOwner.setAddress("110 W. Liberty St.");
		testOwner.setCity("Madison");
		testOwner.setTelephone("6085551023");
	}

	@Test
	void should_returnPagedOwners_when_searchingByLastName() throws Exception {
		given(ownerRepository.findByLastNameStartingWith(anyString(), any()))
			.willReturn(new PageImpl<>(List.of(testOwner), PageRequest.of(0, 5), 1));

		mockMvc.perform(get("/api/owners").param("lastName", "Franklin").param("page", "0"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.content[0].lastName").value("Franklin"));
	}

	@Test
	void should_returnOwner_when_ownerExists() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));

		mockMvc.perform(get("/api/owners/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.firstName").value("George"));
	}

	@Test
	void should_return404_when_ownerNotFound() throws Exception {
		given(ownerRepository.findById(99)).willReturn(Optional.empty());

		mockMvc.perform(get("/api/owners/99"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void should_createOwner_when_validDataProvided() throws Exception {
		Owner newOwner = new Owner();
		newOwner.setFirstName("John");
		newOwner.setLastName("Doe");
		newOwner.setAddress("123 Main St");
		newOwner.setCity("Springfield");
		newOwner.setTelephone("1234567890");

		Owner savedOwner = new Owner();
		savedOwner.setId(2);
		savedOwner.setFirstName("John");
		savedOwner.setLastName("Doe");
		savedOwner.setAddress("123 Main St");
		savedOwner.setCity("Springfield");
		savedOwner.setTelephone("1234567890");

		given(ownerRepository.save(any())).willReturn(savedOwner);

		mockMvc
			.perform(post("/api/owners").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newOwner)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.id").value(2));
	}

	@Test
	void should_updateOwner_when_validDataProvided() throws Exception {
		testOwner.setCity("New City");

		given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));
		given(ownerRepository.save(any())).willReturn(testOwner);

		mockMvc
			.perform(put("/api/owners/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testOwner)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.city").value("New City"));
	}

}
