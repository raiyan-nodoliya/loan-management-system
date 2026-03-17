package com.bank.LMS.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id", nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emi_id", nullable = false)
    private EmiSchedule emiSchedule;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod; // UPI, CREDIT_CARD, DEBIT_CARD

    @Column(name = "payer_detail", length = 150)
    private String payerDetail; // UPI ID or masked card no

    @Column(name = "reference_no", nullable = false, unique = true, length = 60)
    private String referenceNo;

    @Column(nullable = false, length = 20)
    private String status; // SUCCESS, FAILED

    @Column(length = 255)
    private String remarks;

    @PrePersist
    public void onCreate() {
        if (this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "SUCCESS";
        }
    }
}