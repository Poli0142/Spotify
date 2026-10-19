package com.music.streaming.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(NotFoundException e) {
        log.warn("404: {}", e.getMessage());
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> unavailable(ServiceUnavailableException e) {
        log.warn("503: {}", e.getMessage());
        return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("400: {}", message);
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> conflict(DataIntegrityViolationException e) {
        log.warn("409: {}", e.getMostSpecificCause().getMessage());
        return error(HttpStatus.CONFLICT, "Конфликт данных");
    }

    static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()));
    }
}
