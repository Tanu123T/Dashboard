package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.EmployeeHubItemDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EmployeeHubItemRowMapper implements RowMapper<EmployeeHubItemDTO> {
    @Override
    public EmployeeHubItemDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return EmployeeHubItemDTO.builder()
                .id(rs.getLong("id"))
                .employeeCode(rs.getString("employee_code"))
                .fullName(rs.getString("full_name"))
                .designation(rs.getString("designation"))
                .department(rs.getString("department"))
                .branchName(rs.getString("branch_name"))
                .regionName(rs.getString("region_name"))
                .status(rs.getString("status"))
                .profileImage(rs.getString("profile_image"))
                .experienceYears(rs.getInt("experience_years"))
                .build();
    }
}
