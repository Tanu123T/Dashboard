package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;

@Entity
@Table(name = "attendance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Column(name = "date")
    private LocalDate attendanceDate;
    
    @Column(name = "has_checked_in")
    private Boolean hasCheckedIn;
    
    @Column(name = "hours")
    private String workHours;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(name = "last_modified")
    private Instant updatedAt;
    
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}
