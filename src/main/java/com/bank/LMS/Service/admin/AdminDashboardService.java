package com.bank.LMS.Service.admin;

import com.bank.LMS.Entity.StaffUsers;
import com.bank.LMS.Repository.StaffUsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final StaffUsersRepository staffUsersRepository;

    public long totalStaffUsers() {
        return staffUsersRepository.countByRole_RoleNameIn(
                List.of("BANK_OFFICER", "RISK_OFFICER", "BRANCH_MANAGER")
        );
    }

    public long rulesActive() {
        return 0;
    }

    public long pendingRequests() {
        return 0;
    }

    public List<StaffUsers> allStaffUsers() {
        return staffUsersRepository.findAll();
    }
}