package com.bank.LMS.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_application_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "status_code", nullable = false, unique = true, length = 30)
    private String statusCode; // DRAFT, SUBMITTED, IN_REVIEW, NEEDS_INFO, APPROVED, REJECTED, DISBURSED

    @Column(name = "label", nullable = false, length = 60)
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