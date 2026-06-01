package org.springframework.samples.petclinic.system;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ApiResponse}.
 */
class ApiResponseTest {

	@Test
	void should_returnSuccessResponse_when_dataProvided() {
		String data = "hello";

		ApiResponse<String> response = ApiResponse.success(data);

		assertThat(response.success()).isTrue();
		assertThat(response.data()).isEqualTo("hello");
		assertThat(response.error()).isNull();
		assertThat(response.timestamp()).isNotNull();
		assertThat(response.timestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
	}

	@Test
	void should_returnSuccessResponseWithNullData_when_nullProvided() {
		ApiResponse<Void> response = ApiResponse.success(null);

		assertThat(response.success()).isTrue();
		assertThat(response.data()).isNull();
		assertThat(response.error()).isNull();
	}

	@Test
	void should_returnErrorResponse_when_errorDetailProvided() {
		ApiErrorDetail errorDetail = new ApiErrorDetail("VET_NOT_FOUND", "ResourceNotFound", "找不到獸醫資料",
				"Vet with id=99 does not exist", "trace-123");

		ApiResponse<Void> response = ApiResponse.error(errorDetail);

		assertThat(response.success()).isFalse();
		assertThat(response.data()).isNull();
		assertThat(response.error()).isNotNull();
		assertThat(response.error().code()).isEqualTo("VET_NOT_FOUND");
		assertThat(response.error().type()).isEqualTo("ResourceNotFound");
		assertThat(response.error().message()).isEqualTo("找不到獸醫資料");
		assertThat(response.error().detail()).isEqualTo("Vet with id=99 does not exist");
		assertThat(response.error().traceId()).isEqualTo("trace-123");
		assertThat(response.timestamp()).isNotNull();
	}

}
