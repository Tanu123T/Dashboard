package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "region")
@SQLRestriction("company_id = 1")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region_name")
    private String regionName;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "last_modified")
    private java.time.Instant updatedAt;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}
