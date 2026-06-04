package com.ceodashboard.backend.hrms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalInfoDTO {
    private String    officialEmail;
    private String    phoneNumber;
    private String    branch;
    private String    region;
    private LocalDate joinDate;
    private Integer   experienceYears;
    private Integer   experienceMonths;
    private String    totalExperience;          // formatted e.g. "3y 2m"
    private String    reportingManagerName;      // from employee JOIN on reporting_emp_id
}
