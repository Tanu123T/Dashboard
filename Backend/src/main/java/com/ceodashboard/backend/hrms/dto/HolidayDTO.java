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
public class HolidayDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("holidayName")
    private String holidayName;

    @JsonProperty("holidayDate")
    private LocalDate holidayDate;

    @JsonProperty("description")
    private String description;

    @JsonProperty("companyId")
    private Integer companyId;
}
