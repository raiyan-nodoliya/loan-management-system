package com.bank.LMS.Service.auth;

import com.bank.LMS.Entity.Role;
import com.bank.LMS.Entity.StaffUsers;
import com.bank.LMS.Repository.StaffUsersRepository;
import com.bank.LMS.Service.admin.AuditLogService;
import com.bank.LMS.Service.admin.settings.RoleService;
import com.bank.LMS.Service.config.MailService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffUsersService {

    private final StaffUsersRepository staffUsersRepository;
    private final RoleService roleService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AuditLogService auditLogService;
    private final MailService mailService;

    public StaffUsersService(StaffUsersRepository staffUsersRepository,
                             RoleService roleService,
                             AuditLogService auditLogService,
                             MailService mailService) {
        this.staffUsersRepository = staffUsersRepository;
        this.roleService = roleService;
        this.auditLogService = auditLogService;
        this.mailService = mailService;
    }

    public StaffUsers create(Long roleId, String name, String email, String phone, String password) {

        if (roleId == null) throw new IllegalArgumentException("Role is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password is required");

        staffUsersRepository.findByEmail(email.trim()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already exists: " + email);
        });

        Role role = roleService.getRoleOrThrow(roleId);

        StaffUsers user = StaffUsers.builder()
                .name(name.trim())
                .email(email.trim())
                .phone(phone != null ? phone.trim() : null)
                .passwordHash(encoder.encode(password))
                .role(role)
                .active(true)
                .build();

        StaffUsers saved = staffUsersRepository.save(user);

        auditLogService.log(
                email,
                "CREATE_STAFF",
                "STAFF_USER",
                "SUCCESS",
                "Created staff user " + name
        );

        // new created staff ko mail
        mailService.sendStaffAccountCreated(
                saved.getEmail(),
                saved.getName(),
                saved.getRole().getRoleName()
        );

        // all officers ko notification mail
        List<String> officerRoles = List.of(
                "BANK_OFFICER",
                "RISK_OFFICER",
                "BRANCH_MANAGER",
                "ADMIN"
        );

        List<String> officerEmails = staffUsersRepository
                .findByRole_RoleNameInAndActiveTrue(officerRoles)
                .stream()
                .map(StaffUsers::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .toList();

        mailService.sendNewStaffCreatedToOfficers(
                officerEmails,
                saved.getName(),
                saved.getEmail(),
                saved.getRole().getRoleName()
        );

        return saved;
    }

    public List<StaffUsers> listByRole(Long roleId, String q) {
        if (roleId == null) return List.of();

        if (q == null || q.isBlank()) {
            return staffUsersRepository.findByRole_RoleIdOrderByCreatedAtDesc(roleId);
        }

        String keyword = q.trim();
        return staffUsersRepository
                .findByRole_RoleIdAndNameContainingIgnoreCaseOrRole_RoleIdAndEmailContainingIgnoreCaseOrderByCreatedAtDesc(
                        roleId, keyword,
                        roleId, keyword
                );
    }

    public void toggleActive(Long staffId) {
        StaffUsers u = staffUsersRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + staffId));

        u.setActive(!u.isActive());
        staffUsersRepository.save(u);
    }
}