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
package org.springframework.samples.petclinic.system;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.info.GitProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the {@link AppInfoApiController}.
 */
@WebMvcTest(AppInfoApiController.class)
@DisabledInNativeImage
@DisabledInAotMode
class AppInfoApiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GitProperties gitProperties;

	@Test
	void should_returnFormattedLastUpdatedAt_when_gitInfoExists() throws Exception {
		given(this.gitProperties.getCommitTime()).willReturn(Instant.parse("2026-09-29T06:30:00Z"));

		mockMvc.perform(get("/api/app-info").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.error").value(nullValue()))
			.andExpect(jsonPath("$.data.lastUpdatedAt").value("2026/09/29 14:30"));
	}

	@Test
	void should_returnNullLastUpdatedAt_when_commitTimeMissing() throws Exception {
		given(this.gitProperties.getCommitTime()).willReturn(null);

		mockMvc.perform(get("/api/app-info").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.lastUpdatedAt").value(nullValue()));
	}

	@Test
	void should_returnNullLastUpdatedAt_when_gitInfoMissing() {
		AppInfoApiController controller = new AppInfoApiController(
				new StaticListableBeanFactory().getBeanProvider(GitProperties.class));

		ApiResponse<AppInfo> response = controller.showAppInfo();

		assertThat(response.success()).isTrue();
		assertThat(response.data().lastUpdatedAt()).isNull();
	}

}
