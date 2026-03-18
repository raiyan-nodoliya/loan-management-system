package com.bank.LMS.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "risk_id")
    private Long riskId;

    // One assessment per application
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private LoanApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_officer_id", nullable = false)
    private StaffUsers riskOfficer;

    // ✅ FOREIGN KEY instead of String
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rr_status_id", nullable = false)
    private RiskRecommendationStatus recommendationStatus;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(length = 300)
    private String remarks;

    @Column(name = "assessed_at")
    private LocalDateTime assessedAt;

    @PrePersist
    protected void onCreate() {
        this.assessedAt = LocalDateTime.now();
    }
}