package org.springframework.samples.petclinic.system;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the error detail object in a standard API error response.
 */
public record ApiErrorDetail(String code, String type, String message, String detail,
		@JsonProperty("trace_id") String traceId) {
}
