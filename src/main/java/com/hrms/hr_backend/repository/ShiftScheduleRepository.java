package com.hrms.hr_backend.repository;

import com.hrms.hr_backend.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, Long> {

    Optional<ShiftSchedule> findByShiftDate(LocalDate date);

    @Query("SELECT s FROM ShiftSchedule s WHERE " +
           "YEAR(s.shiftDate) = :year AND MONTH(s.shiftDate) = :month " +
           "ORDER BY s.shiftDate")
    List<ShiftSchedule> findByMonth(
        @Param("year") int year,
        @Param("month") int month
    );
}