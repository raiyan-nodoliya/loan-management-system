package com.bank.LMS.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_recommendation_status")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskRecommendationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rr_status_id")
    private Long id;

    @Column(name = "status_code", nullable = false, unique = true, length = 20)
    private String statusCode;

    @Column(name = "label", nullable = false, length = 40)
    private String label;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (active == null) active = true;
        createdAt = LocalDateTime.now();
    }
}