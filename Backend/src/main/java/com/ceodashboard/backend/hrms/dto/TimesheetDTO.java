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
public class TimesheetDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("empId")
    private String empId;

    @JsonProperty("projectId")
    private Long projectId;

    @JsonProperty("taskId")
    private Long taskId;

    @JsonProperty("hoursSpent")
    private Double hoursSpent;

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private String status;
}
