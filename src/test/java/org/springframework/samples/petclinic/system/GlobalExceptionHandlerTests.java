package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

	@Test
	void should_return404_when_illegalArgumentExceptionOccurs() {
		IllegalArgumentException ex = new IllegalArgumentException("Owner not found with id: 99");

		ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgument(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().success()).isFalse();
		assertThat(response.getBody().error().code()).isEqualTo("RESOURCE_NOT_FOUND");
		assertThat(response.getBody().error().detail()).isEqualTo("Owner not found with id: 99");
	}

	@Test
	void should_return400_when_validationFails() {
		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		when(ex.getMessage()).thenReturn("Validation failed");

		ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().success()).isFalse();
		assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_ERROR");
	}

}
