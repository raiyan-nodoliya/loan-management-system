package com.bank.LMS.Service.admin.settings;

import com.bank.LMS.Entity.LoanType;
import com.bank.LMS.Repository.LoanTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class LoanTypeService {

    private final LoanTypeRepository repo;

    public LoanTypeService(LoanTypeRepository repo) {
        this.repo = repo;
    }

    public List<LoanType> getAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void create(String code, String name) {
        if (repo.existsByLoanTypeCodeIgnoreCase(code)) {
            throw new RuntimeException("Code '" + code + "' already exists!");
        }

        LoanType lt = LoanType.builder()
                .loanTypeCode(code.trim().toUpperCase())
                .loanTypeName(name.trim())
                .active(true)
                .build();

        repo.save(lt);
    }

    @Transactional
    public void toggle(Long id) {
        LoanType lt = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        lt.setActive(!lt.getActive());
        repo.save(lt);
    }
}