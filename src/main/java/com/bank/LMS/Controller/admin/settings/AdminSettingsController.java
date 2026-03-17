package com.bank.LMS.Controller.admin.settings;

import com.bank.LMS.Service.admin.settings.LoanApplicationStatusService;
import com.bank.LMS.Service.admin.settings.LoanTypeService;
import com.bank.LMS.Service.admin.settings.RiskRecommendationStatusService;
import com.bank.LMS.Service.admin.settings.RoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    private final LoanTypeService loanTypeService;
    private final LoanApplicationStatusService loanStatusService;
    private final RiskRecommendationStatusService riskRecStatusService;
    private final RoleService roleService;

    public AdminSettingsController(LoanTypeService loanTypeService,
                                   LoanApplicationStatusService loanStatusService,
                                   RiskRecommendationStatusService riskRecStatusService,
                                   RoleService roleService) {
        this.loanTypeService = loanTypeService;
        this.loanStatusService = loanStatusService;
        this.riskRecStatusService = riskRecStatusService;
        this.roleService = roleService;
    }

    @GetMapping
    public String settings(@RequestParam(defaultValue = "loan-types") String tab,
                           Model model) {

        model.addAttribute("activeTab", tab);

        if ("loan-types".equals(tab)) {
            model.addAttribute("loanTypes", loanTypeService.getAll());
        } else if ("loan-application-statuses".equals(tab)) {
            model.addAttribute("statuses", loanStatusService.listAll());
        } else if ("risk-recommendation-statuses".equals(tab)) {
            model.addAttribute("statuses", riskRecStatusService.listAll());
        } else if ("roles".equals(tab)) {
            model.addAttribute("roles", roleService.listAll());
        } else {
            model.addAttribute("loanTypes", loanTypeService.getAll());
            model.addAttribute("activeTab", "loan-types");
        }

        return "admin/settings/settings";
    }

    // ===============================
    // LOAN TYPES
    // ===============================

    @PostMapping("/loan-types")
    public String createLoanType(@RequestParam String loanTypeCode,
                                 @RequestParam String loanTypeName,
                                 RedirectAttributes ra) {
        try {
            String code = normalizeCode(loanTypeCode, "Loan type code");
            String name = normalizeLabel(loanTypeName, "Loan type name");

            loanTypeService.create(code, name);
            ra.addFlashAttribute("success", "Loan type saved successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?tab=loan-types";
    }

    @PostMapping("/loan-types/{id}/toggle")
    public String toggleLoanType(@PathVariable Long id, RedirectAttributes ra) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid loan type id");
            }

            loanTypeService.toggle(id);
            ra.addFlashAttribute("success", "Loan type updated successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?tab=loan-types";
    }

    // ===============================
    // LOAN APPLICATION STATUS
    // ===============================

    @PostMapping("/loan-application-statuses")
    public String createLoanStatus(@RequestParam String statusCode,
                                   @RequestParam String label,
                                   RedirectAttributes ra) {
        try {
            String code = normalizeCode(statusCode, "Status code");
            String safeLabel = normalizeLabel(label, "Label");

            loanStatusService.create(code, safeLabel);
            ra.addFlashAttribute("success", "Status created successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?tab=loan-application-statuses";
    }

    @PostMapping("/loan-application-statuses/{id}/toggle")
    public String toggleLoanStatus(@PathVariable Long id,
                                   RedirectAttributes ra) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid status id");
            }

            loanStatusService.toggle(id);
            ra.addFlashAttribute("success", "Status updated successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?tab=loan-application-statuses";
    }

    // ===============================
    // RISK RECOMMENDATION STATUS
    // ===============================

    @PostMapping("/risk-recommendation-statuses")
    public String createRiskRecStatus(@RequestParam String statusCode,
                                      @RequestParam String label,
                                      RedirectAttributes ra) {
        try {
            String code = normalizeCode(statusCode, "Status code");
            String safeLabel = normalizeLabel(label, "Label");

            riskRecStatusService.create(code, safeLabel);
            ra.addFlashAttribute("success", "Recommendation status created successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?tab=risk-recommendation-statuses";
    }

    @PostMapping("/risk-recommendation-statuses/{id}/toggle")
    public String toggleRiskRecStatus(@PathVariable Long id,
                                      RedirectAttributes ra) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid recommendation status id");
            }

            riskRecStatusService.toggle(id);
            ra.addFlashAttribute("success", "Recommendation status updated successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?tab=risk-recommendation-statuses";
    }

    // ===============================
    // ROLES
    // ===============================

    @PostMapping("/roles")
    public String createRole(@RequestParam String roleName,
                             RedirectAttributes ra) {
        try {
            String name = normalizeCode(roleName, "Role name");
            roleService.createRole(name);
            ra.addFlashAttribute("success", "Role created successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?tab=roles";
    }

    @PostMapping("/roles/{id}/toggle")
    public String toggleRole(@PathVariable Long id,
                             RedirectAttributes ra) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid role id");
            }

            roleService.toggleActive(id);
            ra.addFlashAttribute("success", "Role updated successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?tab=roles";
    }

    // ===============================
    // HELPERS
    // ===============================

    private String normalizeCode(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String normalized = value.trim()
                .replaceAll("\\s+", "_")
                .toUpperCase();

        if (!normalized.matches("^[A-Z0-9_]+$")) {
            throw new IllegalArgumentException(fieldName + " must contain only letters, numbers, and underscore");
        }

        if (normalized.length() > 50) {
            throw new IllegalArgumentException(fieldName + " must not exceed 50 characters");
        }

        return normalized;
    }

    private String normalizeLabel(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String normalized = value.trim().replaceAll("\\s+", " ");

        if (normalized.length() > 100) {
            throw new IllegalArgumentException(fieldName + " must not exceed 100 characters");
        }

        return normalized;
    }
}