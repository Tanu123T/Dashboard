package com.ceodashboard.backend.hrms.exception;

public class ForbiddenException extends HrmsApiException {
    public ForbiddenException(String message) {
        super(message, 403, "FORBIDDEN");
    }
}
