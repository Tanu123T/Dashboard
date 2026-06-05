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
public class RegionDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("regionName")
    private String regionName;

    @JsonProperty("regionCode")
    private String regionCode;

    @JsonProperty("companyId")
    private Integer companyId;
}
