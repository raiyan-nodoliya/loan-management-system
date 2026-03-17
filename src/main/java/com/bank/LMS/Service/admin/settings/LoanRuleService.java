package com.bank.LMS.Service.admin.settings;

import com.bank.LMS.Entity.LoanType;
import com.bank.LMS.Repository.LoanTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class LoanRuleService {

    private final LoanTypeRepository loanTypeRepository;

    public LoanRuleService(LoanTypeRepository loanTypeRepository) {
        this.loanTypeRepository = loanTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<LoanType> getAllLoanTypes() {
        List<LoanType> list = loanTypeRepository.findAll();
        System.out.println("SERVICE -> loan types count = " + list.size());
        for (LoanType x : list) {
            System.out.println("SERVICE -> ID=" + x.getLoanTypeId() + ", NAME=" + x.getLoanTypeName());
        }
        return list;
    }

    @Transactional(readOnly = true)
    public LoanType getById(Long id) {
        return loanTypeRepository.findById(id).orElse(null);
    }

    public void saveRule(Long loanTypeId,
                         BigDecimal minMonthlyIncome,
                         BigDecimal maxAmount,
                         Integer maxTenureMonths,
                         BigDecimal interestRateMin,
                         BigDecimal interestRateMax) {

        LoanType loanType = loanTypeRepository.findById(loanTypeId)
                .orElseThrow(() -> new RuntimeException("Loan type not found with id: " + loanTypeId));

        loanType.setMinMonthlyIncome(minMonthlyIncome);
        loanType.setMaxAmount(maxAmount);
        loanType.setMaxTenureMonths(maxTenureMonths);
        loanType.setInterestRateMin(interestRateMin);
        loanType.setInterestRateMax(interestRateMax);

        loanTypeRepository.save(loanType);
    }
}