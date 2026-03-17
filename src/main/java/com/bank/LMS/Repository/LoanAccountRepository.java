package com.bank.LMS.Repository;

import com.bank.LMS.Entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {

    Optional<LoanAccount> findByApplication_ApplicationId(Long applicationId);

    Optional<LoanAccount> findByAccountNo(String accountNo);

    boolean existsByApplication_ApplicationId(Long applicationId);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}