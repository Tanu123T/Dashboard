package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.EmployeeProfileDTO;
import com.ceodashboard.backend.hrms.entity.Employee;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EmployeeMapper {

    public EmployeeProfileDTO toProfileDTO(Employee employee) {
        if (employee == null) {
            return null;
        }
        
        String fullName = buildFullName(employee.getFirstName(), employee.getLastName());
        
        String managerName = null;
        if (employee.getReportingManager() != null) {
            managerName = buildFullName(employee.getReportingManager().getFirstName(), employee.getReportingManager().getLastName());
        }
        
        return EmployeeProfileDTO.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(fullName)
                .officialEmail(employee.getOfficialEmail())
                .designation(employee.getDesignation() != null ? employee.getDesignation().getTitle() : null)
                .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .employmentType(employee.getEmploymentType() != null ? String.valueOf(employee.getEmploymentType()) : null)
                .employeeStatus(employee.getEmployeeStatus())
                .reportingManagerName(managerName)
                .build();
    }

    private String buildFullName(String firstName, String lastName) {
        StringBuilder nameBuilder = new StringBuilder();
        if (StringUtils.hasText(firstName)) {
            nameBuilder.append(firstName.trim());
        }
        if (StringUtils.hasText(lastName)) {
            if (!nameBuilder.isEmpty()) {
                nameBuilder.append(" ");
            }
            nameBuilder.append(lastName.trim());
        }
        return nameBuilder.length() > 0 ? nameBuilder.toString() : "Unknown";
    }
}
