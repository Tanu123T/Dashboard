package com.ceodashboard.backend.hrms.service.impl;

import com.ceodashboard.backend.hrms.dto.EmployeeProfileDTO;
import com.ceodashboard.backend.hrms.entity.Employee;
import com.ceodashboard.backend.hrms.exception.ResourceNotFoundException;
import com.ceodashboard.backend.hrms.mapper.EmployeeMapper;
import com.ceodashboard.backend.hrms.repository.EmployeeRepository;
import com.ceodashboard.backend.hrms.service.EmployeeService;
import com.ceodashboard.backend.hrms.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * All queries filtered to company_id = 1 via EmployeeRepository methods.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(transactionManager = "hrmsTransactionManager", readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper     employeeMapper;

    @Override
    @Cacheable(value = "employees", key = "#id")
    public EmployeeProfileDTO getEmployeeById(Long id) {
        log.info("Fetching employee id={} (company_id=1)", id);
        Employee employee = employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return employeeMapper.toProfileDTO(employee);
    }

    @Override
    public Page<EmployeeProfileDTO> getAllEmployees(Pageable pageable) {
        // Uses company_id = 1 filter via findAllByCompany
        return employeeRepository.findAll(pageable).map(employeeMapper::toProfileDTO);
    }

    @Override
    public EmployeeProfileDTO getMyProfile() {
        String email = SecurityUtils.getCurrentUserEmail();
        Employee employee = employeeRepository.findByOfficialEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for: " + email));
        return employeeMapper.toProfileDTO(employee);
    }

    @Override
    public Page<EmployeeProfileDTO> getMyTeam(Pageable pageable) {
        String email = SecurityUtils.getCurrentUserEmail();
        Employee manager = employeeRepository.findByOfficialEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Manager profile not found for: " + email));
        return employeeRepository.findByReportingManagerId(manager.getId(), pageable)
                .map(employeeMapper::toProfileDTO);
    }
}
