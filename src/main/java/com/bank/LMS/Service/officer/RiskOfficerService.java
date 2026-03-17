package com.bank.LMS.Service.officer;

import com.bank.LMS.Entity.LoanApplication;
import com.bank.LMS.Entity.LoanApplicationStatus;
import com.bank.LMS.Entity.RiskAssessment;
import com.bank.LMS.Entity.RiskRecommendationStatus;
import com.bank.LMS.Entity.StaffUsers;
import com.bank.LMS.Repository.LoanApplicationRepository;
import com.bank.LMS.Repository.LoanApplicationStatusRepository;
import com.bank.LMS.Repository.RiskAssessmentRepository;
import com.bank.LMS.Repository.RiskRecommendationStatusRepository;
import com.bank.LMS.Repository.StaffUsersRepository;
import com.bank.LMS.Service.config.MailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RiskOfficerService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationStatusRepository loanApplicationStatusRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RiskRecommendationStatusRepository riskRecommendationStatusRepository;
    private final StaffUsersRepository staffUsersRepository;
    private final MailService mailService;

    public RiskOfficerService(LoanApplicationRepository loanApplicationRepository,
                              LoanApplicationStatusRepository loanApplicationStatusRepository,
                              RiskAssessmentRepository riskAssessmentRepository,
                              RiskRecommendationStatusRepository riskRecommendationStatusRepository,
                              StaffUsersRepository staffUsersRepository,
                              MailService mailService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanApplicationStatusRepository = loanApplicationStatusRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.riskRecommendationStatusRepository = riskRecommendationStatusRepository;
        this.staffUsersRepository = staffUsersRepository;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public long pendingEvaluationCount() {
        return loanApplicationRepository.countByStatus_StatusCode("RISK_EVALUATION");
    }

    @Transactional(readOnly = true)
    public long highRiskCount() {
        return riskAssessmentRepository.countByRecommendationStatus_StatusCode("HIGH_RISK");
    }

    @Transactional(readOnly = true)
    public long completedTodayCount() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return riskAssessmentRepository.countByAssessedAtBetween(start, end);
    }

    @Transactional(readOnly = true)
    public List<LoanApplication> riskQueue() {
        return loanApplicationRepository.findByStatus_StatusCodeOrderByApplicationIdDesc("RISK_EVALUATION");
    }

    @Transactional(readOnly = true)
    public LoanApplication getApplication(Long id) {
        return loanApplicationRepository.findByApplicationId(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public RiskAssessment getExistingAssessment(Long applicationId) {
        LoanApplication app = getApplication(applicationId);
        if (app == null) return null;
        return riskAssessmentRepository.findByApplication(app).orElse(null);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateFoir(BigDecimal monthlyIncome, BigDecimal existingEmis) {
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (existingEmis == null) {
            existingEmis = BigDecimal.ZERO;
        }

        return existingEmis
                .multiply(BigDecimal.valueOf(100))
                .divide(monthlyIncome, 2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public int calculateRiskScore(BigDecimal monthlyIncome,
                                  BigDecimal existingEmis,
                                  BigDecimal requestedAmount) {

        int score = 0;

        BigDecimal foir = calculateFoir(monthlyIncome, existingEmis);

        if (foir.compareTo(BigDecimal.valueOf(20)) <= 0) {
            score += 20;
        } else if (foir.compareTo(BigDecimal.valueOf(35)) <= 0) {
            score += 12;
        } else {
            score += 5;
        }

        if (monthlyIncome != null) {
            if (monthlyIncome.compareTo(BigDecimal.valueOf(100000)) >= 0) {
                score += 25;
            } else if (monthlyIncome.compareTo(BigDecimal.valueOf(50000)) >= 0) {
                score += 15;
            } else {
                score += 8;
            }
        }

        if (requestedAmount != null) {
            if (requestedAmount.compareTo(BigDecimal.valueOf(1000000)) <= 0) {
                score += 25;
            } else if (requestedAmount.compareTo(BigDecimal.valueOf(3000000)) <= 0) {
                score += 15;
            } else {
                score += 8;
            }
        }

        score += 20;
        score += 10;

        return Math.min(score, 100);
    }

    public String autoRiskLevel(int riskScore) {
        if (riskScore >= 75) return "LOW";
        if (riskScore >= 45) return "MEDIUM";
        return "HIGH";
    }

    public boolean submitEvaluation(Long applicationId,
                                    BigDecimal monthlyIncome,
                                    BigDecimal existingEmis,
                                    String recommendation,
                                    String notes) {

        Optional<LoanApplication> appOpt = loanApplicationRepository.findByApplicationId(applicationId);
        if (appOpt.isEmpty()) return false;

        LoanApplication app = appOpt.get();

        StaffUsers currentOfficer = getCurrentStaffUser();
        if (currentOfficer == null) return false;

        int riskScore = calculateRiskScore(monthlyIncome, existingEmis, app.getAmountRequested());
        String riskLevel = autoRiskLevel(riskScore);

        String rrCode;
        String appStatusCode;

        switch (recommendation) {
            case "APPROVE" -> {
                rrCode = "RECOMMENDED";
                appStatusCode = "RECOMMENDED";
            }
            case "REJECT" -> {
                rrCode = "REJECTED";
                appStatusCode = "REJECTED";
            }
            case "NEED_MORE_INFO" -> {
                rrCode = "NEED_MORE_INFO";
                appStatusCode = "NEEDS_INFO";
            }
            default -> {
                return false;
            }
        }

        if ("HIGH".equals(riskLevel) && "APPROVE".equals(recommendation)) {
            rrCode = "HIGH_RISK";
            appStatusCode = "RISK_HOLD";
        }

        RiskRecommendationStatus rrStatus = riskRecommendationStatusRepository
                .findByStatusCode(rrCode)
                .orElse(null);

        LoanApplicationStatus appStatus = loanApplicationStatusRepository
                .findByStatusCode(appStatusCode)
                .orElse(null);

        if (rrStatus == null || appStatus == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        RiskAssessment assessment = riskAssessmentRepository
                .findByApplication(app)
                .orElse(new RiskAssessment());

        assessment.setApplication(app);
        assessment.setRiskOfficer(currentOfficer);
        assessment.setRecommendationStatus(rrStatus);
        assessment.setRiskScore(riskScore);
        assessment.setRemarks(notes);
        assessment.setAssessedAt(now);

        riskAssessmentRepository.save(assessment);

        app.setMonthlyIncome(monthlyIncome);
        app.setExistingEmis(existingEmis);
        app.setStatus(appStatus);

        // timeline fields update
        if ("APPROVE".equals(recommendation)) {
            if ("HIGH".equals(riskLevel)) {
                // high risk hold case
                app.setRiskRecommendedApproveAt(null);
                app.setRiskRecommendedRejectAt(null);
            } else {
                app.setRiskRecommendedApproveAt(now);
                app.setRiskRecommendedRejectAt(null);
            }
        } else if ("REJECT".equals(recommendation)) {
            app.setRiskRecommendedRejectAt(now);
            app.setRiskRecommendedApproveAt(null);
        } else if ("NEED_MORE_INFO".equals(recommendation)) {
            app.setNeedsInfoAt(now);
            app.setNeedsInfoMessage(notes);
        }

        loanApplicationRepository.save(app);

        String description =
                "Risk evaluation completed. " +
                        "Risk Score: " + riskScore +
                        ", Risk Level: " + riskLevel +
                        ", Recommendation Code: " + rrCode +
                        ((notes != null && !notes.isBlank()) ? ", Notes: " + notes : "");

        sendCustomerMail(app, "Risk evaluation update", description);

        return true;
    }

    private void sendCustomerMail(LoanApplication app, String actionTitle, String description) {
        if (app == null || app.getCustomer() == null) return;
        if (app.getCustomer().getEmail() == null || app.getCustomer().getEmail().isBlank()) return;

        String customerName = app.getCustomer().getName() != null ? app.getCustomer().getName() : "Customer";
        String applicationNo = app.getApplicationNo() != null
                ? app.getApplicationNo()
                : String.valueOf(app.getApplicationId());

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

    @Transactional(readOnly = true)
    public StaffUsers getCurrentStaffUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.isBlank()) {
            return null;
        }

        return staffUsersRepository.findByEmail(username).orElse(null);
    }
}