package com.bank.LMS.Service.customer;

import com.bank.LMS.Entity.Customer;
import com.bank.LMS.Entity.EmiSchedule;
import com.bank.LMS.Entity.LoanAccount;
import com.bank.LMS.Entity.LoanApplication;
import com.bank.LMS.Repository.CustomerRepository;
import com.bank.LMS.Repository.EmiScheduleRepository;
import com.bank.LMS.Repository.LoanAccountRepository;
import com.bank.LMS.Repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerLoanService {

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final EmiScheduleRepository emiScheduleRepository;

    public CustomerLoanService(CustomerRepository customerRepository,
                               LoanApplicationRepository loanApplicationRepository,
                               LoanAccountRepository loanAccountRepository,
                               EmiScheduleRepository emiScheduleRepository) {
        this.customerRepository = customerRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.emiScheduleRepository = emiScheduleRepository;
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    public List<LoanApplication> getMyLoans(Long customerId) {
        return loanApplicationRepository.findByCustomer_CustomerIdAndStatus_StatusCodeInOrderByApplicationIdDesc(
                customerId,
                List.of("APPROVED", "DISBURSED")
        );
    }

    public LoanAccount getLoanAccountByApplicationId(Long applicationId) {
        return loanAccountRepository.findByApplication_ApplicationId(applicationId).orElse(null);
    }

    public List<EmiSchedule> getEmisByApplicationId(Long applicationId) {
        LoanAccount loanAccount = getLoanAccountByApplicationId(applicationId);
        if (loanAccount == null) {
            return Collections.emptyList();
        }
        return emiScheduleRepository.findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(
                loanAccount.getLoanAccountId()
        );
    }
}