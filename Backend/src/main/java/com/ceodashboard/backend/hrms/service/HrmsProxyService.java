package com.ceodashboard.backend.hrms.service;

import com.ceodashboard.backend.hrms.dto.*;

import java.util.List;

public interface HrmsProxyService {

    List<EmployeeDTO> getAllEmployees(Integer companyId);
    EmployeeDTO getEmployeeById(String empId);
    EmployeeDTO getEmployeeProfile(String empId);
    List<EmployeeDTO> searchEmployees(String query, Integer companyId);
    List<AttendanceDTO> getAttendanceByEmployeeId(String empId);
    List<EducationDTO> getEducationByEmployeeId(String empId);

    List<AddressDTO> getAllAddresses(Integer companyId);
    List<EducationDTO> getAllEducations(Integer companyId);
    List<BranchDTO> getAllBranches(Integer companyId);
    List<RegionDTO> getAllRegions(Integer companyId);
    List<AttendanceDTO> getAllAttendances(Integer companyId);
    List<TimesheetDTO> getAllTimesheets(Integer companyId);
    List<LeaveApplicationDTO> getAllLeaveApplications(Integer companyId);
    List<EmployeeLeaveAccountDTO> getAllEmployeeLeaveAccounts(Integer companyId);
    List<HolidayDTO> getAllHolidays(Integer companyId);
    List<WorkingHoursDTO> getAllWorkingHours(Integer companyId);
}
