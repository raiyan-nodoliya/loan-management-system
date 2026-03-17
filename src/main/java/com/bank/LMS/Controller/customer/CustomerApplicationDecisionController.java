package com.bank.LMS.Controller.customer;

import com.bank.LMS.Entity.Customer;
import com.bank.LMS.Repository.CustomerRepository;
import com.bank.LMS.Service.officer.BranchManagerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/application")
public class CustomerApplicationDecisionController {

    private final CustomerRepository customerRepository;
    private final BranchManagerService branchManagerService;

    public CustomerApplicationDecisionController(CustomerRepository customerRepository,
                                                 BranchManagerService branchManagerService) {
        this.customerRepository = customerRepository;
        this.branchManagerService = branchManagerService;
    }

    private Customer getLoggedCustomer(HttpSession session) {
        Long customerId = (Long) session.getAttribute("CUSTOMER_ID");
        if (customerId == null) return null;
        return customerRepository.findById(customerId).orElse(null);
    }

    @PostMapping("/{id}/accept-offer")
    public String acceptOffer(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes ra) {

        Customer customer = getLoggedCustomer(session);
        if (customer == null) {
            return "redirect:/customer_login";
        }

        boolean ok = branchManagerService.acceptOfferByCustomer(id, customer.getCustomerId());

        ra.addFlashAttribute("toastMessage",
                ok ? "You accepted the loan offer successfully" : "Unable to accept the loan offer");
        ra.addFlashAttribute("toastType", ok ? "success" : "error");

        return "redirect:/customer/application/" + id;
    }

    @PostMapping("/{id}/reject-offer")
    public String rejectOffer(@PathVariable Long id,
                              @RequestParam("rejectReason") String rejectReason,
                              HttpSession session,
                              RedirectAttributes ra) {

        Customer customer = getLoggedCustomer(session);
        if (customer == null) {
            return "redirect:/customer_login";
        }

        boolean ok = branchManagerService.rejectOfferByCustomer(id, customer.getCustomerId(), rejectReason);

        ra.addFlashAttribute("toastMessage",
                ok ? "You rejected the loan offer" : "Unable to reject the loan offer");
        ra.addFlashAttribute("toastType", ok ? "success" : "error");

        return "redirect:/customer/application/" + id;
    }
}