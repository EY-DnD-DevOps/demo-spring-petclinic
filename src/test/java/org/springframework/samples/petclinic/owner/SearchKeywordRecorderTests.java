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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link SearchKeywordRecorder}.
 */
@ExtendWith(MockitoExtension.class)
class SearchKeywordRecorderTests {

	@Mock
	private SearchKeywordRepository searchKeywords;

	@InjectMocks
	private SearchKeywordRecorder recorder;

	@Test
	void should_saveKeyword_when_normalKeywordProvided() {
		recorder.record("fluffy");

		ArgumentCaptor<SearchKeyword> captor = ArgumentCaptor.forClass(SearchKeyword.class);
		verify(searchKeywords).save(captor.capture());
		assertThat(captor.getValue().getKeyword()).isEqualTo("fluffy");
	}

	@Test
	void should_truncateKeyword_when_keywordExceeds255Characters() {
		String longKeyword = "a".repeat(300);

		recorder.record(longKeyword);

		ArgumentCaptor<SearchKeyword> captor = ArgumentCaptor.forClass(SearchKeyword.class);
		verify(searchKeywords).save(captor.capture());
		assertThat(captor.getValue().getKeyword()).hasSize(255);
	}

	@Test
	void should_notThrow_when_repositoryThrowsException() {
		given(searchKeywords.save(any())).willThrow(new RuntimeException("DB error"));

		// must not propagate exception to caller
		recorder.record("cat");
	}

}
