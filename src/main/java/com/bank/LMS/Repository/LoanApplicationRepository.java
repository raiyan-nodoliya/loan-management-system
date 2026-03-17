package com.bank.LMS.Repository;

import com.bank.LMS.Entity.LoanApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    Optional<LoanApplication> findByApplicationId(Long applicationId);

    Optional<LoanApplication> findByApplicationNo(String applicationNo);

    Optional<LoanApplication> findByApplicationIdAndCustomer_CustomerId(Long applicationId, Long customerId);

    List<LoanApplication> findTop5ByCustomer_CustomerIdOrderByApplicationIdDesc(Long customerId);

    List<LoanApplication> findByCustomer_CustomerIdAndStatus_StatusCodeInOrderByApplicationIdDesc(
            Long customerId, List<String> codes
    );

    long countByStatus_StatusCode(String statusCode);

    long countByStatus_StatusCodeIn(List<String> codes);

    List<LoanApplication> findByStatus_StatusCodeOrderByApplicationIdDesc(String statusCode);

    List<LoanApplication> findByStatus_StatusCodeInOrderByApplicationIdDesc(List<String> codes);

    @EntityGraph(attributePaths = {"customer", "loanType", "status"})
    @Query("""
            SELECT a
            FROM LoanApplication a
            LEFT JOIN a.customer c
            LEFT JOIN a.loanType lt
            LEFT JOIN a.status s
            WHERE (:fromDate IS NULL OR a.createdAt >= :fromDate)
              AND (:toDate IS NULL OR a.createdAt <= :toDate)
              AND (:status IS NULL OR :status = '' OR s.statusCode = :status)
              AND (:customerName IS NULL OR :customerName = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :customerName, '%')))
              AND (:customerCity IS NULL OR :customerCity = '' OR LOWER(c.city) LIKE LOWER(CONCAT('%', :customerCity, '%')))
              AND (:gender IS NULL OR :gender = '' OR UPPER(c.gender) = UPPER(:gender))
              AND (:loanTypeName IS NULL OR :loanTypeName = '' OR LOWER(lt.loanTypeName) LIKE LOWER(CONCAT('%', :loanTypeName, '%')))
              AND (:minAmount IS NULL OR a.amountRequested >= :minAmount)
              AND (:maxAmount IS NULL OR a.amountRequested <= :maxAmount)
              AND (:minSalary IS NULL OR a.monthlyIncome >= :minSalary)
              AND (:maxSalary IS NULL OR a.monthlyIncome <= :maxSalary)
              AND (:minDob IS NULL OR c.dob >= :minDob)
              AND (:maxDob IS NULL OR c.dob <= :maxDob)
            ORDER BY a.applicationId DESC
            """)
    List<LoanApplication> findFilteredApplications(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") String status,
            @Param("customerName") String customerName,
            @Param("customerCity") String customerCity,
            @Param("minDob") LocalDate minDob,
            @Param("maxDob") LocalDate maxDob,
            @Param("minSalary") BigDecimal minSalary,
            @Param("maxSalary") BigDecimal maxSalary,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("loanTypeName") String loanTypeName,
            @Param("gender") String gender
    );
}