package com.bank.LMS.Repository;

import com.bank.LMS.Entity.EmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {

    List<EmiSchedule> findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(Long loanAccountId);

    long countByLoanAccount_LoanAccountId(Long loanAccountId);

    void deleteByLoanAccount_LoanAccountId(Long loanAccountId);

    Optional<EmiSchedule> findByEmiIdAndLoanAccount_Customer_CustomerId(Long emiId, Long customerId);
}