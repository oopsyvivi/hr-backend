package com.hrms.hr_backend.repository;

import com.hrms.hr_backend.entity.Attendance;
import com.hrms.hr_backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Find one employee's attendance for a specific date
    Optional<Attendance> findByEmployeeAndWorkDate(Employee employee, LocalDate date);

    // Find all attendance for one employee in a month
    @Query("SELECT a FROM Attendance a WHERE a.employee = :employee " +
           "AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month " +
           "ORDER BY a.workDate")
    List<Attendance> findByEmployeeAndMonth(
        @Param("employee") Employee employee,
        @Param("year") int year,
        @Param("month") int month
    );

    // Find all employees' attendance for a specific date (HR view)
    List<Attendance> findByWorkDateOrderByEmployee_FullName(LocalDate date);

    // Find all attendance for a month (HR monthly report)
    @Query("SELECT a FROM Attendance a WHERE " +
           "YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month " +
           "ORDER BY a.employee.fullName, a.workDate")
    List<Attendance> findByMonth(
        @Param("year") int year,
        @Param("month") int month
    );

    // Check if already checked in today
    boolean existsByEmployeeAndWorkDate(Employee employee, LocalDate date);
    //for weekly/monthly
    List<Attendance> findByWorkDateBetween(LocalDate startDate,LocalDate endDate);

}
