package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.SkillDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class SkillRowMapper implements RowMapper<SkillDTO> {

    @Override
    public SkillDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return SkillDTO.builder()
                .id(rs.getLong("id"))
                .skillName(rs.getString("skill_name"))
                .skillLevel(rs.getString("skill_level"))
                .build();
    }
}
