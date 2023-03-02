package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CheckInReportSummaryDataCollector {
    private static final String PENDING_DEVELOPER_ACTION = "Pending Developer Action";
    private static final String PENDING_ACB_ACTION = "Pending ONC-ACB Action";
    private static final String REJECTED = "Rejected";
    private static final String CANCELLED = "Cancelled by Requester";

    public CheckInReportSummary collect(List<CheckInReport> developerAttestationCheckInReports) {
        return CheckInReportSummary.builder()
                .developerCount(calculateDeveloperCount(developerAttestationCheckInReports))
                .attestationsApprovedCount(calculateAttestationApprovedCount(developerAttestationCheckInReports))
                .pendingAcbActionCount(calculatePendingAcbActionCount(developerAttestationCheckInReports))
                .pendingDeveloperActionCount(calculatePendingDeveloperActionCount(developerAttestationCheckInReports))
                .noSubmissionCount(calculateNoSubmissionCount(developerAttestationCheckInReports))
                .build();
    }

    private Long calculateDeveloperCount(List<CheckInReport> developerAttestationCheckInReports) {
        return Long.valueOf(developerAttestationCheckInReports.size());
    }

    private Long calculateAttestationApprovedCount(List<CheckInReport> developerAttestationCheckInReports) {
        return developerAttestationCheckInReports.stream()
                .filter(row -> row.getPublished())
                .count();
    }

    private Long calculatePendingAcbActionCount(List<CheckInReport> developerAttestationCheckInReports) {
        return developerAttestationCheckInReports.stream()
                .filter(row -> NullSafeEvaluator.eval(() -> row.getCurrentStatusName(), "").equals(PENDING_ACB_ACTION))
                .count();
    }

    private Long calculatePendingDeveloperActionCount(List<CheckInReport> developerAttestationCheckInReports) {
        return developerAttestationCheckInReports.stream()
                .filter(row -> NullSafeEvaluator.eval(() -> row.getCurrentStatusName(), "").equals(PENDING_DEVELOPER_ACTION))
                .count();
    }

    private Long calculateNoSubmissionCount(List<CheckInReport> developerAttestationCheckInReports) {
        return developerAttestationCheckInReports.stream()
                .filter(row -> !NullSafeEvaluator.eval(() -> row.getPublished(), false)
                        && (NullSafeEvaluator.eval(() -> row.getCurrentStatusName(), "").equals(REJECTED)
                                || NullSafeEvaluator.eval(() -> row.getCurrentStatusName(), "").equals(CANCELLED)
                                || NullSafeEvaluator.eval(() -> row.getCurrentStatusName(), "").equals("")))
                .count();
    }
}
