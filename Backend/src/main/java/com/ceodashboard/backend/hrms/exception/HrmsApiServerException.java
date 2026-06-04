package com.ceodashboard.backend.hrms.exception;

public class HrmsApiServerException extends HrmsApiException {
    public HrmsApiServerException(String message, int statusCode) {
        super(message, statusCode, "HRMS_API_SERVER_ERROR");
    }
}
