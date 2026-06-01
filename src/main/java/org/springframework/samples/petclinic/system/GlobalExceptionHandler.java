package org.springframework.samples.petclinic.system;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that returns responses conforming to the EY DnD DevOps API
 * Response Guideline.
 */
@RestControllerAdvice(annotations = RestController.class)
class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	ApiResponse<Void> handleException(Exception ex) {
		ApiErrorDetail errorDetail = new ApiErrorDetail("INTERNAL_SERVER_ERROR", ex.getClass().getSimpleName(),
				"伺服器發生未預期的錯誤，請稍後再試", ex.getMessage(), UUID.randomUUID().toString());
		return ApiResponse.error(errorDetail);
	}

}
