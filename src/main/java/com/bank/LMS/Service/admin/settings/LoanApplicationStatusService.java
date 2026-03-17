package com.bank.LMS.Service.admin.settings;

import com.bank.LMS.Entity.LoanApplicationStatus;
import com.bank.LMS.Repository.LoanApplicationStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanApplicationStatusService {

    private final LoanApplicationStatusRepository repo;

    public LoanApplicationStatusService(LoanApplicationStatusRepository repo) {
        this.repo = repo;
    }

    // ✅ List
    public List<LoanApplicationStatus> listAll() {
        return repo.findAllByOrderByStatusCodeAsc();
    }

    // ✅ Create (SAVE RECORD)
    public LoanApplicationStatus create(String statusCode, String label) {

        String code = statusCode == null ? "" : statusCode.trim().toUpperCase();
        String lab  = label == null ? "" : label.trim();

        if (code.isEmpty() || lab.isEmpty()) {
            throw new RuntimeException("Status code and label are required");
        }

        if (repo.existsByStatusCode(code)) {
            throw new RuntimeException("Status code already exists: " + code);
        }

        LoanApplicationStatus s = LoanApplicationStatus.builder()
                .statusCode(code)
                .label(lab)
                .active(true)
                .build();

        return repo.save(s);
    }

    // ✅ Toggle enable/disable
    public void toggle(Long id) {
        LoanApplicationStatus s = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Status not found: " + id));

        s.setActive(!Boolean.TRUE.equals(s.getActive()));
        repo.save(s);
    }

    // ✅ For dropdown usage (only active)
    public List<LoanApplicationStatus> activeStatuses() {
        return repo.findByActiveTrueOrderByStatusCodeAsc();
    }

    public LoanApplicationStatus mustGetByCode(String code) {
        return repo.findByStatusCode(code)
                .orElseThrow(() -> new RuntimeException("Status not found: " + code));
    }
}