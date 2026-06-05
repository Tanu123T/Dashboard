package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.EmployeeProjectDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmployeeProjectRowMapper implements RowMapper<EmployeeProjectDTO> {

    @Override
    public EmployeeProjectDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date startSql = rs.getDate("start_date");
        Date endSql   = rs.getDate("end_date");

        String techCsv = rs.getString("technologies");
        List<String> technologies = (techCsv != null && !techCsv.isBlank())
                ? Arrays.stream(techCsv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return EmployeeProjectDTO.builder()
                .id(rs.getLong("id"))
                .projectName(rs.getString("project_name"))
                .projectStatus(rs.getString("project_status"))
                .projectDescription(rs.getString("project_description"))
                .startDate(startSql != null ? startSql.toLocalDate() : null)
                .endDate(endSql   != null ? endSql.toLocalDate()   : null)
                .technologies(technologies)
                .build();
    }
}
