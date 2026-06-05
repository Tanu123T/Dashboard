package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.AchievementDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class AchievementRowMapper implements RowMapper<AchievementDTO> {

    @Override
    public AchievementDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date dateSql = rs.getDate("achievement_date");
        return AchievementDTO.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("achievement_title"))
                .description(rs.getString("achievement_description"))
                .achievementDate(dateSql != null ? dateSql.toLocalDate() : null)
                .build();
    }
}
