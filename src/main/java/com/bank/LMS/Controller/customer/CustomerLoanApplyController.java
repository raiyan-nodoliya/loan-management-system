package com.bank.LMS.Controller.customer;

import com.bank.LMS.Entity.Customer;
import com.bank.LMS.Repository.CustomerRepository;
import com.bank.LMS.Repository.LoanTypeRepository;
import com.bank.LMS.Service.customer.LoanApplicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/apply")
public class CustomerLoanApplyController {

    private final LoanApplicationService loanService;
    private final LoanTypeRepository loanTypeRepo;
    private final CustomerRepository customerRepo;

    public CustomerLoanApplyController(LoanApplicationService loanService,
                                       LoanTypeRepository loanTypeRepo,
                                       CustomerRepository customerRepo) {
        this.loanService = loanService;
        this.loanTypeRepo = loanTypeRepo;
        this.customerRepo = customerRepo;
    }

    // ✅ CUSTOMER session based (because your customer login is custom, not Spring Security)
    private Customer getLoggedCustomer(HttpSession session) {
        Long customerId = (Long) session.getAttribute("CUSTOMER_ID");
        if (customerId == null) return null;

        return customerRepo.findById(customerId).orElse(null);
    }

    // STEP 1
    @GetMapping("/step1_personal")
    public String step1(HttpSession session, Model model) {
        Customer customer = getLoggedCustomer(session);
        if (customer == null) return "redirect:/customer_login";

        Long appId = (Long) session.getAttribute("loanAppId");
        var app = loanService.createOrLoadDraft(customer.getCustomerId(), appId);
        session.setAttribute("loanAppId", app.getApplicationId());

        model.addAttribute("customer", customer);

        // ✅ FIXED
        return "customer/apply/step1_personal";
    }

    @PostMapping("/step1_personal")
    public String step1Save(HttpSession session,
                            @RequestParam String name,
                            @RequestParam(required = false) String phone,
                            @RequestParam(required = false) LocalDate dob,
                            @RequestParam(required = false) String address) {

        Customer customer = getLoggedCustomer(session);
        if (customer == null) return "redirect:/customer_login";

        loanService.updateCustomerPersonal(customer.getCustomerId(), name, phone, dob, address);
        return "redirect:/apply/step2_loan";
    }

    //STEP-2
    @GetMapping("/step2_loan")
    public String step2(HttpSession session, Model model) {

        Customer customer = getLoggedCustomer(session);
        if (customer == null) return "redirect:/customer_login";

        Long appId = (Long) session.getAttribute("loanAppId");
        if (appId == null) return "redirect:/apply/step1_personal";

        var list = loanTypeRepo.findByActiveTrueOrderByLoanTypeNameAsc();
        model.addAttribute("loanTypes", (list == null) ? java.util.List.of() : list);

        return "customer/apply/step2_loan";
    }

    @PostMapping("/step2_loan")
    public String step2Save(HttpSession session,
                            @RequestParam(required = false) Long loanTypeId,
                            @RequestParam(required = false) BigDecimal amountRequested,
                            @RequestParam(required = false) Integer tenureMonthsRequested,
                            @RequestParam(required = false) String purpose,
                            Model model) {

        Customer customer = getLoggedCustomer(session);
        if (customer == null) return "redirect:/customer_login";

        Long appId = (Long) session.getAttribute("loanAppId");
        if (appId == null) return "redirect:/apply/step1_personal";

        boolean hasError = false;

        var list = loanTypeRepo.findByActiveTrueOrderByLoanTypeNameAsc();
        model.addAttribute("loanTypes", (list == null) ? java.util.List.of() : list);

        model.addAttribute("loanTypeId", loanTypeId);
        model.addAttribute("amountRequested", amountRequested);
        model.addAttribute("tenureMonthsRequested", tenureMonthsRequested);
        model.addAttribute("purpose", purpose);

        if (loanTypeId == null) {
            model.addAttribute("loanTypeIdError", "Please select loan type");
            hasError = true;
        }

        if (amountRequested == null) {
            model.addAttribute("amountRequestedError", "Loan amount is required");
            hasError = true;
        } else if (amountRequested.compareTo(BigDecimal.ONE) < 0) {
            model.addAttribute("amountRequestedError", "Loan amount must be greater than 0");
            hasError = true;
        }

        if (tenureMonthsRequested == null) {
            model.addAttribute("tenureMonthsRequestedError", "Tenure is required");
            hasError = true;
        } else if (tenureMonthsRequested <= 0) {
            model.addAttribute("tenureMonthsRequestedError", "Tenure must be at least 1 month");
            hasError = true;
        }

        if (purpose != null && purpose.length() > 500) {
            model.addAttribute("purposeError", "Purpose must be under 500 characters");
            hasError = true;
        }

        if (hasError) {
            return "customer/apply/step2_loan";
        }

        loanService.saveLoanDetails(appId, loanTypeId, amountRequested, tenureMonthsRequested, purpose);
        return "redirect:/apply/step3_income";
    }

    // STEP 3
    @GetMapping("/step3_income")
    public String step3(HttpSession session) {
        Customer customer = getLoggedCustomer(session);
        if (customer == null) return "redirect:/customer_login";

        if (session.getAttribute("loanAppId") == null) return "redirect:/apply/step1_personal";

        // ✅ FIXED
        return "customer/apply/step3_income";
    }

    @PostMapping("/step3_income")
    public String step3Save(HttpSession session,
                            @RequestParam(required = false) String employerName,
                            @RequestParam(required = false) String designation,
                            @RequestParam(required = false) BigDecimal monthlyIncome,
                            @RequestParam(required = false) Integer experienceYears,
                            Model model) {

        Customer customer = getLoggedCustomer(session);
        if (customer == null) return "redirect:/customer_login";

        Long appId = (Long) session.getAttribute("loanAppId");
        if (appId == null) return "redirect:/apply/step1_personal";

        boolean hasError = false;

        model.addAttribute("employerName", employerName);
        model.addAttribute("designation", designation);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("experienceYears", experienceYears);

        if (monthlyIncome == null) {
            model.addAttribute("monthlyIncomeError", "Monthly income is required");
            hasError = true;
        } else if (monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("monthlyIncomeError", "Monthly income must be greater than 0");
            hasError = true;
        }

        if (experienceYears != null && experienceYears < 0) {
            model.addAttribute("experienceYearsError", "Experience cannot be negative");
            hasError = true;
        }

        if (employerName != null && employerName.length() > 100) {
            model.addAttribute("employerNameError", "Employer name is too long");
            hasError = true;
        }

        if (designation != null && designation.length() > 100) {
            model.addAttribute("designationError", "Designation is too long");
            hasError = true;
        }

        if (hasError) {
            return "customer/apply/step3_income";
        }

        loanService.saveIncomeEmployment(appId, employerName, designation, monthlyIncome, experienceYears);
        return "redirect:/apply/step4_documents";
    }

    // STEP 4
    @GetMapping("/step4_documents")
    public String step4(HttpSession session) {
        Customer customer = getLoggedCustomer(session);
        if (customer == null) return "redirect:/customer_login";

        if (session.getAttribute("loanAppId") == null) return "redirect:/apply/step1_personal";

        // ✅ FIXED
        return "customer/apply/step4_documents";
    }

    @PostMapping("/step4_documents")
    public String submit(HttpSession session,
                         @RequestParam(required = false) MultipartFile panFile,
                         @RequestParam(required = false) MultipartFile aadhaarFile,
                         @RequestParam(required = false) MultipartFile incomeProofFile,
                         @RequestParam(required = false) MultipartFile bankStmtFile,
                         @RequestParam(required = false) MultipartFile addressProofFile,
                         RedirectAttributes ra,
                         Model model) {

        Customer customer = getLoggedCustomer(session);
        if (customer == null) return "redirect:/customer_login";

        Long appId = (Long) session.getAttribute("loanAppId");
        if (appId == null) return "redirect:/apply/step1_personal";

        boolean hasError = false;

        if (panFile == null || panFile.isEmpty()) {
            model.addAttribute("panFileError", "PAN file is required");
            hasError = true;
        }

        if (aadhaarFile == null || aadhaarFile.isEmpty()) {
            model.addAttribute("aadhaarFileError", "Aadhaar file is required");
            hasError = true;
        }

        if (incomeProofFile == null || incomeProofFile.isEmpty()) {
            model.addAttribute("incomeProofFileError", "Income proof is required");
            hasError = true;
        }

        if (bankStmtFile == null || bankStmtFile.isEmpty()) {
            model.addAttribute("bankStmtFileError", "Bank statement is required");
            hasError = true;
        }

        if (addressProofFile == null || addressProofFile.isEmpty()) {
            model.addAttribute("addressProofFileError", "Address proof is required");
            hasError = true;
        }

        if (hasError) {
            return "customer/apply/step4_documents";
        }

        try {
            loanService.uploadDocument(appId, "PAN", panFile);
            loanService.uploadDocument(appId, "AADHAAR", aadhaarFile);
            loanService.uploadDocument(appId, "INCOME_PROOF", incomeProofFile);
            loanService.uploadDocument(appId, "BANK_STATEMENT", bankStmtFile);
            loanService.uploadDocument(appId, "ADDRESS_PROOF", addressProofFile);

            loanService.submitApplication(appId);

            session.removeAttribute("loanAppId");

            ra.addFlashAttribute("toastMessage", "Application submitted successfully!");
            ra.addFlashAttribute("toastType", "success");
            return "redirect:/customer/dashboard";

        } catch (Exception e) {
            model.addAttribute("panFileError", e.getMessage());
            return "customer/apply/step4_documents";
        }
    }


}