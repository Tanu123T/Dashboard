package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.EducationDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EducationRowMapper implements RowMapper<EducationDTO> {

    @Override
    public EducationDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return EducationDTO.builder()
                .id(rs.getLong("id"))
                .educationType(rs.getString("education_type"))
                .subject(rs.getString("subject"))
                .institution(rs.getString("institution"))
                .startYear(rs.getString("start_year"))
                .endDate(rs.getString("end_date"))
                .grade(rs.getString("grade"))
                .description(rs.getString("description"))
                .build();
    }
}
