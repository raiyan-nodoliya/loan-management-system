package com.bank.LMS.Service.officer;
import com.bank.LMS.Entity.StaffUsers;
import com.bank.LMS.Repository.StaffUsersRepository;
import org.springframework.stereotype.Service;

@Service
public class StaffPermissionService {

    private final StaffUsersRepository staffUsersRepository;

    public StaffPermissionService(StaffUsersRepository staffUsersRepository) {
        this.staffUsersRepository = staffUsersRepository;
    }

    public boolean canViewReports(String email) {
        if (email == null || email.isBlank()) return false;

        StaffUsers user = staffUsersRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        return Boolean.TRUE.equals(user.getReportViewEnabled());
    }

    public boolean canExportReports(String email) {
        if (email == null || email.isBlank()) return false;

        StaffUsers user = staffUsersRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        return Boolean.TRUE.equals(user.getReportExportEnabled());
    }
}