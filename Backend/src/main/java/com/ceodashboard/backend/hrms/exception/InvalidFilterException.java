package com.ceodashboard.backend.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when invalid filter parameters are provided to workforce
 * health APIs
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFilterException extends RuntimeException {

    public InvalidFilterException(String message) {
        super(message);
    }

    public InvalidFilterException(String message, Throwable cause) {
        super(message, cause);
    }
}
