package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.PersonalInfoDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class PersonalInfoRowMapper implements RowMapper<PersonalInfoDTO> {

    @Override
    public PersonalInfoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        int expYears  = rs.getInt("experience_years");
        int expMonths = rs.getInt("experience_months");
        String formatted = expYears + "y " + expMonths + "m";

        Date joinSql = rs.getDate("join_date");

        return PersonalInfoDTO.builder()
                .officialEmail(rs.getString("official_email"))
                .phoneNumber(rs.getString("phone_number"))
                .branch(rs.getString("branch"))
                .region(rs.getString("region"))
                .joinDate(joinSql != null ? joinSql.toLocalDate() : null)
                .experienceYears(expYears)
                .experienceMonths(expMonths)
                .totalExperience(formatted)
                .reportingManagerName(rs.getString("reporting_manager_name"))
                .build();
    }
}
