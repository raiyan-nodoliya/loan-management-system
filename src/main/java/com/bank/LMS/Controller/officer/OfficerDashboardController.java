package com.bank.LMS.Controller.officer;

import com.bank.LMS.Entity.LoanApplication;
import com.bank.LMS.Service.officer.OfficerReviewService;
import com.bank.LMS.Service.officer.StaffPermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/officer")
public class OfficerDashboardController {

    private final OfficerReviewService service;
    private final StaffPermissionService staffPermissionService;

    public OfficerDashboardController(OfficerReviewService service,
                                      StaffPermissionService staffPermissionService) {
        this.service = service;
        this.staffPermissionService = staffPermissionService;
    }

    @GetMapping("/dashboard")
    public String officerDashboard(Model model, Principal principal) {
        model.addAttribute("applications", service.queue());
        model.addAttribute("submittedCount", service.countSubmitted());
        model.addAttribute("needsInfoCount", service.countNeedsInfo());
        model.addAttribute("inReviewCount", service.countInReview());

        String email = principal != null ? principal.getName() : null;
        model.addAttribute("canViewReports", staffPermissionService.canViewReports(email));

        return "officer/dashboard";
    }

    @GetMapping("/application/{id}")
    public String reviewApplication(@PathVariable("id") Long id,
                                    Model model,
                                    RedirectAttributes ra) {

        LoanApplication app = service.getApp(id);
        if (app == null) {
            ra.addFlashAttribute("toastMessage", "Application not found");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/officer/dashboard";
        }

        model.addAttribute("app", app);
        model.addAttribute("documents", service.getDocs(id));

        return "officer/review_application";
    }

    @PostMapping("/application/{id}/mark-in-review")
    public String markInReview(@PathVariable("id") Long id,
                               @RequestParam(value = "officerNotes", required = false) String officerNotes,
                               RedirectAttributes ra) {

        boolean ok = service.markInReview(id, officerNotes);

        ra.addFlashAttribute("toastMessage", ok ? "Application marked as In Review" : "Failed to mark application as In Review");
        ra.addFlashAttribute("toastType", ok ? "success" : "error");

        return "redirect:/officer/application/" + id;
    }

    @PostMapping("/application/{id}/request-info")
    public String requestInfo(@PathVariable("id") Long id,
                              @RequestParam("needsInfoMessage") String needsInfoMessage,
                              @RequestParam(value = "officerNotes", required = false) String officerNotes,
                              RedirectAttributes ra) {

        boolean ok = service.requestInfo(id, needsInfoMessage, officerNotes);

        ra.addFlashAttribute("toastMessage", ok ? "Information requested from customer" : "Failed to request information");
        ra.addFlashAttribute("toastType", ok ? "success" : "error");

        return "redirect:/officer/application/" + id;
    }

    @PostMapping("/application/{id}/forward-risk")
    public String forwardToRisk(@PathVariable("id") Long id,
                                @RequestParam(value = "officerNotes", required = false) String officerNotes,
                                RedirectAttributes ra) {

        boolean ok = service.forwardToRisk(id, officerNotes);

        ra.addFlashAttribute("toastMessage", ok ? "Application forwarded to Risk Officer" : "Failed to forward application");
        ra.addFlashAttribute("toastType", ok ? "success" : "error");

        return "redirect:/officer/application/" + id;
    }

    @GetMapping("/document/test")
    @ResponseBody
    public String test() {
        return "OK";
    }
}