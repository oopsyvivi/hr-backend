package com.hrms.hr_backend.repository;

import com.hrms.hr_backend.entity.Employee;
import com.hrms.hr_backend.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    // Get all payroll for a month
    List<Payroll> findByMonthAndYearOrderByEmployee_FullName(int month, int year);

    // Get payroll for one employee
    List<Payroll> findByEmployeeOrderByYearDescMonthDesc(Employee employee);

    // Get specific payroll record
    Optional<Payroll> findByEmployeeAndMonthAndYear(
        Employee employee, int month, int year
    );

    // Check if payroll already exists
    boolean existsByEmployeeAndMonthAndYear(
        Employee employee, int month, int year
    );

    // Summary query
    @Query("SELECT SUM(p.grossSalary) FROM Payroll p " +
           "WHERE p.month = :month AND p.year = :year")
    Double getTotalGross(@Param("month") int month, @Param("year") int year);

    @Query("SELECT SUM(p.netSalary) FROM Payroll p " +
           "WHERE p.month = :month AND p.year = :year")
    Double getTotalNet(@Param("month") int month, @Param("year") int year);
}
