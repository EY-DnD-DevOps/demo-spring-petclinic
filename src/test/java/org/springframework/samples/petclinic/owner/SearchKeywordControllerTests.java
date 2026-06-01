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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link SearchKeywordController}.
 */
@WebMvcTest(SearchKeywordController.class)
@DisabledInNativeImage
@DisabledInAotMode
class SearchKeywordControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SearchKeywordRepository searchKeywords;

	@Test
	void should_returnJsonSummary_when_keywordsExist() throws Exception {
		given(searchKeywords.findKeywordSummary())
			.willReturn(List.of(new KeywordCount("cat", 5), new KeywordCount("dog", 3)));

		mockMvc.perform(get("/api/search-keywords/summary"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.error").value(nullValue()))
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.data", hasSize(2)))
			.andExpect(jsonPath("$.data[0].keyword", is("cat")))
			.andExpect(jsonPath("$.data[0].count", is(5)))
			.andExpect(jsonPath("$.data[1].keyword", is("dog")))
			.andExpect(jsonPath("$.data[1].count", is(3)));
	}

	@Test
	void should_returnEmptyArray_when_noKeywordsExist() throws Exception {
		given(searchKeywords.findKeywordSummary()).willReturn(List.of());

		mockMvc.perform(get("/api/search-keywords/summary"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data", hasSize(0)));
	}

}
