package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.WorkExperienceDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class WorkExperienceRowMapper implements RowMapper<WorkExperienceDTO> {

    @Override
    public WorkExperienceDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date startSql = rs.getDate("start_date");
        Date endSql   = rs.getDate("end_date");
        return WorkExperienceDTO.builder()
                .id(rs.getLong("id"))
                .companyName(rs.getString("company_name"))
                .jobTitle(rs.getString("job_title"))
                .startDate(startSql != null ? startSql.toLocalDate() : null)
                .endDate(endSql     != null ? endSql.toLocalDate()   : null)
                .description(rs.getString("description"))
                .status(rs.getString("status"))
                .build();
    }
}
