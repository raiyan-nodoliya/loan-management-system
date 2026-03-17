package com.bank.LMS.Controller.auth;

import com.bank.LMS.Repository.StaffUsersRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class staffAuthController {

    @Autowired
    private StaffUsersRepository staffUsersRepository;

    @GetMapping("/auth/staff_login")
    public String staffLoginPage(@RequestParam(required = false) String logout,
                                 @RequestParam(required = false) String error,
                                 Model model) {

        if ("1".equals(logout)) {
            model.addAttribute("toastMessage", "Logged out successfully!");
            model.addAttribute("toastType", "info");
        }

        if (error != null) {
            model.addAttribute("toastMessage", "Invalid email or password");
            model.addAttribute("toastType", "error");
        }

        return "auth/staff_login";
    }

    @GetMapping("/staff/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/staff_login?logout=1";
    }
}