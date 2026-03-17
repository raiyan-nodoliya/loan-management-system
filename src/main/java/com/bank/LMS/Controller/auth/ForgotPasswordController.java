package com.bank.LMS.Controller.auth;

import com.bank.LMS.Service.auth.ForgotPasswordService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/auth")
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(ForgotPasswordService forgotPasswordService) {
        this.forgotPasswordService = forgotPasswordService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        return "auth/forgot_password";
    }

    @PostMapping("/forgot-password")
    public String sendOtp(@RequestParam("email") String email,
                          HttpSession session,
                          Model model) {

        email = email == null ? "" : email.trim().toLowerCase();

        if (email.isBlank()) {
            model.addAttribute("error", "Email is required.");
            return "auth/forgot_password";
        }

        if (!forgotPasswordService.emailExists(email)) {
            model.addAttribute("error", "This email is not registered in our system.");
            model.addAttribute("email", email);
            return "auth/forgot_password";
        }

        String otp = forgotPasswordService.generateOtp();
        String userType = forgotPasswordService.findUserTypeByEmail(email);

        session.setAttribute("resetEmail", email);
        session.setAttribute("resetOtp", otp);
        session.setAttribute("resetUserType", userType);
        session.setAttribute("otpVerified", false);
        session.setAttribute("otpExpiry", LocalDateTime.now().plusMinutes(5));

        forgotPasswordService.sendOtpToEmail(email, otp);

        model.addAttribute("email", email);
        model.addAttribute("success", "OTP sent successfully to your email.");
        return "auth/verify_otp";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("resetEmail");

        if (email == null) {
            return "redirect:/auth/forgot-password";
        }

        model.addAttribute("email", email);
        return "auth/verify_otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp,
                            HttpSession session,
                            Model model) {

        String sessionOtp = (String) session.getAttribute("resetOtp");
        String email = (String) session.getAttribute("resetEmail");
        LocalDateTime otpExpiry = (LocalDateTime) session.getAttribute("otpExpiry");

        if (email == null || sessionOtp == null || otpExpiry == null) {
            return "redirect:/auth/forgot-password";
        }

        if (otp == null || otp.trim().isBlank()) {
            model.addAttribute("email", email);
            model.addAttribute("error", "OTP is required.");
            return "auth/verify_otp";
        }

        if (LocalDateTime.now().isAfter(otpExpiry)) {
            session.removeAttribute("resetOtp");
            session.removeAttribute("otpExpiry");
            model.addAttribute("email", email);
            model.addAttribute("error", "OTP has expired. Please request a new OTP.");
            return "auth/verify_otp";
        }

        if (!sessionOtp.equals(otp.trim())) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Invalid OTP.");
            return "auth/verify_otp";
        }

        session.setAttribute("otpVerified", true);
        model.addAttribute("email", email);
        return "auth/reset_password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("resetEmail");
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");

        if (email == null || otpVerified == null || !otpVerified) {
            return "redirect:/auth/forgot-password";
        }

        model.addAttribute("email", email);
        return "auth/reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("resetEmail");
        String userType = (String) session.getAttribute("resetUserType");
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
        LocalDateTime otpExpiry = (LocalDateTime) session.getAttribute("otpExpiry");

        if (email == null || otpVerified == null || !otpVerified || otpExpiry == null) {
            return "redirect:/auth/forgot-password";
        }

        if (LocalDateTime.now().isAfter(otpExpiry)) {
            session.invalidate();
            return "redirect:/auth/forgot-password";
        }

        if (password == null || password.isBlank()) {
            model.addAttribute("email", email);
            model.addAttribute("error", "New password is required.");
            return "auth/reset_password";
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Confirm password is required.");
            return "auth/reset_password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Password and confirm password do not match.");
            return "auth/reset_password";
        }

        if (password.length() < 6) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "auth/reset_password";
        }

        boolean updated = forgotPasswordService.updatePassword(email, password);

        if (!updated) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Password update failed. Please try again.");
            return "auth/reset_password";
        }

        session.removeAttribute("resetEmail");
        session.removeAttribute("resetOtp");
        session.removeAttribute("resetUserType");
        session.removeAttribute("otpVerified");
        session.removeAttribute("otpExpiry");

        redirectAttributes.addFlashAttribute("toastMessage", "Password updated successfully.");
        redirectAttributes.addFlashAttribute("toastType", "success");

        if ("CUSTOMER".equals(userType)) {
            return "redirect:/customer_login";
        } else {
            return "redirect:/auth/staff_login";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, Model model) {
        String email = (String) session.getAttribute("resetEmail");
        String userType = (String) session.getAttribute("resetUserType");

        if (email == null || userType == null) {
            return "redirect:/auth/forgot-password";
        }

        String otp = forgotPasswordService.generateOtp();

        session.setAttribute("resetOtp", otp);
        session.setAttribute("otpVerified", false);
        session.setAttribute("otpExpiry", LocalDateTime.now().plusMinutes(5));

        forgotPasswordService.sendOtpToEmail(email, otp);

        model.addAttribute("email", email);
        model.addAttribute("success", "New OTP sent successfully.");
        return "auth/verify_otp";
    }
}