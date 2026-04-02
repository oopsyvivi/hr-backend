package com.hrms.hr_backend.repository;

import com.hrms.hr_backend.entity.Employee;
import com.hrms.hr_backend.entity.Overtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OvertimeRepository extends JpaRepository<Overtime, Long> {

    // All requests for one employee
    List<Overtime> findByEmployeeOrderByDateDesc(Employee employee);

    // All requests — HR view
    List<Overtime> findAllByOrderByAppliedAtDesc();

    // Monthly report
    @Query("SELECT o FROM Overtime o WHERE " +
           "YEAR(o.date) = :year AND MONTH(o.date) = :month " +
           "AND o.status = 'Approved' " +
           "ORDER BY o.employee.fullName, o.date")
    List<Overtime> findApprovedByMonth(
        @Param("year")  int year,
        @Param("month") int month
    );

    // Check duplicate date for same employee
    @Query("SELECT COUNT(o) > 0 FROM Overtime o WHERE " +
           "o.employee = :employee AND o.date = :date " +
           "AND o.status IN ('Pending', 'Approved')")
    boolean existsByEmployeeAndDate(
        @Param("employee") Employee employee,
        @Param("date")     LocalDate date
    );

    // Monthly summary per employee
    @Query("SELECT o FROM Overtime o WHERE " +
           "YEAR(o.date) = :year AND MONTH(o.date) = :month " +
           "ORDER BY o.appliedAt DESC")
    List<Overtime> findByMonth(
        @Param("year")  int year,
        @Param("month") int month
    );
}