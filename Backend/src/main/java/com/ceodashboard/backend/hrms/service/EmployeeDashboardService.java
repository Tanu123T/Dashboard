package com.ceodashboard.backend.hrms.service;

import com.ceodashboard.backend.hrms.dto.EmployeeDashboardResponse;

public interface EmployeeDashboardService {

    /**
     * Assembles the complete employee dashboard response for the given employee ID.
     * Each section is fetched independently via JDBC so a missing table/row in one
     * section never fails the entire response.
     *
     * @param employeeId primary key of the employee row
     * @return fully-populated dashboard DTO
     */
    EmployeeDashboardResponse getDashboard(Long employeeId);
}
