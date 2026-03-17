package com.bank.LMS.Service.customer;

import com.bank.LMS.Entity.LoanApplication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ApplicationTimelineService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");

    public List<Map<String, String>> buildTimeline(LoanApplication app) {
        List<Map<String, String>> steps = new ArrayList<>();

        if (app == null) {
            return steps;
        }

        // 1. Draft / created
        if (app.getCreatedAt() != null) {
            steps.add(step("Application created", format(app.getCreatedAt())));
        }

        // 2. Submitted
        if (app.getSubmittedAt() != null) {
            steps.add(step("Application submitted", format(app.getSubmittedAt())));
        }

        // 3. Officer review started
        if (app.getOfficerReviewStartedAt() != null) {
            steps.add(step("Under officer review", format(app.getOfficerReviewStartedAt())));
        }

        // 4. Needs info
        if (app.getNeedsInfoAt() != null) {
            String msg = (app.getNeedsInfoMessage() != null && !app.getNeedsInfoMessage().isBlank())
                    ? "Additional information requested: " + app.getNeedsInfoMessage()
                    : "Additional information requested from customer";

            steps.add(step(msg, format(app.getNeedsInfoAt())));
        }

        // 5. Forwarded to risk
        if (app.getForwardedToRiskAt() != null) {
            steps.add(step("Forwarded to risk evaluation", format(app.getForwardedToRiskAt())));
        }

        // 6. Risk approve recommendation
        if (app.getRiskRecommendedApproveAt() != null) {
            steps.add(step("Risk officer recommended approval", format(app.getRiskRecommendedApproveAt())));
        }

        // 7. Risk reject recommendation
        if (app.getRiskRecommendedRejectAt() != null) {
            steps.add(step("Risk officer recommended rejection", format(app.getRiskRecommendedRejectAt())));
        }

        // 8. Final approved
        if (app.getApprovedAt() != null) {
            steps.add(step("Loan approved by branch manager", format(app.getApprovedAt())));
        }

        // 9. Final rejected
        if (app.getRejectedAt() != null) {
            steps.add(step("Loan rejected by branch manager", format(app.getRejectedAt())));
        }

        // Fallback: agar sirf current status hai aur timestamp missing hai
        if (steps.isEmpty() && app.getStatus() != null) {
            steps.add(step("Current status: " + app.getStatus().getLabel(), "In progress"));
        }

        return steps;
    }

    private String format(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }

    private Map<String, String> step(String title, String date) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("title", title);
        map.put("date", date);
        return map;
    }
}