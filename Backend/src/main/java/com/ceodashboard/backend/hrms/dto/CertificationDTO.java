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
public class CertificationDTO {
    private Long      id;
    private String    certificationName;
    private String    issuingOrganization;
    private LocalDate certificationDate;
}
