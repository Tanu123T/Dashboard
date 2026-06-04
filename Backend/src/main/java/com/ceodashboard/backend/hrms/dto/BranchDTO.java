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
public class BranchDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("branchName")
    private String branchName;

    @JsonProperty("branchCode")
    private String branchCode;

    @JsonProperty("city")
    private String city;

    @JsonProperty("state")
    private String state;

    @JsonProperty("country")
    private String country;

    @JsonProperty("regionId")
    private Long regionId;

    @JsonProperty("companyId")
    private Integer companyId;
}
