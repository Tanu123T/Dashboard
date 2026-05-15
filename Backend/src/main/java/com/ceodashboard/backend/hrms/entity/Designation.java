package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "designation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Designation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String title;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "department_id")
    private Long departmentId;
    
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(name = "last_modified")
    private Instant updatedAt;
    
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}
