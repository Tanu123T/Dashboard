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
public class AttendanceDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("day")
    private String day;

    @JsonProperty("hours")
    private Double hours;

    @JsonProperty("status")
    private String status;

    @JsonProperty("hasCheckedIn")
    private Boolean hasCheckedIn;

    @JsonProperty("checkInTime")
    private String checkInTime;

    @JsonProperty("checkOutTime")
    private String checkOutTime;

    @JsonProperty("empId")
    private String empId;

    @JsonProperty("companyId")
    private Integer companyId;
}
