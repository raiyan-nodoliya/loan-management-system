package com.bank.LMS.Repository;

import com.bank.LMS.Entity.LoanApplication;
import com.bank.LMS.Entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    Optional<RiskAssessment> findByApplication(LoanApplication application);

    long countByRecommendationStatus_StatusCode(String statusCode);

    long countByAssessedAtBetween(LocalDateTime start, LocalDateTime end);
}