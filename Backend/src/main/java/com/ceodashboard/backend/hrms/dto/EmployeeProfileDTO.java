package com.ceodashboard.backend.hrms.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeProfileDTO {
    private Long id;
    private String employeeCode;
    private String fullName;
    private String officialEmail;
    private String profileImage;
    private String designation;
    private String department;
    private String workMode;
    private String employmentType;
    private String employeeStatus;
    private String reportingManagerName;
}
