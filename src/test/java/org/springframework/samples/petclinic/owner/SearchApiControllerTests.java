package org.springframework.samples.petclinic.owner;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link SearchApiController}.
 */
@WebMvcTest(SearchApiController.class)
@DisabledInNativeImage
@DisabledInAotMode
class SearchApiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository ownerRepository;

	@MockitoBean
	private VetRepository vetRepository;

	@MockitoBean
	private SearchKeywordRecorder recorder;

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
	void should_returnSearchResults_when_queryProvided() throws Exception {
		given(ownerRepository.searchByName(anyString())).willReturn(List.of(testOwner));
		given(ownerRepository.findOwnersByPetName(anyString())).willReturn(List.of());
		given(vetRepository.searchByName(anyString())).willReturn(List.of());

		mockMvc.perform(get("/api/search").param("query", "Franklin"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.owners[0].lastName").value("Franklin"));
	}

	@Test
	void should_returnEmptyResults_when_emptyQuery() throws Exception {
		mockMvc.perform(get("/api/search").param("query", ""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.owners").isEmpty())
			.andExpect(jsonPath("$.data.vets").isEmpty());
	}

}
