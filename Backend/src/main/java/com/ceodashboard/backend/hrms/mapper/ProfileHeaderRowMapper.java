package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.ProfileHeaderDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ProfileHeaderRowMapper implements RowMapper<ProfileHeaderDTO> {

    @Override
    public ProfileHeaderDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ProfileHeaderDTO.builder()
                .employeeId(rs.getLong("employee_id"))
                .fullName(rs.getString("full_name"))
                .designation(rs.getString("designation"))
                .department(rs.getString("department"))
                .profileImage(rs.getString("profile_image"))
                .officialEmail(rs.getString("official_email"))
                .build();
    }
}
