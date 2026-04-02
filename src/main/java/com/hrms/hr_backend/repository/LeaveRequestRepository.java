package com.hrms.hr_backend.repository;

import com.hrms.hr_backend.entity.Employee;
import com.hrms.hr_backend.entity.LeaveRequest;
import com.hrms.hr_backend.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // All requests for one employee
    List<LeaveRequest> findByEmployeeOrderByAppliedAtDesc(Employee employee);

    // All requests — HR view
    List<LeaveRequest> findAllByOrderByAppliedAtDesc();

    // Pending requests only
    List<LeaveRequest> findByStatusOrderByAppliedAtDesc(String status);

    // Approved leaves for one employee and leave type
    // Used for balance calculation
    @Query("SELECT COALESCE(SUM(l.daysCount), 0) FROM LeaveRequest l " +
           "WHERE l.employee = :employee " +
           "AND l.leaveType = :leaveType " +
           "AND l.status = 'Approved' " +
           "AND YEAR(l.startDate) = :year")
    Double getTotalUsedDays(
        @Param("employee")  Employee employee,
        @Param("leaveType") LeaveType leaveType,
        @Param("year")      int year
    );

    // Check overlapping leave requests
    @Query("SELECT l FROM LeaveRequest l " +
           "WHERE l.employee = :employee " +
           "AND l.status IN ('Pending', 'Approved') " +
           "AND l.startDate <= :endDate " +
           "AND l.endDate >= :startDate")
    List<LeaveRequest> findOverlapping(
        @Param("employee")  Employee employee,
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate")   java.time.LocalDate endDate
    );
}