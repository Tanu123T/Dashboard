package com.ceodashboard.backend.hrms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there's an error retrieving or processing workforce
 * health data
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class WorkforceHealthDataException extends RuntimeException {

    public WorkforceHealthDataException(String message) {
        super(message);
    }

    public WorkforceHealthDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
