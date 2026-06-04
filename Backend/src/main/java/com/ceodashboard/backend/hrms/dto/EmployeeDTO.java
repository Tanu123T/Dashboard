package com.ceodashboard.backend.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    @JsonProperty("empUniqueId")
    private String empUniqueId;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("middleName")
    private String middleName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("fullName")
    private String fullName;

    @JsonProperty("emailId")
    private String emailId;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("status")
    private String status;

    @JsonProperty("joindate")
    private LocalDate joinDate;

    @JsonProperty("employmentDate")
    private LocalDate employmentDate;

    @JsonProperty("confirmationDate")
    private LocalDate confirmationDate;

    @JsonProperty("retirementDate")
    private LocalDate retirementDate;

    @JsonProperty("reportingEmpId")
    private String reportingEmpId;

    @JsonProperty("reportingManagerName")
    private String reportingManagerName;

    @JsonProperty("reportingManagerEmail")
    private String reportingManagerEmail;

    @JsonProperty("experienceYears")
    private Double experienceYears;

    @JsonProperty("department")
    private String department;

    @JsonProperty("designation")
    private String designation;

    @JsonProperty("mobileNo")
    private String mobileNo;

    @JsonProperty("personalEmail")
    private String personalEmail;

    @JsonProperty("dateOfBirth")
    private LocalDate dateOfBirth;

    @JsonProperty("companyId")
    private Integer companyId;
}
