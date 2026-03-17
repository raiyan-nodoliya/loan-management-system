package com.bank.LMS.Service.admin;

import com.bank.LMS.Entity.AuditLog;
import com.bank.LMS.Repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repo;

    public void log(String actorEmail, String action, String module, String status, String details) {
        repo.save(AuditLog.builder()
                .actorEmail(actorEmail)
                .action(action)
                .module(module)
                .status(status)
                .details(details)
                .build());
    }
}