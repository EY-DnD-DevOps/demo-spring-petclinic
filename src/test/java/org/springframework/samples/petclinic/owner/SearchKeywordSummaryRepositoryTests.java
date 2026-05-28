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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SearchKeywordRepository#findKeywordSummary()}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class SearchKeywordSummaryRepositoryTests {

	@Autowired
	private SearchKeywordRepository searchKeywords;

	@Test
	@Transactional
	void should_returnKeywordsOrderedByCountDesc_when_multipleKeywordsSaved() {
		searchKeywords.save(new SearchKeyword("cat"));
		searchKeywords.save(new SearchKeyword("cat"));
		searchKeywords.save(new SearchKeyword("cat"));
		searchKeywords.save(new SearchKeyword("dog"));
		searchKeywords.save(new SearchKeyword("dog"));
		searchKeywords.save(new SearchKeyword("fish"));

		List<KeywordCount> summary = searchKeywords.findKeywordSummary();

		assertThat(summary).isNotEmpty();
		assertThat(summary.get(0).keyword()).isEqualTo("cat");
		assertThat(summary.get(0).count()).isEqualTo(3);
		assertThat(summary.get(1).keyword()).isEqualTo("dog");
		assertThat(summary.get(1).count()).isEqualTo(2);
	}

	@Test
	@Transactional
	void should_returnEmptyList_when_noKeywordsSaved() {
		List<KeywordCount> summary = searchKeywords.findKeywordSummary();

		assertThat(summary).isEmpty();
	}

}
