package com.ceodashboard.backend.hrms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeProjectDTO {
    private Long         id;
    private String       projectName;
    private String       projectStatus;
    private String       projectDescription;
    private LocalDate    startDate;
    private LocalDate    endDate;
    private List<String> technologies;
}
