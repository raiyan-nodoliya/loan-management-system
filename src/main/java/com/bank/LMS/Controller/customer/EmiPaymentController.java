package com.bank.LMS.Controller.customer;

import com.bank.LMS.Entity.EmiSchedule;
import com.bank.LMS.Service.customer.EmiPaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
public class EmiPaymentController {

    private final EmiPaymentService emiPaymentService;


    public EmiPaymentController(EmiPaymentService emiPaymentService) {
        this.emiPaymentService = emiPaymentService;
    }

    @GetMapping("/emi/pay/{emiId}")
    public String paymentPage(@PathVariable Long emiId,
                              HttpSession session,
                              RedirectAttributes ra,
                              Model model) {

        Long customerId = (Long) session.getAttribute("CUSTOMER_ID");
        if (customerId == null) {
            ra.addFlashAttribute("toastMessage", "Please login first");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/customer_login";
        }

        EmiSchedule emi = emiPaymentService.getCustomerEmi(emiId, customerId);
        if (emi == null) {
            ra.addFlashAttribute("toastMessage", "EMI not found");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/customer/my_loans";
        }

        if (!"PENDING".equalsIgnoreCase(emi.getStatus())) {
            ra.addFlashAttribute("toastMessage", "This EMI is already paid");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/customer/emi_schedule/" + emi.getLoanAccount().getApplication().getApplicationId();
        }

        model.addAttribute("emi", emi);
        return "customer/payment";
    }

    @PostMapping("/emi/pay/{emiId}")
    public String processPayment(@PathVariable Long emiId,
                                 @RequestParam String paymentMethod,
                                 @RequestParam(required = false) String upiId,
                                 @RequestParam(required = false) String cardNumber,
                                 @RequestParam(required = false) String cardHolderName,
                                 @RequestParam(required = false) String expiry,
                                 @RequestParam(required = false) String cvv,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        Long customerId = (Long) session.getAttribute("CUSTOMER_ID");
        if (customerId == null) {
            ra.addFlashAttribute("toastMessage", "Please login first");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/customer_login";
        }

        String validationError = emiPaymentService.validatePayment(
                paymentMethod, upiId, cardNumber, cardHolderName, expiry, cvv
        );

        if (validationError != null) {
            ra.addFlashAttribute("toastMessage", validationError);
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/customer/emi/pay/" + emiId;
        }

        boolean ok = emiPaymentService.payEmi(
                emiId, customerId, paymentMethod, upiId, cardNumber, cardHolderName, expiry, cvv
        );

        if (ok) {
            ra.addFlashAttribute("toastMessage", "Payment successful");
            ra.addFlashAttribute("toastType", "success");
        } else {
            ra.addFlashAttribute("toastMessage", "Payment failed");
            ra.addFlashAttribute("toastType", "error");
        }

        return "redirect:/customer/my_loans";
    }
}