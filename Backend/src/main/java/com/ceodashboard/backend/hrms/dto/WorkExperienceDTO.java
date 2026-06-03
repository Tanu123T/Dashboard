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
public class WorkExperienceDTO {
    private Long      id;
    private String    companyName;
    private String    jobTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String    description;
    private String    status;
}
