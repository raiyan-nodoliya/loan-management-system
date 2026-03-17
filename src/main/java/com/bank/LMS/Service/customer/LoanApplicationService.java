package com.bank.LMS.Service.customer;

import com.bank.LMS.Entity.*;
import com.bank.LMS.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository appRepo;
    private final CustomerRepository customerRepo;
    private final LoanTypeRepository loanTypeRepo;
    private final LoanDocumentRepository docRepo;
    private final LoanDocumentStorageService storage;
    private final LoanApplicationStatusRepository loanStatusRepo;

    public LoanApplicationService(LoanApplicationRepository appRepo,
                                  CustomerRepository customerRepo,
                                  LoanTypeRepository loanTypeRepo,
                                  LoanDocumentRepository docRepo,
                                  LoanDocumentStorageService storage,
                                  LoanApplicationStatusRepository loanStatusRepo) {
        this.appRepo = appRepo;
        this.customerRepo = customerRepo;
        this.loanTypeRepo = loanTypeRepo;
        this.docRepo = docRepo;
        this.storage = storage;
        this.loanStatusRepo = loanStatusRepo;
    }

    // =========================================================
    // STEP-1: Draft create/load
    // =========================================================
    @Transactional
    public LoanApplication createOrLoadDraft(Long customerId, Long existingAppId) {

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        if (existingAppId != null) {
            LoanApplication existing = appRepo.findById(existingAppId).orElse(null);
            if (existing != null
                    && existing.getCustomer() != null
                    && existing.getCustomer().getCustomerId().equals(customerId)) {
                return existing;
            }
        }

        LoanApplication draft = new LoanApplication();
        draft.setCustomer(customer);
        draft.setApplicationNo(generateApplicationNo());

        LoanApplicationStatus draftStatus = loanStatusRepo.findByStatusCode("DRAFT")
                .orElseThrow(() -> new IllegalStateException("DRAFT status missing"));
        draft.setStatus(draftStatus);

        // IMPORTANT:
        // DB me agar ye columns NOT NULL hain, to draft create time par safe default do
        draft.setAmountRequested(BigDecimal.ZERO);
        draft.setTenureMonthsRequested(0);
        draft.setMonthlyIncome(BigDecimal.ZERO);

        // Optional text fields
        draft.setPurpose("");
        draft.setEmployerName("");
        draft.setDesignation("");
        draft.setExperienceYears(0);

        return appRepo.save(draft);
    }

    // =========================================================
    // STEP-1: Update Customer personal
    // =========================================================
    @Transactional
    public void updateCustomerPersonal(Long customerId,
                                       String name,
                                       String phone,
                                       LocalDate dob,
                                       String address) {

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        if (name != null && !name.isBlank()) customer.setName(name);
        if (phone != null && !phone.isBlank()) customer.setPhone(phone);
        if (dob != null) customer.setDob(dob);
        if (address != null && !address.isBlank()) customer.setAddress(address);

        customerRepo.save(customer);
    }

    // =========================================================
    // STEP-2: Save Loan details
    // =========================================================
    @Transactional
    public void saveLoanDetails(Long appId,
                                Long loanTypeId,
                                BigDecimal amountRequested,
                                Integer tenureMonthsRequested,
                                String purpose) {

        LoanApplication app = appRepo.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + appId));

        LoanType loanType = loanTypeRepo.findById(loanTypeId)
                .orElseThrow(() -> new IllegalArgumentException("LoanType not found: " + loanTypeId));

        app.setLoanType(loanType);
        app.setAmountRequested(amountRequested);
        app.setTenureMonthsRequested(tenureMonthsRequested);
        app.setPurpose(purpose);

        appRepo.save(app);
    }

    // =========================================================
    // STEP-3: Save income/employment
    // =========================================================
    @Transactional
    public void saveIncomeEmployment(Long appId,
                                     String employerName,
                                     String designation,
                                     BigDecimal monthlyIncome,
                                     Integer experienceYears) {

        LoanApplication app = appRepo.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + appId));

        app.setEmployerName(employerName);
        app.setDesignation(designation);
        app.setMonthlyIncome(monthlyIncome);
        app.setExperienceYears(experienceYears);

        appRepo.save(app);
    }

    // =========================================================
    // STEP-4: Upload document
    // =========================================================
    @Transactional
    public void uploadDocument(Long appId, String docType, MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) return;

        LoanApplication app = appRepo.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + appId));

        Customer customer = app.getCustomer();
        if (customer == null) throw new IllegalStateException("Application has no customer.");

        var stored = storage.store(file, app.getApplicationNo(), docType);

        int nextVersion = 1;
        var lastLatest = docRepo
                .findTopByApplication_ApplicationIdAndDocumentTypeAndIsLatestTrueOrderByVersionNoDesc(appId, docType);

        if (lastLatest.isPresent()) {
            Integer lastV = lastLatest.get().getVersionNo();
            nextVersion = (lastV == null ? 1 : lastV + 1);
            docRepo.markOldLatestFalse(appId, docType);
        }

        LoanDocument doc = LoanDocument.builder()
                .application(app)
                .customer(customer)
                .documentType(docType)
                .fileName(stored.fileName())
                .filePath(stored.filePath())
                .originalFileName(stored.originalFileName())
                .mimeType(stored.mimeType())
                .fileSizeBytes(stored.sizeBytes())
                .versionNo(nextVersion)
                .isLatest(true)
                .status(LoanDocument.DocumentStatus.UPLOADED)
                .build();

        docRepo.save(doc);
    }

    // =========================================================
    // FINAL: Submit application
    // =========================================================
    @Transactional
    public void submitApplication(Long appId) {
        try {
            LoanApplication app = appRepo.findById(appId)
                    .orElseThrow(() -> new IllegalArgumentException("Application not found: " + appId));

            validateBeforeSubmit(app);

            LoanApplicationStatus submittedStatus = loanStatusRepo.findByStatusCode("SUBMITTED")
                    .orElseThrow(() -> new IllegalStateException("SUBMITTED status missing"));

            app.setStatus(submittedStatus);
            app.setSubmittedAt(LocalDateTime.now());

            appRepo.saveAndFlush(app);

        } catch (Exception e) {
            System.out.println("SUBMIT FAILED: " + e.getClass().getName() + " -> " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // =========================================================
    // VALIDATION BEFORE SUBMIT
    // =========================================================
    private void validateBeforeSubmit(LoanApplication app) {
        if (app.getLoanType() == null) {
            throw new IllegalStateException("Loan type is required");
        }

        if (app.getAmountRequested() == null || app.getAmountRequested().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Amount requested must be greater than 0");
        }

        if (app.getTenureMonthsRequested() == null || app.getTenureMonthsRequested() <= 0) {
            throw new IllegalStateException("Tenure months is required");
        }

        if (app.getMonthlyIncome() == null || app.getMonthlyIncome().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Monthly income must be greater than 0");
        }
    }

    // =========================================================
    // Utility
    // =========================================================
    private String generateApplicationNo() {
        String date = java.time.LocalDate.now().toString().replace("-", "");
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "LA-" + date + "-" + rand;
    }
}