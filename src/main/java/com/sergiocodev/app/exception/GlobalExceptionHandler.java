package com.sergiocodev.app.exception;

import com.sergiocodev.app.dto.ResponseApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Handles UserNotFoundException
         */
        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ResponseApi<Object>> handleUserNotFound(
                        UserNotFoundException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ResponseApi.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
        }

        /**
         * Handles UserAlreadyExistsException
         */
        @ExceptionHandler(UserAlreadyExistsException.class)
        public ResponseEntity<ResponseApi<Object>> handleUserAlreadyExists(
                        UserAlreadyExistsException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ResponseApi.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
        }

        /**
         * Handles BadCredentialsException
         */
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ResponseApi<Object>> handleBadCredentials(
                        BadCredentialsException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ResponseApi.error(HttpStatus.UNAUTHORIZED.value(),
                                                "Invalid username or password"));
        }

        /**
         * Handles CustomerNotFoundException
         */
        @ExceptionHandler(CustomerNotFoundException.class)
        public ResponseEntity<ResponseApi<Object>> handleCustomerNotFound(
                        CustomerNotFoundException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ResponseApi.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
        }

        /**
         * Handles DuplicateDniException
         */
        @ExceptionHandler(DuplicateDniException.class)
        public ResponseEntity<ResponseApi<Object>> handleDniDuplicated(
                        DuplicateDniException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ResponseApi.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
        }

        /**
         * Handles BrandNotFoundException
         */
        @ExceptionHandler(BrandNotFoundException.class)
        public ResponseEntity<ResponseApi<Object>> handleBrandNotFound(
                        BrandNotFoundException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ResponseApi.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
        }

        /**
         * Handles DuplicateBrandException
         */
        @ExceptionHandler(DuplicateBrandException.class)
        public ResponseEntity<ResponseApi<Object>> handleBrandNameDuplicated(
                        DuplicateBrandException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ResponseApi.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
        }

        /**
         * Handles CategoryNotFoundException
         */
        @ExceptionHandler(CategoryNotFoundException.class)
        public ResponseEntity<ResponseApi<Object>> handleCategoryNotFound(
                        CategoryNotFoundException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ResponseApi.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
        }

        /**
         * Handles DuplicateCategoryException
         */
        @ExceptionHandler(DuplicateCategoryException.class)
        public ResponseEntity<ResponseApi<Object>> handleCategoryNameDuplicated(
                        DuplicateCategoryException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ResponseApi.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
        }

        /**
         * Handles PharmaceuticalFormNotFoundException
         */
        @ExceptionHandler(PharmaceuticalFormNotFoundException.class)
        public ResponseEntity<ResponseApi<Object>> handlePharmaceuticalFormNotFound(
                        PharmaceuticalFormNotFoundException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ResponseApi.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
        }

        /**
         * Handles validation errors (Bean Validation)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ResponseApi<Object>> handleValidationErrors(
                        MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();

                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String field = ((FieldError) error).getField();
                        String message = error.getDefaultMessage();
                        errors.put(field, message);
                });

                ResponseApi<Object> response = ResponseApi.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Validation failed")
                                .data(errors)
                                .build();

                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Handles StockInsufficientException
         */
        @ExceptionHandler(StockInsufficientException.class)
        public ResponseEntity<ResponseApi<Object>> handleStockInsufficient(
                        StockInsufficientException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ResponseApi.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        }

        /**
         * Handles ResourceNotFoundException
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ResponseApi<Object>> handleResourceNotFound(
                        ResourceNotFoundException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ResponseApi.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
        }

        /**
         * Handles BadRequestException
         */
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ResponseApi<Object>> handleBadRequest(
                        BadRequestException ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ResponseApi.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
        }

        /**
         * Handles general exceptions
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ResponseApi<Object>> handleGeneralException(
                        Exception ex, WebRequest request) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ResponseApi.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "Internal server error: " + ex.getMessage()));
        }
}
