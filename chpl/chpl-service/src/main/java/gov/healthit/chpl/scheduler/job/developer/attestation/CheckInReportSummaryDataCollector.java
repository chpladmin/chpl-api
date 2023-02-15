package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DeveloperAttestationCheckInReportSummaryDataCollector {
    private static final String PENDING_DEVELOPER_ACTION = "Pending Developer Action";
    private static final String PENDING_ACB_ACTION = "Pending ONC-ACB Action";
    private static final String REJECTED = "Rejected";
    private static final String CANCELLED = "Cancelled by Requester";

    public DeveloperAttestationCheckInReportSummary collect(List<DeveloperAttestationCheckInReport> developerAttestationCheckInReports) {
        return DeveloperAttestationCheckInReportSummary.builder()
                .developerCount(calculateDeveloperCount(developerAttestationCheckInReports))
                .attestationsApprovedCount(calculateAttestationApprovedCount(developerAttestationCheckInReports))
                .pendingAcbActionCount(calculatePendingAcbActionCount(developerAttestationCheckInReports))
                .pendingDeveloperActionCount(calculatePendingDeveloperActionCount(developerAttestationCheckInReports))
                .noSubmissionCount(calculateNoSubmissionCount(developerAttestationCheckInReports))
                .build();
    }

    private Long calculateDeveloperCount(List<DeveloperAttestationCheckInReport> developerAttestationCheckInReports) {
        return Long.valueOf(developerAttestationCheckInReports.size());
    }

    private Long calculateAttestationApprovedCount(List<DeveloperAttestationCheckInReport> developerAttestationCheckInReports) {
        return developerAttestationCheckInReports.stream()
                .filter(row -> row.getPublished())
                .count();
    }

    private Long calculatePendingAcbActionCount(List<DeveloperAttestationCheckInReport> developerAttestationCheckInReports) {
        return developerAttestationCheckInReports.stream()
                .filter(row -> NullSafeEvaluator.eval(() -> row.getCurrentStatusName(), "").equals(PENDING_ACB_ACTION))
                .count();
    }

    private Long calculatePendingDeveloperActionCount(List<DeveloperAttestationCheckInReport> developerAttestationCheckInReports) {
        return developerAttestationCheckInReports.stream()
                .filter(row -> NullSafeEvaluator.eval(() -> row.getCurrentStatusName(), "").equals(PENDING_DEVELOPER_ACTION))
                .count();
    }

    private Long calculateNoSubmissionCount(List<DeveloperAttestationCheckInReport> developerAttestationCheckInReports) {
        return developerAttestationCheckInReports.stream()
                .filter(row -> NullSafeEvaluator.eval(() -> row.getCurrentStatusName(), "").equals(REJECTED)
                        || NullSafeEvaluator.eval(() -> row.getCurrentStatusName(), "").equals(CANCELLED)
                        || NullSafeEvaluator.eval(() -> row.getCurrentStatusName(), "").equals(""))
                .count();
    }
}
