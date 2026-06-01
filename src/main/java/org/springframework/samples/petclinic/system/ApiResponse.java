package org.springframework.samples.petclinic.system;

import java.time.OffsetDateTime;

/**
 * Standard API response wrapper following EY DnD DevOps API Response Guideline.
 *
 * @param <T> type of the response data
 */
public record ApiResponse<T>(boolean success, T data, ApiErrorDetail error, OffsetDateTime timestamp) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null, OffsetDateTime.now());
	}

	public static <T> ApiResponse<T> error(ApiErrorDetail errorDetail) {
		return new ApiResponse<>(false, null, errorDetail, OffsetDateTime.now());
	}

}
