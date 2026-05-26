package com.ceodashboard.backend.hrms.specification;

import com.ceodashboard.backend.hrms.entity.Attendance;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkforceHealthAttendanceSpecification {

    /**
     * Build dynamic specification for attendance filtering with search, date range,
     * and department
     */
    public static Specification<Attendance> buildAttendanceFilter(
            String searchTerm,
            Long departmentId,
            LocalDate fromDate,
            LocalDate toDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by employee name or code
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("employee").get("firstName")), searchPattern),
                        cb.like(cb.lower(root.get("employee").get("lastName")), searchPattern),
                        cb.like(cb.lower(root.get("employee").get("employeeCode")), searchPattern)));
            }

            // Filter by department
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("employee").get("department").get("id"), departmentId));
            }

            // Filter by date range
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("attendanceDate"), fromDate));
            }

            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("attendanceDate"), toDate));
            }

            // Only include checked-in records
            predicates.add(cb.equal(root.get("hasCheckedIn"), true));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter attendance by employee and date
     */
    public static Specification<Attendance> byEmployeeAndDate(Long employeeId, LocalDate date) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("employee").get("id"), employeeId),
                cb.equal(root.get("attendanceDate"), date));
    }

    /**
     * Filter late arrivals for current date
     */
    public static Specification<Attendance> lateLateArrivals(LocalDate date) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("attendanceDate"), date),
                cb.equal(root.get("hasCheckedIn"), true));
    }
}
