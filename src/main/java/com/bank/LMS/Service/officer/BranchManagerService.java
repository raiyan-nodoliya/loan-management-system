package com.bank.LMS.Service.officer;

import com.bank.LMS.Entity.*;
import com.bank.LMS.Repository.*;
import com.bank.LMS.Service.config.MailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BranchManagerService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationStatusRepository loanApplicationStatusRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final StaffUsersRepository staffUsersRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final MailService mailService;

    public BranchManagerService(LoanApplicationRepository loanApplicationRepository,
                                LoanApplicationStatusRepository loanApplicationStatusRepository,
                                RiskAssessmentRepository riskAssessmentRepository,
                                StaffUsersRepository staffUsersRepository,
                                LoanAccountRepository loanAccountRepository,
                                EmiScheduleRepository emiScheduleRepository,
                                MailService mailService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanApplicationStatusRepository = loanApplicationStatusRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.staffUsersRepository = staffUsersRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.emiScheduleRepository = emiScheduleRepository;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public long recommendedApproveCount() {
        return loanApplicationRepository.countByStatus_StatusCode("RECOMMENDED");
    }

    @Transactional(readOnly = true)
    public long recommendedRejectCount() {
        return loanApplicationRepository.countByStatus_StatusCode("REJECTED");
    }

    @Transactional(readOnly = true)
    public long approvedTodayCount() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return loanAccountRepository.countByCreatedAtBetween(start, end);
    }

    @Transactional(readOnly = true)
    public long disbursalPendingCount() {
        return loanApplicationRepository.countByStatus_StatusCode("APPROVED");
    }

    @Transactional(readOnly = true)
    public List<LoanApplication> dashboardApplications() {
        return loanApplicationRepository.findByStatus_StatusCodeInOrderByApplicationIdDesc(
                List.of(
                        "RECOMMENDED",
                        "OFFER_SENT_TO_CUSTOMER",
                        "CUSTOMER_ACCEPTED_OFFER",
                        "CUSTOMER_REJECTED_OFFER",
                        "APPROVED",
                        "DISBURSED",
                        "REJECTED"
                )
        );
    }

    @Transactional(readOnly = true)
    public LoanApplication getApplication(Long applicationId) {
        return loanApplicationRepository.findByApplicationId(applicationId).orElse(null);
    }

    @Transactional(readOnly = true)
    public RiskAssessment getRiskAssessment(Long applicationId) {
        LoanApplication app = getApplication(applicationId);
        if (app == null) return null;
        return riskAssessmentRepository.findByApplication(app).orElse(null);
    }

    @Transactional(readOnly = true)
    public LoanAccount getLoanAccountByApplication(Long applicationId) {
        return loanAccountRepository.findByApplication_ApplicationId(applicationId).orElse(null);
    }

    public boolean approveLoan(Long applicationId,
                               BigDecimal sanctionAmount,
                               BigDecimal interestRateAnnual,
                               Integer tenureMonths,
                               String managerNotes) {

        Optional<LoanApplication> appOpt = loanApplicationRepository.findByApplicationId(applicationId);
        if (appOpt.isEmpty()) return false;

        LoanApplication app = appOpt.get();

        if (app.getStatus() == null || app.getStatus().getStatusCode() == null) return false;

        String currentStatus = app.getStatus().getStatusCode();
        if (!"RECOMMENDED".equals(currentStatus) && !"APPROVED".equals(currentStatus)) {
            return false;
        }

        if (sanctionAmount == null || sanctionAmount.compareTo(BigDecimal.ZERO) <= 0) return false;
        if (interestRateAnnual == null || interestRateAnnual.compareTo(BigDecimal.ZERO) <= 0) return false;
        if (tenureMonths == null || tenureMonths <= 0) return false;

        StaffUsers manager = getCurrentStaffUser();
        if (manager == null) return false;

        LoanApplicationStatus approvedStatus = loanApplicationStatusRepository
                .findByStatusCode("APPROVED")
                .orElse(null);

        if (approvedStatus == null) return false;

        LoanAccount account = loanAccountRepository
                .findByApplication_ApplicationId(applicationId)
                .orElse(null);

        if (account == null) {
            account = LoanAccount.builder()
                    .application(app)
                    .customer(app.getCustomer())
                    .accountNo(generateAccountNo(app))
                    .sanctionAmount(sanctionAmount)
                    .interestRateAnnual(interestRateAnnual)
                    .tenureMonths(tenureMonths)
                    .startDate(LocalDate.now())
                    .status("ACTIVE")
                    .approvedByManager(manager)
                    .build();

            account = loanAccountRepository.save(account);
            createOrRecreateEmiSchedule(account);

        } else {
            account.setSanctionAmount(sanctionAmount);
            account.setInterestRateAnnual(interestRateAnnual);
            account.setTenureMonths(tenureMonths);
            account.setApprovedByManager(manager);

            if (account.getStartDate() == null) {
                account.setStartDate(LocalDate.now());
            }

            account = loanAccountRepository.save(account);
            createOrRecreateEmiSchedule(account);
        }

        LocalDateTime now = LocalDateTime.now();

        app.setStatus(approvedStatus);
        app.setManagerNotes(managerNotes);
        app.setApprovedAt(now);
        app.setRejectedAt(null);

        loanApplicationRepository.save(app);

        String description =
                "Your loan application has been approved by Branch Manager." +
                        " Sanction Amount: " + sanctionAmount +
                        ", Interest Rate: " + interestRateAnnual + "%" +
                        ", Tenure: " + tenureMonths + " months" +
                        ((managerNotes != null && !managerNotes.isBlank()) ? ", Notes: " + managerNotes : "");

        sendCustomerMail(app, "Loan approved", description);

        return true;
    }

    public boolean rejectLoan(Long applicationId, String managerNotes) {
        Optional<LoanApplication> appOpt = loanApplicationRepository.findByApplicationId(applicationId);
        if (appOpt.isEmpty()) return false;

        LoanApplication app = appOpt.get();

        LoanApplicationStatus rejectedStatus = loanApplicationStatusRepository
                .findByStatusCode("REJECTED")
                .orElse(null);

        if (rejectedStatus == null) return false;

        LocalDateTime now = LocalDateTime.now();

        app.setStatus(rejectedStatus);
        app.setManagerNotes(managerNotes);
        app.setRejectedAt(now);
        app.setApprovedAt(null);

        loanApplicationRepository.save(app);

        String description = "Your loan application has been rejected by Branch Manager."
                + ((managerNotes != null && !managerNotes.isBlank()) ? " Reason/Notes: " + managerNotes : "");

        sendCustomerMail(app, "Loan rejected", description);

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

    private String generateAccountNo(LoanApplication app) {
        String appIdPart = app.getApplicationId() != null
                ? String.format("%06d", app.getApplicationId())
                : "000000";

        String timePart = String.valueOf(System.currentTimeMillis());
        if (timePart.length() > 6) {
            timePart = timePart.substring(timePart.length() - 6);
        }

        return "LN-" + appIdPart + "-" + timePart;
    }

    private void createOrRecreateEmiSchedule(LoanAccount loanAccount) {
        emiScheduleRepository.deleteByLoanAccount_LoanAccountId(loanAccount.getLoanAccountId());

        BigDecimal principal = loanAccount.getSanctionAmount();
        BigDecimal annualRate = loanAccount.getInterestRateAnnual();
        Integer months = loanAccount.getTenureMonths();

        BigDecimal emiAmount = calculateEmi(principal, annualRate, months);

        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal balance = principal;

        for (int i = 1; i <= months; i++) {
            BigDecimal interestComponent = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalComponent = emiAmount.subtract(interestComponent).setScale(2, RoundingMode.HALF_UP);

            if (i == months) {
                principalComponent = balance.setScale(2, RoundingMode.HALF_UP);
                emiAmount = principalComponent.add(interestComponent).setScale(2, RoundingMode.HALF_UP);
            }

            EmiSchedule emi = EmiSchedule.builder()
                    .loanAccount(loanAccount)
                    .installmentNo(i)
                    .dueDate(loanAccount.getStartDate().plusMonths(i))
                    .emiAmount(emiAmount)
                    .principalComponent(principalComponent)
                    .interestComponent(interestComponent)
                    .status("PENDING")
                    .build();

            emiScheduleRepository.save(emi);

            balance = balance.subtract(principalComponent).setScale(2, RoundingMode.HALF_UP);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = BigDecimal.ZERO;
            }
        }
    }

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, Integer months) {
        if (principal == null || annualRate == null || months == null || months <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        MathContext mc = new MathContext(20, RoundingMode.HALF_UP);

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate, mc);
        BigDecimal power = onePlusR.pow(months, mc);

        BigDecimal numerator = principal.multiply(monthlyRate, mc).multiply(power, mc);
        BigDecimal denominator = power.subtract(BigDecimal.ONE, mc);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public StaffUsers getCurrentStaffUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return staffUsersRepository.findByEmail(username).orElse(null);
    }


    public boolean sendOfferToCustomer(Long applicationId,
                                       BigDecimal offeredAmount,
                                       BigDecimal interestRateAnnual,
                                       Integer tenureMonths,
                                       String managerNotes) {

        Optional<LoanApplication> appOpt = loanApplicationRepository.findByApplicationId(applicationId);
        if (appOpt.isEmpty()) return false;

        LoanApplication app = appOpt.get();

        if (app.getStatus() == null || app.getStatus().getStatusCode() == null) return false;

        String currentStatus = app.getStatus().getStatusCode();
        if (!"RECOMMENDED".equals(currentStatus) && !"APPROVED".equals(currentStatus)) {
            return false;
        }

        if (offeredAmount == null || offeredAmount.compareTo(BigDecimal.ZERO) <= 0) return false;
        if (interestRateAnnual == null || interestRateAnnual.compareTo(BigDecimal.ZERO) <= 0) return false;
        if (tenureMonths == null || tenureMonths <= 0) return false;

        LoanApplicationStatus offerSentStatus = loanApplicationStatusRepository
                .findByStatusCode("OFFER_SENT_TO_CUSTOMER")
                .orElse(null);

        if (offerSentStatus == null) return false;

        StaffUsers manager = null;
        try {
            manager = getCurrentStaffUser();
        } catch (Exception ignored) {
        }

        LocalDateTime now = LocalDateTime.now();

        app.setOfferedAmount(offeredAmount);
        app.setOfferedInterestRateAnnual(interestRateAnnual);
        app.setOfferedTenureMonths(tenureMonths);
        app.setOfferStatus("PENDING");
        app.setOfferSentAt(now);
        app.setCustomerOfferRespondedAt(null);
        app.setOfferRejectionReason(null);
        app.setManagerNotes(managerNotes);
        app.setOfferSentByManager(manager);
        app.setStatus(offerSentStatus);

        loanApplicationRepository.save(app);

        String description =
                "You requested a loan amount of Rs. " + app.getAmountRequested() +
                        ". Based on our assessment, we can offer Rs. " + offeredAmount +
                        " at " + interestRateAnnual + "% annual interest for " + tenureMonths + " months." +
                        " Please log in to your account and accept or reject this offer." +
                        ((managerNotes != null && !managerNotes.isBlank()) ? " Notes: " + managerNotes : "");

        sendCustomerMail(app, "Loan offer from bank", description);

        return true;
    }


    public boolean acceptOfferByCustomer(Long applicationId, Long customerId) {
        Optional<LoanApplication> appOpt = loanApplicationRepository.findByApplicationId(applicationId);
        if (appOpt.isEmpty()) return false;

        LoanApplication app = appOpt.get();

        if (app.getCustomer() == null || app.getCustomer().getCustomerId() == null) return false;
        if (!app.getCustomer().getCustomerId().equals(customerId)) return false;

        if (app.getStatus() == null || !"OFFER_SENT_TO_CUSTOMER".equals(app.getStatus().getStatusCode())) {
            return false;
        }

        if (app.getOfferedAmount() == null || app.getOfferedInterestRateAnnual() == null || app.getOfferedTenureMonths() == null) {
            return false;
        }

        LoanApplicationStatus approvedStatus = loanApplicationStatusRepository
                .findByStatusCode("APPROVED")
                .orElse(null);

        if (approvedStatus == null) return false;

        LoanAccount account = loanAccountRepository
                .findByApplication_ApplicationId(applicationId)
                .orElse(null);

        if (account == null) {
            account = LoanAccount.builder()
                    .application(app)
                    .customer(app.getCustomer())
                    .accountNo(generateAccountNo(app))
                    .sanctionAmount(app.getOfferedAmount())
                    .interestRateAnnual(app.getOfferedInterestRateAnnual())
                    .tenureMonths(app.getOfferedTenureMonths())
                    .startDate(LocalDate.now())
                    .status("ACTIVE")
                    .approvedByManager(app.getOfferSentByManager())
                    .build();

            account = loanAccountRepository.save(account);
        } else {
            account.setSanctionAmount(app.getOfferedAmount());
            account.setInterestRateAnnual(app.getOfferedInterestRateAnnual());
            account.setTenureMonths(app.getOfferedTenureMonths());
            account.setApprovedByManager(app.getOfferSentByManager());

            if (account.getStartDate() == null) {
                account.setStartDate(LocalDate.now());
            }

            account = loanAccountRepository.save(account);
        }

        createOrRecreateEmiSchedule(account);

        LocalDateTime now = LocalDateTime.now();

        app.setOfferStatus("ACCEPTED");
        app.setCustomerOfferRespondedAt(now);
        app.setApprovedAt(now);
        app.setRejectedAt(null);
        app.setStatus(approvedStatus);

        loanApplicationRepository.save(app);

        String description =
                "You accepted the bank offer. Your loan is now approved for Rs. " +
                        app.getOfferedAmount() + " at " + app.getOfferedInterestRateAnnual() +
                        "% annual interest for " + app.getOfferedTenureMonths() + " months.";

        sendCustomerMail(app, "Offer accepted and loan approved", description);

        return true;
    }


    public boolean rejectOfferByCustomer(Long applicationId, Long customerId, String rejectReason) {
        Optional<LoanApplication> appOpt = loanApplicationRepository.findByApplicationId(applicationId);
        if (appOpt.isEmpty()) return false;

        LoanApplication app = appOpt.get();

        if (app.getCustomer() == null || app.getCustomer().getCustomerId() == null) return false;
        if (!app.getCustomer().getCustomerId().equals(customerId)) return false;

        if (app.getStatus() == null || !"OFFER_SENT_TO_CUSTOMER".equals(app.getStatus().getStatusCode())) {
            return false;
        }

        LoanApplicationStatus rejectedOfferStatus = loanApplicationStatusRepository
                .findByStatusCode("CUSTOMER_REJECTED_OFFER")
                .orElse(null);

        if (rejectedOfferStatus == null) return false;

        app.setOfferStatus("REJECTED");
        app.setCustomerOfferRespondedAt(LocalDateTime.now());
        app.setOfferRejectionReason(rejectReason);
        app.setApprovedAt(null);
        app.setStatus(rejectedOfferStatus);

        loanApplicationRepository.save(app);

        String description =
                "You rejected the bank offer. " +
                        "Offered Amount: Rs. " + app.getOfferedAmount() +
                        ", Interest Rate: " + app.getOfferedInterestRateAnnual() + "%" +
                        ", Tenure: " + app.getOfferedTenureMonths() + " months" +
                        ((rejectReason != null && !rejectReason.isBlank()) ? ", Rejection Reason: " + rejectReason : "");

        sendCustomerMail(app, "Offer rejected", description);

        return true;
    }


}