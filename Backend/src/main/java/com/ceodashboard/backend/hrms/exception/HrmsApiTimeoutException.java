package com.ceodashboard.backend.hrms.exception;

public class HrmsApiTimeoutException extends HrmsApiException {
    public HrmsApiTimeoutException(String endpoint) {
        super("Request to HRMS API endpoint " + endpoint + " timed out", 504, "REQUEST_TIMEOUT");
    }
}
