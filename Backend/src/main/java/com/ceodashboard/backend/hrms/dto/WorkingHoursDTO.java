package com.ceodashboard.backend.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHoursDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("noOfHours")
    private Double noOfHours;

    @JsonProperty("dayOfWeek")
    private String dayOfWeek;

    @JsonProperty("branchId")
    private Long branchId;

    @JsonProperty("companyId")
    private Integer companyId;
}
