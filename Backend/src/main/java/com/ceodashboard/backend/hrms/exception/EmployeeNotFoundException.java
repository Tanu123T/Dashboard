package com.ceodashboard.backend.hrms.exception;

public class EmployeeNotFoundException extends HrmsApiException {
    public EmployeeNotFoundException(String empId) {
        super("Employee not found with ID: " + empId, 404, "EMPLOYEE_NOT_FOUND");
    }
}
