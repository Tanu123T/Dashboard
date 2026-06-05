package com.ceodashboard.backend.hrms.service.impl;

import com.ceodashboard.backend.hrms.client.*;
import com.ceodashboard.backend.hrms.dto.*;
import com.ceodashboard.backend.hrms.service.HrmsProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class HrmsProxyServiceImpl implements HrmsProxyService {

    private static final Logger logger = LoggerFactory.getLogger(HrmsProxyServiceImpl.class);

    private final EmployeeClient employeeClient;
    private final AddressClient addressClient;
    private final EducationClient educationClient;
    private final BranchClient branchClient;
    private final RegionClient regionClient;
    private final AttendanceClient attendanceClient;
    private final TimesheetClient timesheetClient;
    private final LeaveApplicationClient leaveApplicationClient;
    private final EmployeeLeaveAccountClient leaveAccountClient;
    private final HolidayClient holidayClient;
    private final WorkingHoursClient workingHoursClient;

    public HrmsProxyServiceImpl(
            EmployeeClient employeeClient,
            AddressClient addressClient,
            EducationClient educationClient,
            BranchClient branchClient,
            RegionClient regionClient,
            AttendanceClient attendanceClient,
            TimesheetClient timesheetClient,
            LeaveApplicationClient leaveApplicationClient,
            EmployeeLeaveAccountClient leaveAccountClient,
            HolidayClient holidayClient,
            WorkingHoursClient workingHoursClient) {
        this.employeeClient = employeeClient;
        this.addressClient = addressClient;
        this.educationClient = educationClient;
        this.branchClient = branchClient;
        this.regionClient = regionClient;
        this.attendanceClient = attendanceClient;
        this.timesheetClient = timesheetClient;
        this.leaveApplicationClient = leaveApplicationClient;
        this.leaveAccountClient = leaveAccountClient;
        this.holidayClient = holidayClient;
        this.workingHoursClient = workingHoursClient;
    }

    @Override
    public List<EmployeeDTO> getAllEmployees(Integer companyId) {
        try {
            return employeeClient.getAllEmployees(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch employees", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public EmployeeDTO getEmployeeById(String empId) {
        return employeeClient.getEmployeeById(empId);
    }

    @Override
    public EmployeeDTO getEmployeeProfile(String empId) {
        return employeeClient.getEmployeeProfile(empId);
    }

    @Override
    public List<EmployeeDTO> searchEmployees(String query, Integer companyId) {
        try {
            return employeeClient.searchEmployees(query, companyId);
        } catch (Exception ex) {
            logger.error("Failed to search employees", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<AttendanceDTO> getAttendanceByEmployeeId(String empId) {
        try {
            return attendanceClient.getAttendanceByEmployeeId(empId);
        } catch (Exception ex) {
            logger.error("Failed to fetch attendance for employee: " + empId, ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<EducationDTO> getEducationByEmployeeId(String empId) {
        try {
            return educationClient.getEducationByEmployeeId(empId);
        } catch (Exception ex) {
            logger.error("Failed to fetch education for employee: " + empId, ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<AddressDTO> getAllAddresses(Integer companyId) {
        try {
            return addressClient.getAllAddresses(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch addresses", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<EducationDTO> getAllEducations(Integer companyId) {
        try {
            return educationClient.getAllEducations(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch educations", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<BranchDTO> getAllBranches(Integer companyId) {
        try {
            return branchClient.getAllBranches(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch branches", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RegionDTO> getAllRegions(Integer companyId) {
        try {
            return regionClient.getAllRegions(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch regions", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<AttendanceDTO> getAllAttendances(Integer companyId) {
        try {
            return attendanceClient.getAttendanceByCompanyId(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch attendances", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<TimesheetDTO> getAllTimesheets(Integer companyId) {
        try {
            return timesheetClient.getAllTimesheets(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch timesheets", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<LeaveApplicationDTO> getAllLeaveApplications(Integer companyId) {
        try {
            return leaveApplicationClient.getAllLeaveApplications(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch leave applications", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<EmployeeLeaveAccountDTO> getAllEmployeeLeaveAccounts(Integer companyId) {
        try {
            return leaveAccountClient.getAllLeaveAccounts(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch employee leave accounts", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<HolidayDTO> getAllHolidays(Integer companyId) {
        try {
            return holidayClient.getAllHolidays(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch holidays", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<WorkingHoursDTO> getAllWorkingHours(Integer companyId) {
        try {
            return workingHoursClient.getAllWorkingHours(companyId);
        } catch (Exception ex) {
            logger.error("Failed to fetch working hours", ex);
            return Collections.emptyList();
        }
    }
}
