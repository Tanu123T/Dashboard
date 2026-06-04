package com.ceodashboard.backend.hrms.exception;

public class UnauthorizedException extends HrmsApiException {
    public UnauthorizedException(String message) {
        super(message, 401, "UNAUTHORIZED");
    }
}
