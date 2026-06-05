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
public class EmployeeLeaveAccountDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("empId")
    private String empId;

    @JsonProperty("leaveTypeId")
    private Long leaveTypeId;

    @JsonProperty("balance")
    private Double balance;

    @JsonProperty("creditedLeaves")
    private Double creditedLeaves;

    @JsonProperty("carriedLeaves")
    private Double carriedLeaves;

    @JsonProperty("carryForwardBalance")
    private Double carryForwardBalance;

    @JsonProperty("lop")
    private Double lop;

    @JsonProperty("utilizedLeaves")
    private Double utilizedLeaves;
}
