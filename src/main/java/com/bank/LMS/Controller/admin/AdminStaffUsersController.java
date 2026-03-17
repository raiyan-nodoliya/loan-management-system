package com.bank.LMS.Controller.admin;

import com.bank.LMS.Entity.Role;
import com.bank.LMS.Service.admin.settings.RoleService;
import com.bank.LMS.Service.auth.StaffUsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/users")
public class AdminStaffUsersController {

    private final RoleService roleService;
    private final StaffUsersService staffUsersService;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z\\s]{1,59}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    public AdminStaffUsersController(RoleService roleService, StaffUsersService staffUsersService) {
        this.roleService = roleService;
        this.staffUsersService = staffUsersService;
    }

    @GetMapping
    public String page(@RequestParam(required = false) Long roleId,
                       @RequestParam(required = false) String q,
                       Model model) {

        List<Role> roles = roleService.getActiveRoles();
        model.addAttribute("roles", roles);

        String selectedRoleName = null;
        if (roleId != null) {
            selectedRoleName = roleService.getRoleOrThrow(roleId).getRoleName();
        }

        model.addAttribute("selectedRoleId", roleId);
        model.addAttribute("selectedRoleName", selectedRoleName);
        model.addAttribute("q", q);
        model.addAttribute("staffUsers", staffUsersService.listByRole(roleId, q));
        model.addAttribute("pageTitle", "Staff Users");
        model.addAttribute("pageSubTitle", "Create and manage staff users");

        return "admin/users/staff_users";
    }

    @PostMapping
    public String create(@RequestParam Long roleId,
                         @RequestParam String name,
                         @RequestParam String email,
                         @RequestParam(required = false) String phone,
                         @RequestParam String password,
                         RedirectAttributes ra) {
        try {
            if (roleId == null || roleId <= 0) {
                throw new IllegalArgumentException("Please select a valid role");
            }

            String safeName = normalizeName(name);
            String safeEmail = normalizeEmail(email);
            String safePhone = normalizePhone(phone);
            String safePassword = normalizePassword(password);

            staffUsersService.create(roleId, safeName, safeEmail, safePhone, safePassword);
            ra.addFlashAttribute("success", "Staff user created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users?roleId=" + roleId;
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id,
                         @RequestParam(required = false) Long roleId,
                         RedirectAttributes ra) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid user id");
            }

            staffUsersService.toggleActive(id);
            ra.addFlashAttribute("success", "Status updated!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return (roleId != null)
                ? "redirect:/admin/users?roleId=" + roleId
                : "redirect:/admin/users";
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        String normalized = name.trim().replaceAll("\\s+", " ");

        if (normalized.length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters");
        }

        if (normalized.length() > 60) {
            throw new IllegalArgumentException("Name must not exceed 60 characters");
        }

        if (!NAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Name must contain only letters and spaces");
        }

        return normalized;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        String normalized = email.trim().toLowerCase();

        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Email is too long");
        }

        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address");
        }

        return normalized;
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        String normalized = phone.trim().replaceAll("[^0-9]", "");

        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits");
        }

        return normalized;
    }

    private String normalizePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        String normalized = password.trim();

        if (normalized.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Password must not exceed 100 characters");
        }

        return normalized;
    }
}