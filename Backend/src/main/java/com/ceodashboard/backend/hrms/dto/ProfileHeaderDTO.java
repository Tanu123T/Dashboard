package com.ceodashboard.backend.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileHeaderDTO {
    private Long   employeeId;
    private String fullName;
    private String designation;
    private String department;
    private String profileImage;
    private String officialEmail;
}
