package com.bank.LMS.Controller.common_reports;

import com.bank.LMS.Service.admin.AdminReportService;
import com.bank.LMS.Service.officer.StaffPermissionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final AdminReportService adminReportService;
    private final StaffPermissionService staffPermissionService;

    public ReportController(AdminReportService adminReportService,
                            StaffPermissionService staffPermissionService) {
        this.adminReportService = adminReportService;
        this.staffPermissionService = staffPermissionService;
    }

    @GetMapping("/applications")
    public String applicationReports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerCity,
            @RequestParam(required = false) String ageRange,
            @RequestParam(required = false) String salaryRange,
            @RequestParam(required = false) String amountRange,
            @RequestParam(required = false) String loanTypeName,
            @RequestParam(required = false) String gender,
            Authentication authentication,
            RedirectAttributes ra,
            Model model
    ) {
        String email = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean canView = isAdmin || staffPermissionService.canViewReports(email);
        boolean canExport = isAdmin || staffPermissionService.canExportReports(email);

        if (!canView) {
            ra.addFlashAttribute("toastMessage", "You do not have permission to view reports");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        String headerRole = isAdmin ? "ADMIN" : "STAFF";
        model.addAttribute("headerRole", headerRole);

        model.addAttribute("applications",
                adminReportService.getFilteredApplications(
                        fromDate, toDate, status, customerName,
                        customerCity, ageRange, salaryRange, amountRange,
                        loanTypeName, gender
                ));

        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("status", status);
        model.addAttribute("customerName", customerName);
        model.addAttribute("customerCity", customerCity);
        model.addAttribute("ageRange", ageRange);
        model.addAttribute("salaryRange", salaryRange);
        model.addAttribute("amountRange", amountRange);
        model.addAttribute("loanTypeName", loanTypeName);
        model.addAttribute("gender", gender);
        model.addAttribute("canExport", canExport);

        return "admin/common_reports/application_reports";
    }

    @GetMapping("/applications/export/pdf")
    public void exportApplicationsPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerCity,
            @RequestParam(required = false) String ageRange,
            @RequestParam(required = false) String salaryRange,
            @RequestParam(required = false) String amountRange,
            @RequestParam(required = false) String loanTypeName,
            @RequestParam(required = false) String gender,
            Authentication authentication,
            HttpServletResponse response
    ) throws Exception {

        String email = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean canExport = isAdmin || staffPermissionService.canExportReports(email);

        if (!canExport) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to export reports");
            return;
        }

        var applications = adminReportService.getFilteredApplications(
                fromDate, toDate, status, customerName,
                customerCity, ageRange, salaryRange, amountRange,
                loanTypeName, gender
        );

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=loan_applications_report.pdf");

        adminReportService.exportApplicationsToPdf(
                applications, fromDate, toDate, status, customerName,
                customerCity, ageRange, salaryRange, amountRange,
                loanTypeName, gender,
                response.getOutputStream()
        );
    }

    @GetMapping("/applications/export/excel")
    public void exportApplicationsExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerCity,
            @RequestParam(required = false) String ageRange,
            @RequestParam(required = false) String salaryRange,
            @RequestParam(required = false) String amountRange,
            @RequestParam(required = false) String loanTypeName,
            @RequestParam(required = false) String gender,
            Authentication authentication,
            HttpServletResponse response
    ) throws Exception {

        String email = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean canExport = isAdmin || staffPermissionService.canExportReports(email);

        if (!canExport) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to export reports");
            return;
        }

        var applications = adminReportService.getFilteredApplications(
                fromDate, toDate, status, customerName,
                customerCity, ageRange, salaryRange, amountRange,
                loanTypeName, gender
        );

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=loan_applications_report.xlsx");

        adminReportService.exportApplicationsToExcel(
                applications, fromDate, toDate, status, customerName,
                customerCity, ageRange, salaryRange, amountRange,
                loanTypeName, gender,
                response.getOutputStream()
        );
    }
}