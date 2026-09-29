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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller exposing application information.
 */
@RestController
@RequestMapping("/api/app-info")
class AppInfoApiController {

	private static final DateTimeFormatter LAST_UPDATED_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
		.withZone(ZoneId.of("Asia/Taipei"));

	private final ObjectProvider<BuildProperties> buildProperties;

	AppInfoApiController(ObjectProvider<BuildProperties> buildProperties) {
		this.buildProperties = buildProperties;
	}

	@GetMapping
	ApiResponse<AppInfo> showAppInfo() {
		BuildProperties build = this.buildProperties.getIfAvailable();
		Instant buildTime = (build != null) ? build.getTime() : null;
		String lastUpdatedAt = (buildTime != null) ? LAST_UPDATED_FORMAT.format(buildTime) : null;
		return ApiResponse.success(new AppInfo(lastUpdatedAt));
	}

}
