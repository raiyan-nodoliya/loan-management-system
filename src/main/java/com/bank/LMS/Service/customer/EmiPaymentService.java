package com.bank.LMS.Service.customer;

import com.bank.LMS.Entity.EmiSchedule;
import com.bank.LMS.Entity.LoanTransaction;
import com.bank.LMS.Repository.EmiScheduleRepository;
import com.bank.LMS.Repository.LoanTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class EmiPaymentService {

    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanTransactionRepository loanTransactionRepository;

    public EmiPaymentService(EmiScheduleRepository emiScheduleRepository,
                             LoanTransactionRepository loanTransactionRepository) {
        this.emiScheduleRepository = emiScheduleRepository;
        this.loanTransactionRepository = loanTransactionRepository;
    }

    public EmiSchedule getCustomerEmi(Long emiId, Long customerId) {
        return emiScheduleRepository.findByEmiIdAndLoanAccount_Customer_CustomerId(emiId, customerId)
                .orElse(null);
    }

    public String validatePayment(String paymentMethod,
                                  String upiId,
                                  String cardNumber,
                                  String cardHolderName,
                                  String expiry,
                                  String cvv) {

        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "Please select payment method";
        }

        switch (paymentMethod) {
            case "UPI" -> {
                if (upiId == null || upiId.isBlank()) {
                    return "UPI ID is required";
                }
                if (!Pattern.matches("^[a-zA-Z0-9._-]{2,}@[a-zA-Z]{2,}$", upiId.trim())) {
                    return "Enter valid UPI ID";
                }
            }

            case "CREDIT_CARD", "DEBIT_CARD" -> {
                if (cardHolderName == null || cardHolderName.isBlank()) {
                    return "Card holder name is required";
                }

                String cleanCard = cardNumber == null ? "" : cardNumber.replaceAll("\\s+", "");
                if (!Pattern.matches("^\\d{16}$", cleanCard)) {
                    return "Card number must be 16 digits";
                }

                if (expiry == null || !Pattern.matches("^(0[1-9]|1[0-2])/\\d{2}$", expiry.trim())) {
                    return "Expiry must be in MM/YY format";
                }

                if (cvv == null || !Pattern.matches("^\\d{3}$", cvv.trim())) {
                    return "CVV must be 3 digits";
                }
            }

            default -> {
                return "Invalid payment method";
            }
        }

        return null;
    }

    public boolean payEmi(Long emiId,
                          Long customerId,
                          String paymentMethod,
                          String upiId,
                          String cardNumber,
                          String cardHolderName,
                          String expiry,
                          String cvv) {

        EmiSchedule emi = getCustomerEmi(emiId, customerId);
        if (emi == null) return false;

        if (!"PENDING".equalsIgnoreCase(emi.getStatus())) {
            return false;
        }

        String validationError = validatePayment(paymentMethod, upiId, cardNumber, cardHolderName, expiry, cvv);
        if (validationError != null) {
            return false;
        }

        String payerDetail;
        if ("UPI".equals(paymentMethod)) {
            payerDetail = upiId.trim();
        } else {
            String cleanCard = cardNumber.replaceAll("\\s+", "");
            payerDetail = "XXXX-XXXX-XXXX-" + cleanCard.substring(12);
        }

        LoanTransaction txn = LoanTransaction.builder()
                .loanAccount(emi.getLoanAccount())
                .emiSchedule(emi)
                .amount(emi.getEmiAmount())
                .paymentDate(LocalDateTime.now())
                .paymentMethod(paymentMethod)
                .payerDetail(payerDetail)
                .referenceNo("TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                .status("SUCCESS")
                .remarks("Dummy payment success")
                .build();

        loanTransactionRepository.save(txn);

        emi.setStatus("PAID");
        emi.setPaidAt(LocalDateTime.now());
        emiScheduleRepository.save(emi);

        return true;
    }
}