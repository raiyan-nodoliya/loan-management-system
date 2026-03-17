package com.bank.LMS.Controller.admin;

import com.bank.LMS.Repository.AuditLogRepository;
import com.bank.LMS.Service.admin.AdminDashboardService;
import com.bank.LMS.Service.admin.AdminReportService;
import com.bank.LMS.Service.admin.AdminStaffPermissionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminDashboardService adminDashboardService;
    private final AuditLogRepository auditLogRepository;
    private final AdminReportService adminReportService;
    private final AdminStaffPermissionService adminStaffPermissionService;

    public AdminController(AdminDashboardService adminDashboardService,
                           AuditLogRepository auditLogRepository,
                           AdminReportService adminReportService,
                           AdminStaffPermissionService adminStaffPermissionService) {
        this.adminDashboardService = adminDashboardService;
        this.auditLogRepository = auditLogRepository;
        this.adminReportService = adminReportService;
        this.adminStaffPermissionService = adminStaffPermissionService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String adminDashboard(Model model) {
        model.addAttribute("totalOfficers", adminDashboardService.totalStaffUsers());
        model.addAttribute("rulesActive", adminDashboardService.rulesActive());
        model.addAttribute("pendingRequests", adminDashboardService.pendingRequests());
        model.addAttribute("recentLogs", auditLogRepository.findTop10ByOrderByCreatedAtDesc());

        return "admin/dashboard";
    }

    @GetMapping("/audit_logs")
    public String auditLogs(Model model) {
        model.addAttribute("recentLogs", auditLogRepository.findAll());
        return "admin/audit_logs";
    }

    @GetMapping("/branches")
    public String branches() {
        return "admin/branches";
    }

    @GetMapping("/emi_config")
    public String emiConfig() {
        return "admin/emi_config";
    }

    @GetMapping("/staff_permissions")
    public String staffPermissions(Model model) {
        model.addAttribute("staffUsers", adminDashboardService.allStaffUsers());
        return "admin/staff_permissions";
    }

    @PostMapping("/staff/{id}/report-permission")
    public String updateReportPermission(@PathVariable("id") Long staffId,
                                         @RequestParam(defaultValue = "false") boolean reportViewEnabled,
                                         @RequestParam(defaultValue = "false") boolean reportExportEnabled,
                                         RedirectAttributes ra) {

        adminStaffPermissionService.updateReportPermissions(staffId, reportViewEnabled, reportExportEnabled);

        ra.addFlashAttribute("toastMessage", "Report permissions updated successfully");
        ra.addFlashAttribute("toastType", "success");

        return "redirect:/admin/staff_permissions";
    }


}