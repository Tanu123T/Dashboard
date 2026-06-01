package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.PerformanceTrendDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class PerformanceTrendRowMapper implements RowMapper<PerformanceTrendDTO> {

    @Override
    public PerformanceTrendDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PerformanceTrendDTO.builder()
                .month(rs.getString("month"))
                .score(rs.getInt("score"))
                .build();
    }
}
