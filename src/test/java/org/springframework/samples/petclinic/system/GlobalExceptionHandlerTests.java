package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 */
class GlobalExceptionHandlerTests {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void should_returnErrorResponse_when_exceptionOccurs() {
		RuntimeException ex = new RuntimeException("something went wrong");

		ApiResponse<Void> response = handler.handleException(ex);

		assertThat(response.success()).isFalse();
		assertThat(response.data()).isNull();
		assertThat(response.error()).isNotNull();
		assertThat(response.error().code()).isEqualTo("INTERNAL_SERVER_ERROR");
		assertThat(response.error().type()).isEqualTo("RuntimeException");
		assertThat(response.error().message()).isNotEmpty();
		assertThat(response.error().detail()).isEqualTo("something went wrong");
		assertThat(response.error().traceId()).isNotEmpty();
		assertThat(response.timestamp()).isNotNull();
	}

	@Test
	void should_useClassNameAsDetail_when_exceptionMessageIsNull() {
		NullPointerException ex = new NullPointerException();

		ApiResponse<Void> response = handler.handleException(ex);

		assertThat(response.error().detail()).isEqualTo(NullPointerException.class.getName());
	}

}
