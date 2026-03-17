package com.bank.LMS.Service.officer;

import com.bank.LMS.Entity.LoanApplication;
import com.bank.LMS.Entity.LoanApplicationStatus;
import com.bank.LMS.Entity.LoanDocument;
import com.bank.LMS.Repository.LoanApplicationRepository;
import com.bank.LMS.Repository.LoanApplicationStatusRepository;
import com.bank.LMS.Repository.LoanDocumentRepository;
import com.bank.LMS.Service.config.MailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OfficerReviewService {

    private final LoanApplicationRepository loanRepo;
    private final LoanDocumentRepository docRepo;
    private final LoanApplicationStatusRepository statusRepo;
    private final MailService mailService;

    public OfficerReviewService(LoanApplicationRepository loanRepo,
                                LoanDocumentRepository docRepo,
                                LoanApplicationStatusRepository statusRepo,
                                MailService mailService) {
        this.loanRepo = loanRepo;
        this.docRepo = docRepo;
        this.statusRepo = statusRepo;
        this.mailService = mailService;
    }

    public long countSubmitted() {
        return loanRepo.countByStatus_StatusCode("SUBMITTED");
    }

    public long countNeedsInfo() {
        return loanRepo.countByStatus_StatusCode("NEEDS_INFO");
    }

    public long countInReview() {
        return loanRepo.countByStatus_StatusCode("IN_REVIEW");
    }

    public List<LoanApplication> queue() {
        return loanRepo.findByStatus_StatusCodeInOrderByApplicationIdDesc(
                List.of("SUBMITTED", "NEEDS_INFO", "IN_REVIEW")
        );
    }

    public LoanApplication getApp(Long id) {
        return loanRepo.findById(id).orElse(null);
    }

    public List<LoanDocument> getDocs(Long appId) {
        return docRepo.findByApplication_ApplicationIdAndIsLatestTrueOrderByDocumentTypeAsc(appId);
    }

    @Transactional
    public boolean markInReview(Long appId, String notes) {
        LoanApplication app = loanRepo.findById(appId).orElse(null);
        if (app == null) return false;

        LoanApplicationStatus st = statusRepo.findByStatusCode("IN_REVIEW").orElse(null);
        if (st == null) return false;

        app.setStatus(st);

        if (app.getOfficerReviewStartedAt() == null) {
            app.setOfficerReviewStartedAt(LocalDateTime.now());
        }

        if (notes != null && !notes.isBlank()) {
            app.setOfficerNotes(notes);
        }

        loanRepo.save(app);

        sendCustomerMail(
                app,
                "Application put under review",
                notes != null && !notes.isBlank() ? notes : "Your application is currently under officer review."
        );

        return true;
    }

    @Transactional
    public boolean requestInfo(Long appId, String message, String notes) {
        LoanApplication app = loanRepo.findById(appId).orElse(null);
        if (app == null) return false;

        LoanApplicationStatus st = statusRepo.findByStatusCode("NEEDS_INFO").orElse(null);
        if (st == null) return false;

        app.setStatus(st);
        app.setNeedsInfoMessage(message);
        app.setNeedsInfoAt(LocalDateTime.now());

        if (app.getOfficerReviewStartedAt() == null) {
            app.setOfficerReviewStartedAt(LocalDateTime.now());
        }

        if (notes != null && !notes.isBlank()) {
            app.setOfficerNotes(notes);
        }

        loanRepo.save(app);

        StringBuilder desc = new StringBuilder("Requested additional information from customer.");
        if (message != null && !message.isBlank()) {
            desc.append(" Message: ").append(message);
        }
        if (notes != null && !notes.isBlank()) {
            desc.append(" Officer Notes: ").append(notes);
        }

        sendCustomerMail(app, "Additional information required", desc.toString());
        return true;
    }

    @Transactional
    public boolean forwardToRisk(Long id, String officerNotes) {
        LoanApplication app = loanRepo.findById(id).orElse(null);
        if (app == null) return false;

        LoanApplicationStatus riskEvalStatus = statusRepo.findByStatusCode("FORWARDED_TO_RISK").orElse(null);
        if (riskEvalStatus == null) {
            riskEvalStatus = statusRepo.findByStatusCode("RISK_EVALUATION").orElse(null);
        }
        if (riskEvalStatus == null) return false;

        app.setStatus(riskEvalStatus);

        if (app.getOfficerReviewStartedAt() == null) {
            app.setOfficerReviewStartedAt(LocalDateTime.now());
        }

        app.setForwardedToRiskAt(LocalDateTime.now());

        if (officerNotes != null && !officerNotes.isBlank()) {
            app.setOfficerNotes(officerNotes);
        }

        loanRepo.save(app);

        sendCustomerMail(
                app,
                "Application forwarded to Risk Officer",
                officerNotes != null && !officerNotes.isBlank()
                        ? officerNotes
                        : "Your application has been forwarded for risk evaluation."
        );

        return true;
    }

    private void sendCustomerMail(LoanApplication app, String actionTitle, String description) {
        if (app == null || app.getCustomer() == null) return;
        if (app.getCustomer().getEmail() == null || app.getCustomer().getEmail().isBlank()) return;

        String customerName = app.getCustomer().getName() != null ? app.getCustomer().getName() : "Customer";
        String applicationNo = app.getApplicationNo() != null ? app.getApplicationNo() : String.valueOf(app.getApplicationId());
        String statusCode = (app.getStatus() != null && app.getStatus().getStatusCode() != null)
                ? app.getStatus().getStatusCode()
                : "UNKNOWN";

        mailService.sendApplicationStatusUpdate(
                app.getCustomer().getEmail(),
                customerName,
                applicationNo,
                statusCode,
                actionTitle,
                description
        );
    }
}