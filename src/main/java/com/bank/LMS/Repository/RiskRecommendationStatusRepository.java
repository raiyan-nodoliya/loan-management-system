package com.bank.LMS.Repository;

import com.bank.LMS.Entity.RiskRecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiskRecommendationStatusRepository extends JpaRepository<RiskRecommendationStatus, Long> {

    Optional<RiskRecommendationStatus> findByStatusCode(String statusCode);


    List<RiskRecommendationStatus> findAllByOrderByStatusCodeAsc();

    List<RiskRecommendationStatus> findByActiveTrueOrderByStatusCodeAsc();
}