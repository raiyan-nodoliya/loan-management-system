package com.bank.LMS.Controller.customer;

import com.bank.LMS.Entity.Customer;
import com.bank.LMS.Service.customer.CustomerProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/customer")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    public CustomerProfileController(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    @GetMapping("/my_profile")
    public String profilePage(HttpSession session, Model model, RedirectAttributes ra) {
        Long customerId = (Long) session.getAttribute("CUSTOMER_ID");

        if (customerId == null) {
            ra.addFlashAttribute("toastMessage", "Please login first");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/customer_login";
        }

        Customer customer = customerProfileService.getCustomerOrThrow(customerId);
        model.addAttribute("customer", customer);

        return "customer/my_profile";
    }

    @PostMapping("/my_profile")
    public String updateProfile(@RequestParam(required = false) String fullName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String dob,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String city,
                                @RequestParam(required = false) String state,
                                @RequestParam(required = false) String pincode,
                                HttpSession session,
                                Model model,
                                RedirectAttributes ra) {

        Long customerId = (Long) session.getAttribute("CUSTOMER_ID");

        if (customerId == null) {
            ra.addFlashAttribute("toastMessage", "Please login first");
            ra.addFlashAttribute("toastType", "error");
            return "redirect:/customer_login";
        }

        Map<String, String> errors = new LinkedHashMap<>();

        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim();
        phone = phone == null ? "" : phone.trim();
        dob = dob == null ? "" : dob.trim();
        address = address == null ? "" : address.trim();
        city = city == null ? "" : city.trim();
        state = state == null ? "" : state.trim();
        pincode = pincode == null ? "" : pincode.trim();

        if (fullName.isBlank()) {
            errors.put("fullName", "Full name is required");
        } else if (!Pattern.compile("^[A-Za-z][A-Za-z\\s.'-]{1,99}$").matcher(fullName).matches()) {
            errors.put("fullName", "Name must contain only letters and spaces");
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
        } else {
            try {
                LocalDate parsedDob = LocalDate.parse(dob);
                if (parsedDob.isAfter(LocalDate.now())) {
                    errors.put("dob", "DOB cannot be in the future");
                }
            } catch (DateTimeParseException e) {
                errors.put("dob", "Invalid date of birth");
            }
        }

        if (address.isBlank()) {
            errors.put("address", "Address is required");
        } else if (address.length() < 10) {
            errors.put("address", "Address must be at least 10 characters");
        } else if (address.length() > 255) {
            errors.put("address", "Address must be within 255 characters");
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
            model.addAttribute("toastMessage", "Please fix the highlighted errors.");
            model.addAttribute("toastType", "error");
            return "customer/my_profile";
        }

        try {
            customerProfileService.updateProfile(
                    customerId, fullName, email, phone, dob, address, city, state, pincode
            );

            session.setAttribute("CUSTOMER_EMAIL", email);
            session.setAttribute("CUSTOMER_NAME", fullName);

            ra.addFlashAttribute("toastMessage", "Profile updated successfully!");
            ra.addFlashAttribute("toastType", "success");
            return "redirect:/customer/my_profile";

        } catch (Exception ex) {
            String msg = ex.getMessage() == null ? "Profile update failed" : ex.getMessage();
            String lower = msg.toLowerCase();

            if (lower.contains("email")) errors.put("email", msg);
            else if (lower.contains("phone")) errors.put("phone", msg);
            else if (lower.contains("dob")) errors.put("dob", msg);
            else if (lower.contains("address")) errors.put("address", msg);
            else if (lower.contains("city")) errors.put("city", msg);
            else if (lower.contains("state")) errors.put("state", msg);
            else if (lower.contains("pincode")) errors.put("pincode", msg);
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
            return "customer/my_profile";
        }
    }
}