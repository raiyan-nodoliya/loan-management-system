package com.bank.LMS.Repository;

import com.bank.LMS.Entity.LoanTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long> {

    List<LoanTransaction> findByLoanAccount_LoanAccountIdOrderByPaymentDateDesc(Long loanAccountId);

    List<LoanTransaction> findByEmiSchedule_EmiIdOrderByPaymentDateDesc(Long emiId);
}