package com.ceodashboard.backend.hrms.service;

import com.ceodashboard.backend.hrms.dto.EmployeeProfileDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeProfileDTO getEmployeeById(Long id);
    Page<EmployeeProfileDTO> getAllEmployees(Pageable pageable);
    EmployeeProfileDTO getMyProfile();
    Page<EmployeeProfileDTO> getMyTeam(Pageable pageable);
}
