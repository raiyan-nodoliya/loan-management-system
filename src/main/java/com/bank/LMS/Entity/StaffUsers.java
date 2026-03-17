package com.bank.LMS.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StaffUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Thymeleaf helper: u.roleName
    @Transient
    public String getRoleName() {
        return role != null ? role.getRoleName() : null;
    }

    @Column(name = "report_view_enabled", nullable = false)
    @Builder.Default
    private Boolean reportViewEnabled = false;

    @Column(name = "report_export_enabled", nullable = false)
    @Builder.Default
    private Boolean reportExportEnabled = false;
}