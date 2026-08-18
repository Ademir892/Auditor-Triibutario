package br.com.auditortributario.api.error;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public final class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(
                        fieldError -> new ApiFieldError(
                                fieldError.getField(),
                                fieldError
                                        .getDefaultMessage()))
                .sorted(
                        Comparator.comparing(
                                ApiFieldError::field))
                .toList();

        ApiErrorResponse response = createResponse(
                HttpStatus.BAD_REQUEST,
                "Dados da requisição inválidos.",
                request,
                fieldErrors);

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        ApiErrorResponse response = createResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                List.of());

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        ApiErrorResponse response = createResponse(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente "
                        + "ou possui formato inválido.",
                request,
                List.of());

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    private ApiErrorResponse createResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<ApiFieldError> fieldErrors) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors);
    }
}