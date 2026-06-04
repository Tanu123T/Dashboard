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
public class LeaveApplicationDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("empId")
    private String empId;

    @JsonProperty("leaveTypeId")
    private Long leaveTypeId;

    @JsonProperty("fromDate")
    private LocalDate fromDate;

    @JsonProperty("toDate")
    private LocalDate toDate;

    @JsonProperty("numberOfDays")
    private Double numberOfDays;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("status")
    private String status;

    @JsonProperty("approvedBy")
    private String approvedBy;

    @JsonProperty("approvalDate")
    private LocalDate approvalDate;
}
