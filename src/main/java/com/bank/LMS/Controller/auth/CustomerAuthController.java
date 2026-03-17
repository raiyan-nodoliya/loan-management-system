package com.bank.LMS.Controller.auth;

import com.bank.LMS.Entity.Customer;
import com.bank.LMS.Service.auth.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
public class CustomerAuthController {

    private final CustomerService customerService;

    public CustomerAuthController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@RequestParam(required = false) String fullName,
                                 @RequestParam(required = false) String email,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(required = false) String dob,
                                 @RequestParam(required = false) String address,
                                 @RequestParam(required = false) String city,
                                 @RequestParam(required = false) String state,
                                 @RequestParam(required = false) String pincode,
                                 @RequestParam(required = false) String password,
                                 @RequestParam(required = false) String confirmPassword,
                                 @RequestParam(required = false) String gender,
                                 Model model,
                                 RedirectAttributes ra) {

        Map<String, String> errors = new LinkedHashMap<>();

        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim();
        phone = phone == null ? "" : phone.trim();
        dob = dob == null ? "" : dob.trim();
        address = address == null ? "" : address.trim();
        city = city == null ? "" : city.trim();
        state = state == null ? "" : state.trim();
        pincode = pincode == null ? "" : pincode.trim();
        password = password == null ? "" : password;
        confirmPassword = confirmPassword == null ? "" : confirmPassword;
        gender  = gender  == null ? "" : gender.trim();


        if (fullName.isBlank()) {
            errors.put("fullName", "Full name is required");
        } else if (!Pattern.compile("^[A-Za-z][A-Za-z\\s.'-]{1,99}$").matcher(fullName).matches()) {
            errors.put("fullName", "Name must contain only letters and spaces");
        }

        if (gender == null || gender.isBlank()) {
            errors.put("gender", "Gender is required");
        }

        if (email.isBlank()) {
            errors.put("email", "Email is required");
        } else if (!Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(email).matches()) {
            errors.put("email", "Invalid email format");
        }

        if (phone.isBlank()) {
            errors.put("phone", "Phone is required");
        } else if (!Pattern.compile("^[6-9]\\d{9}$").matcher(phone).matches()) {
            errors.put("phone", "Enter valid 10-digit phone number");
        }

        if (dob.isBlank()) {
            errors.put("dob", "DOB is required");
        }

        if (city.isBlank()) {
            errors.put("city", "City is required");
        } else if (city.length() > 80) {
            errors.put("city", "City must be within 80 characters");
        }

        if (state.isBlank()) {
            errors.put("state", "State is required");
        } else if (state.length() > 80) {
            errors.put("state", "State must be within 80 characters");
        }

        if (pincode.isBlank()) {
            errors.put("pincode", "Pincode is required");
        } else if (!Pattern.compile("^[0-9]{4,12}$").matcher(pincode).matches()) {
            errors.put("pincode", "Pincode must be 4 to 12 digits");
        }

        if (address.isBlank()) {
            errors.put("address", "Address is required");
        } else if (address.length() < 10) {
            errors.put("address", "Address must be at least 10 characters");
        } else if (address.length() > 255) {
            errors.put("address", "Address must be within 255 characters");
        }

        if (password.isBlank()) {
            errors.put("password", "Password is required");
        } else if (password.length() < 8) {
            errors.put("password", "Password must be at least 8 characters");
        }

        if (confirmPassword.isBlank()) {
            errors.put("confirmPassword", "Confirm password is required");
        } else if (!password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Password and Confirm Password do not match");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            model.addAttribute("dob", dob);
            model.addAttribute("address", address);
            model.addAttribute("city", city);
            model.addAttribute("state", state);
            model.addAttribute("pincode", pincode);
            model.addAttribute("gender",gender);
            model.addAttribute("toastMessage", "Please fix the highlighted errors.");
            model.addAttribute("toastType", "error");
            return "auth/register";
        }

        try {
            customerService.registerCustomer(
                    fullName,
                    email,
                    phone,
                    dob,
                    gender,
                    address,
                    city,
                    state,
                    pincode,
                    password,
                    confirmPassword
            );

            ra.addFlashAttribute("toastMessage", "Your registration is successful!");
            ra.addFlashAttribute("toastType", "success");
            return "redirect:/customer_login";

        } catch (Exception ex) {
            String msg = ex.getMessage() == null ? "Registration failed" : ex.getMessage();

            String lower = msg.toLowerCase();
            if (lower.contains("email")) errors.put("email", msg);
            else if (lower.contains("phone")) errors.put("phone", msg);
            else if (lower.contains("city")) errors.put("city", msg);
            else if (lower.contains("state")) errors.put("state", msg);
            else if (lower.contains("pincode")) errors.put("pincode", msg);
            else if (lower.contains("address")) errors.put("address", msg);
            else if (lower.contains("password")) errors.put("password", msg);
            else errors.put("form", msg);

            model.addAttribute("errors", errors);
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            model.addAttribute("dob", dob);
            model.addAttribute("address", address);
            model.addAttribute("city", city);
            model.addAttribute("state", state);
            model.addAttribute("pincode", pincode);
            model.addAttribute("toastMessage", msg);
            model.addAttribute("toastType", "error");

            return "auth/register";
        }
    }

    @GetMapping("/customer_login")
    public String customerLoginPage() {
        return "auth/customer_login";
    }

    @PostMapping("/customer/login")
    public String customerLoginSubmit(@RequestParam(required = false) String email,
                                      @RequestParam(required = false) String password,
                                      Model model,
                                      HttpSession session,
                                      RedirectAttributes ra) {

        Map<String, String> errors = new LinkedHashMap<>();

        email = email == null ? "" : email.trim();
        password = password == null ? "" : password;

        if (email.isBlank()) errors.put("email", "Email is required");
        else if (!Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(email).matches())
            errors.put("email", "Invalid email format");

        if (password.isBlank()) errors.put("password", "Password is required");
        else if (password.length() < 8) errors.put("password", "Password must be at least 8 characters");

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("email", email);
            model.addAttribute("toastMessage", "Please fix the highlighted errors.");
            model.addAttribute("toastType", "error");
            return "auth/customer_login";
        }

        try {
            Customer customer = customerService.authenticateCustomer(email, password);

            session.setAttribute("CUSTOMER_ID", customer.getCustomerId());
            session.setAttribute("CUSTOMER_EMAIL", customer.getEmail());
            session.setAttribute("CUSTOMER_NAME", customer.getName());

            ra.addFlashAttribute("toastMessage", "Login successful!");
            ra.addFlashAttribute("toastType", "success");
            return "redirect:/customer/dashboard";

        } catch (Exception ex) {
            String msg = ex.getMessage() == null ? "Login failed" : ex.getMessage();

            if (msg.toLowerCase().contains("email")) errors.put("email", msg);
            else if (msg.toLowerCase().contains("password")) errors.put("password", msg);
            else errors.put("form", msg);

            model.addAttribute("errors", errors);
            model.addAttribute("email", email);
            model.addAttribute("toastMessage", msg);
            model.addAttribute("toastType", "error");
            return "auth/customer_login";
        }
    }

    @GetMapping("/customer/logout")
    public String customerLogout(HttpServletRequest request, RedirectAttributes ra) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        ra.addFlashAttribute("toastMessage", "Logout successful!");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/customer_login";
    }
}