package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.CertificationDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class CertificationRowMapper implements RowMapper<CertificationDTO> {

    @Override
    public CertificationDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date dateSql = rs.getDate("certification_date");
        return CertificationDTO.builder()
                .id(rs.getLong("id"))
                .certificationName(rs.getString("certification_name"))
                .issuingOrganization(rs.getString("issuing_organization"))
                .certificationDate(dateSql != null ? dateSql.toLocalDate() : null)
                .build();
    }
}
