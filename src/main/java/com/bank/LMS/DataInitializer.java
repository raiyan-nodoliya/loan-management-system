package com.bank.LMS;

import com.bank.LMS.Entity.Role;
import com.bank.LMS.Entity.StaffUsers;
import com.bank.LMS.Repository.RoleRepository;
import com.bank.LMS.Repository.StaffUsersRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final StaffUsersRepository staffRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {

        // 1) Ensure ADMIN role exists
        Role adminRole = roleRepo.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepo.save(
                        Role.builder()
                                .roleName("ADMIN")
                                .active(true)
                                .build()
                ));

        // 2) Create default admin user if not exists
        if (!staffRepo.existsByEmail("admin@lms.com")) {

            StaffUsers admin = StaffUsers.builder()
                    .name("System Admin")
                    .email("admin@lms.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role(adminRole)          // ✅ set Role entity here
                    .phone("9999999999")
                    .active(true)
                    .build();

            staffRepo.save(admin);
            System.out.println("Default ADMIN created.");
        }
    }
}