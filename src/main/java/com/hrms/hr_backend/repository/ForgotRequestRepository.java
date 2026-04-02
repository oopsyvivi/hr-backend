package com.hrms.hr_backend.repository;

import com.hrms.hr_backend.entity.Employee;
import com.hrms.hr_backend.entity.ForgotRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ForgotRequestRepository extends JpaRepository<ForgotRequest, Long> {

    List<ForgotRequest> findByStatusOrderBySubmittedAtDesc(String status);
    List<ForgotRequest> findByEmployeeOrderBySubmittedAtDesc(Employee employee);
    List<ForgotRequest> findAllByOrderBySubmittedAtDesc();
}
