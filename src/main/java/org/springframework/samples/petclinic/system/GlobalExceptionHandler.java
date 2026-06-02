package org.springframework.samples.petclinic.system;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
		String detail = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
		ApiErrorDetail errorDetail = new ApiErrorDetail("RESOURCE_NOT_FOUND", ex.getClass().getSimpleName(), "找不到指定的資源",
				detail, UUID.randomUUID().toString());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(errorDetail));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
		String detail = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
		ApiErrorDetail errorDetail = new ApiErrorDetail("VALIDATION_ERROR", ex.getClass().getSimpleName(), "請求資料驗證失敗",
				detail, UUID.randomUUID().toString());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorDetail));
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	ApiResponse<Void> handleException(Exception ex) {
		String detail = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
		ApiErrorDetail errorDetail = new ApiErrorDetail("INTERNAL_SERVER_ERROR", ex.getClass().getSimpleName(),
				"伺服器發生未預期的錯誤，請稍後再試", detail, UUID.randomUUID().toString());
		return ApiResponse.error(errorDetail);
	}

}
