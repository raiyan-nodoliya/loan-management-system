package com.bank.LMS.Repository;

import com.bank.LMS.Entity.LoanApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanApplicationStatusRepository extends JpaRepository<LoanApplicationStatus, Long> {

    List<LoanApplicationStatus> findAllByOrderByStatusCodeAsc();

    List<LoanApplicationStatus> findByActiveTrueOrderByStatusCodeAsc();

    Optional<LoanApplicationStatus> findByStatusCode(String statusCode);


    boolean existsByStatusCode(String statusCode);
}