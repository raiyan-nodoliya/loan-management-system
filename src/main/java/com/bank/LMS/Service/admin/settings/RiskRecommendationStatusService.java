package com.bank.LMS.Service.admin.settings;

import com.bank.LMS.Entity.RiskRecommendationStatus;
import com.bank.LMS.Repository.RiskRecommendationStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RiskRecommendationStatusService {

    private final RiskRecommendationStatusRepository repo;

    public RiskRecommendationStatusService(RiskRecommendationStatusRepository repo) {
        this.repo = repo;
    }

    public List<RiskRecommendationStatus> listAll() {
        return repo.findAllByOrderByStatusCodeAsc();
    }

    public List<RiskRecommendationStatus> listActive() {
        return repo.findByActiveTrueOrderByStatusCodeAsc();
    }

    @Transactional
    public RiskRecommendationStatus create(String statusCode, String label) {

        String code = normalizeCode(statusCode);
        String lbl = normalizeLabel(label);

        if (code.isBlank()) throw new IllegalArgumentException("Status code is required");
        if (lbl.isBlank()) throw new IllegalArgumentException("Label is required");

        // Duplicate check
        repo.findByStatusCode(code).ifPresent(x -> {
            throw new IllegalArgumentException("Status code already exists: " + code);
        });

        RiskRecommendationStatus s = RiskRecommendationStatus.builder()
                .statusCode(code)
                .label(lbl)
                .active(true)
                .build();

        return repo.save(s);
    }

    @Transactional
    public void toggle(Long id) {
        RiskRecommendationStatus s = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Status not found with id: " + id));

        s.setActive(s.getActive() == null ? false : !s.getActive());
        repo.save(s);
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    private String normalizeLabel(String label) {
        return label == null ? "" : label.trim();
    }
}