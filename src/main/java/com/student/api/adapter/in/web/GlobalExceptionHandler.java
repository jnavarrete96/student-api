package com.student.api.adapter.in.web;

import com.student.api.domain.exception.DuplicateStudentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateStudentException.class)
    public Mono<org.springframework.http.ResponseEntity<ErrorResponse>> handleDuplicateStudent(
            DuplicateStudentException ex, ServerWebExchange exchange) {

        log.warn("Duplicate student error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(org.springframework.http.ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<org.springframework.http.ResponseEntity<ErrorResponse>> handleValidationErrors(
            WebExchangeBindException ex, ServerWebExchange exchange) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        field -> field.getDefaultMessage() != null ? field.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        log.warn("Validation error on fields: {}", fieldErrors);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("One or more fields are invalid")
                .fieldErrors(fieldErrors)
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(org.springframework.http.ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<org.springframework.http.ResponseEntity<ErrorResponse>> handleGenericError(
            Exception ex, ServerWebExchange exchange) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(org.springframework.http.ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error));
    }
}