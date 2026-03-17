package com.bank.LMS.Repository;

import com.bank.LMS.Entity.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanTypeRepository extends JpaRepository<LoanType, Long> {

    List<LoanType> findAllByOrderByCreatedAtDesc();

    List<LoanType> findByActiveTrueOrderByLoanTypeNameAsc();

    boolean existsByLoanTypeCodeIgnoreCase(String code);

    Optional<LoanType> findByLoanTypeId(Long loanTypeId);
    List<LoanType> findByActiveTrue();

}