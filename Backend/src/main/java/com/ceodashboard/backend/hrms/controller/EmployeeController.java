package com.ceodashboard.backend.hrms.controller;

import com.ceodashboard.backend.hrms.dto.ApiResponse;
import com.ceodashboard.backend.hrms.dto.EmployeeProfileDTO;
import com.ceodashboard.backend.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hrms/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CEO', 'ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<Page<EmployeeProfileDTO>>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CEO', 'ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeProfileDTO>> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeById(id)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<EmployeeProfileDTO>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getMyProfile()));
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Page<EmployeeProfileDTO>>> getMyTeam(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getMyTeam(pageable)));
    }
}
