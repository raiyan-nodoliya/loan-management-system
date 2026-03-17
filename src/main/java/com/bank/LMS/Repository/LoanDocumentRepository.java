package com.bank.LMS.Repository;

import com.bank.LMS.Entity.LoanDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanDocumentRepository extends JpaRepository<LoanDocument, Long> {

    Optional<LoanDocument> findTopByApplication_ApplicationIdAndDocumentTypeAndIsLatestTrueOrderByVersionNoDesc(
            Long applicationId, String documentType
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update LoanDocument d
           set d.isLatest = false
         where d.application.applicationId = :appId
           and d.documentType = :docType
           and d.isLatest = true
    """)
    int markOldLatestFalse(@Param("appId") Long appId, @Param("docType") String docType);

    List<LoanDocument> findByApplication_ApplicationIdAndIsLatestTrueOrderByDocumentTypeAsc(Long appId);

    List<LoanDocument> findByApplication_ApplicationIdAndIsLatestTrueAndCustomer_CustomerIdOrderByDocumentTypeAsc(
            Long appId, Long customerId
    );

    List<LoanDocument> findByApplication_ApplicationIdAndCustomer_CustomerIdAndIsLatestTrueAndUploadedAtGreaterThanEqualOrderByDocumentTypeAsc(
            Long appId,
            Long customerId,
            java.time.LocalDateTime uploadedAt
    );
}