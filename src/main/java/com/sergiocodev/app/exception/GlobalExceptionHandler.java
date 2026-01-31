package com.sergiocodev.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Handles UserNotFoundException
         */
        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUserNotFound(
                        UserNotFoundException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false).replace("uri=", ""));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        /**
         * Handles UserAlreadyExistsException
         */
        @ExceptionHandler(UserAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
                        UserAlreadyExistsException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false).replace("uri=", ""));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        /**
         * Handles BadCredentialsException
         */
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(
                        BadCredentialsException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Invalid username or password",
                                LocalDateTime.now(),
                                request.getDescription(false).replace("uri=", ""));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        /**
         * Handles CustomerNotFoundException
         */
        @ExceptionHandler(CustomerNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleCustomerNotFound(
                        CustomerNotFoundException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false).replace("uri=", ""));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        /**
         * Handles DuplicateDniException
         */
        @ExceptionHandler(DuplicateDniException.class)
        public ResponseEntity<ErrorResponse> handleDniDuplicated(
                        DuplicateDniException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false).replace("uri=", ""));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        /**
         * Handles BrandNotFoundException
         */
        @ExceptionHandler(BrandNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleBrandNotFound(
                        BrandNotFoundException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false).replace("uri=", ""));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        /**
         * Handles DuplicateBrandException
         */
        @ExceptionHandler(DuplicateBrandException.class)
        public ResponseEntity<ErrorResponse> handleBrandNameDuplicated(
                        DuplicateBrandException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false).replace("uri=", ""));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        /**
         * Handles CategoryNotFoundException
         */
        @ExceptionHandler(CategoryNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleCategoryNotFound(
                        CategoryNotFoundException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false).replace("uri=", ""));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        /**
         * Handles DuplicateCategoryException
         */
        @ExceptionHandler(DuplicateCategoryException.class)
        public ResponseEntity<ErrorResponse> handleCategoryNameDuplicated(
                        DuplicateCategoryException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false).replace("uri=", ""));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        /**
         * Handles validation errors (Bean Validation)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationErrors(
                        MethodArgumentNotValidException ex) {
                Map<String, Object> response = new HashMap<>();
                Map<String, String> errors = new HashMap<>();

                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String field = ((FieldError) error).getField();
                        String message = error.getDefaultMessage();
                        errors.put(field, message);
                });

                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("errors", errors);
                response.put("timestamp", LocalDateTime.now());

                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Handles general exceptions
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneralException(
                        Exception ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal server error: " + ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false).replace("uri=", ""));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
}
