package com.epam.interfaces.web.advice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.epam.application.exception.ValidationException;
import com.epam.infrastructure.logging.MdcConstants;
import com.epam.interfaces.web.dto.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String formattedErrors = formatValidationErrors(errors);
        logException(ex, request, "Errors: " + errors);

        ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", formattedErrors, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException ex, WebRequest request) {

        logException(ex, request, null);

        ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "Account is locked", ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

    @ExceptionHandler({ AuthenticationException.class, AuthorizationDeniedException.class })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex, WebRequest request) {

        logException(ex, request, null);

        ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler({ ValidationException.class, IllegalArgumentException.class })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception ex, WebRequest request) {

        logException(ex, request, null);

        String errorTitle = ex instanceof ValidationException ? "Validation Error" : "Invalid Request";

        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, errorTitle, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {

        logExceptionAsWarning(ex, request);

        ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
            MissingRequestHeaderException ex,
            WebRequest request) {

        logException(ex, request, null);

        ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {

        String transactionId = getTransactionId();
        String endpoint = getRequestURI(request);

        log
                .error(
                    "UnexpectedException | TransactionId: {} | Endpoint: {} | ExceptionType: {} | Message: {} | StackTrace:",
                    transactionId,
                    endpoint,
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex);

        String userMessage =
                "An unexpected error occurred. Please contact support with transaction ID: " + transactionId;

        ErrorResponse errorResponse =
                buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", userMessage, request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private void logException(Exception ex, WebRequest request, String additionalInfo) {
        String transactionId = getTransactionId();
        String endpoint = getRequestURI(request);
        String exceptionType = ex.getClass().getSimpleName();

        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            log
                    .error(
                        "{} | TransactionId: {} | Endpoint: {} | Message: {} | {}",
                        exceptionType,
                        transactionId,
                        endpoint,
                        ex.getMessage(),
                        additionalInfo);
        }
        else {
            log
                    .error(
                        "{} | TransactionId: {} | Endpoint: {} | Message: {}",
                        exceptionType,
                        transactionId,
                        endpoint,
                        ex.getMessage());
        }
    }

    private void logExceptionAsWarning(Exception ex, WebRequest request) {
        String transactionId = getTransactionId();
        String endpoint = getRequestURI(request);
        String exceptionType = ex.getClass().getSimpleName();

        log
                .warn(
                    "{} | TransactionId: {} | Endpoint: {} | Message: {}",
                    exceptionType,
                    transactionId,
                    endpoint,
                    ex.getMessage());
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String error, String message, WebRequest request) {

        return new ErrorResponse(LocalDateTime
                .now(), status.value(), error, message, getRequestURI(request), getTransactionId());
    }

    private String getTransactionId() {
        return MDC.get(MdcConstants.TRANSACTION_ID_MDC_KEY);
    }

    private String getRequestURI(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return request.getDescription(false).replace("uri=", "");
    }

    private String formatValidationErrors(Map<String, String> errors) {
        return errors
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

}
