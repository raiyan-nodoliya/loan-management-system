package com.bank.LMS.Controller.officer;

import com.bank.LMS.Entity.LoanApplication;
import com.bank.LMS.Entity.RiskAssessment;
import com.bank.LMS.Entity.StaffUsers;
import com.bank.LMS.Repository.StaffUsersRepository;
import com.bank.LMS.Service.officer.RiskOfficerService;
import com.bank.LMS.Service.officer.StaffPermissionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/risk")
public class RiskOfficerDashboardController {

    private final RiskOfficerService riskOfficerService;
    private final StaffUsersRepository staffUsersRepository;
    private final StaffPermissionService staffPermissionService;

    public RiskOfficerDashboardController(RiskOfficerService riskOfficerService,
                                          StaffUsersRepository staffUsersRepository,
                                          StaffPermissionService staffPermissionService) {
        this.riskOfficerService = riskOfficerService;
        this.staffUsersRepository = staffUsersRepository;
        this.staffPermissionService = staffPermissionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal, HttpSession session) {

        if (principal != null) {
            String username = principal.getName();
            StaffUsers user = staffUsersRepository.findByEmail(username).orElse(null);
            model.addAttribute("loggedInName", user != null ? user.getName() : username);
            model.addAttribute("canViewReports", staffPermissionService.canViewReports(username));
        }

        Object toastMsg = session.getAttribute("toastMessage");
        Object toastType = session.getAttribute("toastType");

        if (toastMsg != null) {
            model.addAttribute("toastMessage", toastMsg);
            model.addAttribute("toastType", toastType);
            session.removeAttribute("toastMessage");
            session.removeAttribute("toastType");
        }

        model.addAttribute("pendingEvaluation", riskOfficerService.pendingEvaluationCount());
        model.addAttribute("highRiskCases", riskOfficerService.highRiskCount());
        model.addAttribute("completedToday", riskOfficerService.completedTodayCount());
        model.addAttribute("applications", riskOfficerService.riskQueue());

        return "risk/dashboard";
    }

    @GetMapping("/evaluate/{id}")
    public String evaluatePage(@PathVariable Long id,
                               Model model,
                               RedirectAttributes ra,
                               Principal principal) {

        LoanApplication app = riskOfficerService.getApplication(id);
        if (app == null) {
            ra.addFlashAttribute("toastMessage", "Application not found");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/risk/dashboard";
        }

        if (principal != null) {
            String username = principal.getName();
            StaffUsers user = staffUsersRepository.findByEmail(username).orElse(null);
            model.addAttribute("loggedInName", user != null ? user.getName() : username);
        }

        RiskAssessment existingAssessment = riskOfficerService.getExistingAssessment(id);

        BigDecimal monthlyIncome = app.getMonthlyIncome() != null ? app.getMonthlyIncome() : BigDecimal.ZERO;
        BigDecimal existingEmis = app.getExistingEmis() != null ? app.getExistingEmis() : BigDecimal.ZERO;
        BigDecimal foir = riskOfficerService.calculateFoir(monthlyIncome, existingEmis);

        model.addAttribute("app", app);
        model.addAttribute("existingAssessment", existingAssessment);
        model.addAttribute("foir", foir);

        return "risk/evaluate_application";
    }

    @PostMapping("/evaluate/{id}")
    public String submitEvaluation(@PathVariable Long id,
                                   @RequestParam BigDecimal monthlyIncome,
                                   @RequestParam(defaultValue = "0") BigDecimal existingEmis,
                                   @RequestParam String recommendation,
                                   @RequestParam(required = false) String notes,
                                   RedirectAttributes ra) {

        boolean ok = riskOfficerService.submitEvaluation(id, monthlyIncome, existingEmis, recommendation, notes);

        ra.addFlashAttribute("toastMessage",
                ok ? "Risk evaluation submitted successfully" : "Failed to submit risk evaluation");
        ra.addFlashAttribute("toastType", ok ? "success" : "error");

        return ok ? "redirect:/risk/dashboard" : "redirect:/risk/evaluate/" + id;
    }
}