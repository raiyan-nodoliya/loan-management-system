package com.bank.LMS.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "actor_email", nullable = false, length = 80)
    private String actorEmail;

    @Column(nullable = false, length = 120)
    private String action;

    @Column(nullable = false, length = 60)
    private String module;

    @Column(nullable = false, length = 30)
    private String status; // SUCCESS / FAILED

    @Column(length = 255)
    private String details;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}