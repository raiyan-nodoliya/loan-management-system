package com.bank.LMS.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "loan_documents",
        indexes = {
                @Index(name = "idx_ld_app", columnList = "application_id"),
                @Index(name = "idx_ld_cust", columnList = "customer_id"),
                @Index(name = "idx_ld_type_latest", columnList = "document_type,is_latest")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDocument {

    public enum DocumentStatus { UPLOADED, VERIFIED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_doc_application"))
    private LoanApplication application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_doc_customer"))
    private Customer customer;

    @Column(name = "document_type", nullable = false, length = 40)
    private String documentType; // PAN, AADHAAR, etc.

    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName; // stored unique name

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath; // relative recommended: loan_docs/APPNO/xyz.pdf

    @Column(name = "original_file_name", length = 200)
    private String originalFileName;

    @Column(name = "mime_type", length = 80)
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Builder.Default
    @Column(name = "version_no", nullable = false)
    private Integer versionNo = 1;

    @Builder.Default
    @Column(name = "is_latest", nullable = false)
    private boolean isLatest = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_staff_id",
            foreignKey = @ForeignKey(name = "fk_doc_review_staff"))
    private StaffUsers reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(name = "review_remarks", length = 500)
    private String reviewRemarks;

    @Column(name = "uploaded_at", updatable = false, nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (status == null) status = DocumentStatus.UPLOADED;
        if (versionNo == null) versionNo = 1;
    }
}