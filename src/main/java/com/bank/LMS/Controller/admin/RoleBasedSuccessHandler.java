package com.bank.LMS.Controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        HttpSession session = request.getSession();
        session.setAttribute("toastMessage", "Login Successful!");
        session.setAttribute("toastType", "success");

        var authorities = authentication.getAuthorities().toString();

        if (authorities.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
            return;
        }
        if (authorities.contains("ROLE_BRANCH_MANAGER")) {
            response.sendRedirect("/manager/dashboard");
            return;
        }
        if (authorities.contains("ROLE_BANK_OFFICER")) {
            response.sendRedirect("/officer/dashboard");
            return;
        }
        if (authorities.contains("ROLE_RISK_OFFICER")) {
            response.sendRedirect("/risk/dashboard");
            return;
        }

        response.sendRedirect("/customer_login?error");
    }
}