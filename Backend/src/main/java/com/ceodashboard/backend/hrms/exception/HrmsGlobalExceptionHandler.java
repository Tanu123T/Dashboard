package com.ceodashboard.backend.hrms.exception;

import com.ceodashboard.backend.hrms.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ceodashboard.backend.hrms.controller")
@Slf4j
public class HrmsGlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage(), "Not Found"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage(), "Bad Request"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "Access denied. Insufficient privileges.", "Forbidden"));
    }

    @ExceptionHandler(InvalidFilterException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidFilterException(InvalidFilterException ex) {
        log.error("Invalid filter parameter: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage(), "Invalid Filter"));
    }

    @ExceptionHandler(WorkforceHealthDataException.class)
    public ResponseEntity<ApiResponse<Object>> handleWorkforceHealthDataException(WorkforceHealthDataException ex) {
        log.error("Workforce health data error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, ex.getMessage(), "Workforce Health Data Error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        log.error("Internal server error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, ex.getClass().getSimpleName() + ": " + ex.getMessage(), "Internal Server Error"));
    }

    @ExceptionHandler(com.ceodashboard.backend.hrms.exception.EmployeeNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmployeeNotFound(com.ceodashboard.backend.hrms.exception.EmployeeNotFoundException ex) {
        log.warn("Employee not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(com.ceodashboard.backend.hrms.exception.UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(com.ceodashboard.backend.hrms.exception.UnauthorizedException ex) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(com.ceodashboard.backend.hrms.exception.ForbiddenException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbidden(com.ceodashboard.backend.hrms.exception.ForbiddenException ex) {
        log.warn("Forbidden access: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(com.ceodashboard.backend.hrms.exception.HrmsApiServerException.class)
    public ResponseEntity<ApiResponse<Object>> handleHrmsServerError(com.ceodashboard.backend.hrms.exception.HrmsApiServerException ex) {
        log.error("HRMS API server error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getStatusCode(), ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(com.ceodashboard.backend.hrms.exception.HrmsApiTimeoutException.class)
    public ResponseEntity<ApiResponse<Object>> handleHrmsTimeout(com.ceodashboard.backend.hrms.exception.HrmsApiTimeoutException ex) {
        log.error("HRMS API timeout: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ApiResponse.error(504, ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(com.ceodashboard.backend.hrms.exception.HrmsApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleHrmsApiException(com.ceodashboard.backend.hrms.exception.HrmsApiException ex) {
        log.error("HRMS API error: {}", ex.getMessage());
        int status = ex.getStatusCode() <= 0 ? 500 : ex.getStatusCode();
        return ResponseEntity.status(HttpStatus.valueOf(status))
                .body(ApiResponse.error(status, ex.getMessage(), ex.getErrorCode()));
    }
}
