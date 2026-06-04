package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "employee_leave_account")
@SQLRestriction("company_id = 1")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLeaveAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "balance")
    private Double balance;

    @Column(name = "carried_leaves")
    private Double carriedLeaves;

    @Column(name = "credited_leaves")
    private Double creditedLeaves;

    @Column(name = "date")
    private Instant date;

    @Column(name = "status")
    private String status;

    @Column(name = "leave_type_id")
    private Long leaveTypeId;

    @Column(name = "lop")
    private Double lop;

    @Column(name = "leave_policy_id")
    private Long leavePolicyId;

    @Column(name = "carry_forward_balance")
    private Double carryForwardBalance;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "last_modified")
    private Instant updatedAt;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}
