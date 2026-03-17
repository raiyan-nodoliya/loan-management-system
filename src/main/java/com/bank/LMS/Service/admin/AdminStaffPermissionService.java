package com.bank.LMS.Service.admin;

import com.bank.LMS.Entity.StaffUsers;
import com.bank.LMS.Repository.StaffUsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminStaffPermissionService {

    private final StaffUsersRepository staffUsersRepository;

    public AdminStaffPermissionService(StaffUsersRepository staffUsersRepository) {
        this.staffUsersRepository = staffUsersRepository;
    }

    @Transactional
    public void updateReportPermissions(Long staffId, boolean viewEnabled, boolean exportEnabled) {
        StaffUsers staff = staffUsersRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff user not found: " + staffId));

        staff.setReportViewEnabled(viewEnabled);
        staff.setReportExportEnabled(exportEnabled);

        staffUsersRepository.save(staff);
    }
}