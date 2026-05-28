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

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.springframework.samples.petclinic.model.BaseEntity;

/**
 * Records a keyword submitted to the global search, together with the time the search was
 * performed. Used for analyzing frequently searched terms.
 */
@Entity
@Table(name = "search_keywords")
class SearchKeyword extends BaseEntity {

	@Column(name = "keyword", nullable = false, length = 255)
	private String keyword;

	@Column(name = "searched_at", nullable = false)
	private LocalDateTime searchedAt;

	SearchKeyword() {
	}

	SearchKeyword(String keyword) {
		this.keyword = keyword;
		this.searchedAt = LocalDateTime.now();
	}

	String getKeyword() {
		return keyword;
	}

	LocalDateTime getSearchedAt() {
		return searchedAt;
	}

}
