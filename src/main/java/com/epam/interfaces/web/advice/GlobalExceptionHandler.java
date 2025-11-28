package com.epam.interfaces.web.advice;

import com.epam.application.exception.AuthenticationException;
import com.epam.application.exception.ValidationException;
import com.epam.infrastructure.logging.MdcConstants;
import com.epam.interfaces.web.dto.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
			WebRequest request) {

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		String transactionId = MDC.get(MdcConstants.TRANSACTION_ID_MDC_KEY);
		String endpoint = getRequestURI(request);

		log.error("ValidationException | TransactionId: {} | Endpoint: {} | Errors: {}", transactionId, endpoint,
				errors);

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				"Validation Failed", formatValidationErrors(errors), endpoint, transactionId);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {

		String transactionId = MDC.get(MdcConstants.TRANSACTION_ID_MDC_KEY);
		String endpoint = getRequestURI(request);

		log.error("AuthenticationException | TransactionId: {} | Endpoint: {} | Message: {}", transactionId, endpoint,
				ex.getMessage());

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(),
				"Authentication Failed", ex.getMessage(), endpoint, transactionId);

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {

		String transactionId = MDC.get(MdcConstants.TRANSACTION_ID_MDC_KEY);
		String endpoint = getRequestURI(request);

		log.error("ValidationException | TransactionId: {} | Endpoint: {} | Message: {}", transactionId, endpoint,
				ex.getMessage());

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				"Validation Error", ex.getMessage(), endpoint, transactionId);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {

		String transactionId = MDC.get(MdcConstants.TRANSACTION_ID_MDC_KEY);
		String endpoint = getRequestURI(request);

		log.warn("EntityNotFoundException | TransactionId: {} | Endpoint: {} | Message: {}", transactionId, endpoint,
				ex.getMessage());

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
				"Resource Not Found", ex.getMessage(), endpoint, transactionId);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
			WebRequest request) {

		String transactionId = MDC.get(MdcConstants.TRANSACTION_ID_MDC_KEY);
		String endpoint = getRequestURI(request);

		log.error("IllegalArgumentException | TransactionId: {} | Endpoint: {} | Message: {}", transactionId, endpoint,
				ex.getMessage());

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				"Invalid Request", ex.getMessage(), endpoint, transactionId);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex,
			WebRequest request) {

		String transactionId = MDC.get(MdcConstants.TRANSACTION_ID_MDC_KEY);
		String endpoint = getRequestURI(request);

		log.error("MissingHeaderException | TransactionId: {} | Endpoint: {} | Message: {}", transactionId, endpoint,
				ex.getMessage());

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				"Invalid Request", ex.getMessage(), endpoint, transactionId);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {

		String transactionId = MDC.get(MdcConstants.TRANSACTION_ID_MDC_KEY);
		String endpoint = getRequestURI(request);

		log.error(
				"UnexpectedException | TransactionId: {} | Endpoint: {} | ExceptionType: {} | Message: {} | StackTrace:",
				transactionId, endpoint, ex.getClass().getSimpleName(), ex.getMessage(), ex);

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error",
				"An unexpected error occurred. Please contact support with transaction ID: " + transactionId, endpoint,
				transactionId);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

	// Helper methods

	private String getRequestURI(WebRequest request) {
		if (request instanceof ServletWebRequest) {
			return ((ServletWebRequest) request).getRequest().getRequestURI();
		}
		return request.getDescription(false).replace("uri=", "");
	}

	private String formatValidationErrors(Map<String, String> errors) {
		return errors.entrySet()
			.stream()
			.map(entry -> entry.getKey() + ": " + entry.getValue())
			.collect(Collectors.joining(", "));
	}

}