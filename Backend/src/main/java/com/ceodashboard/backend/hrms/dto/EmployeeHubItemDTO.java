package com.ceodashboard.backend.hrms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeHubItemDTO {
    private Long    id;
    private String  employeeCode;
    private String  fullName;
    private String  designation;
    private String  department;
    private String  branchName;
    private String  regionName;
    private String  status;
    private String  profileImage;
    private Integer experienceYears;
}
