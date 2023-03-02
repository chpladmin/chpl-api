package gov.healthit.chpl.scheduler.job.realworldtesting;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReportSummary;
import gov.healthit.chpl.util.NullSafeEvaluator;

public final class RealWorldTestingReportSummaryCalculator {

    private RealWorldTestingReportSummaryCalculator() {

    }

    public static RealWorldTestingReportSummary calculateSummariesByEligibityYear(List<RealWorldTestingReport> rows, Integer eligibilityYear) {
        List<RealWorldTestingReport> filteredRows = filterByEligibilityYear(rows, eligibilityYear);
        return RealWorldTestingReportSummary.builder()
                .rwtEligibilityYear(eligibilityYear)
                .totalListings(calculateTotalListings(filteredRows))
                .totalActive(calculateActiveListings(filteredRows))
                .totalWithdrawn(calculateWithdrawnListings(filteredRows))
                .totalWithPlansUrl(calculateWithPlansUrl(filteredRows))
                .totalWithPlansUrlValidated(calculateWithPlansUrlDate(filteredRows))
                .totalWithResultsUrlValidated(calculateWithResultsUrlDate(filteredRows))
                .totalWithResultsUrl(calculateWithResultsUrl(filteredRows))
                .totalEligibleViaIcs(calculateRwtEligibleDueToIcs(filteredRows))
                .build();
    }

    private static List<RealWorldTestingReport> filterByEligibilityYear(List<RealWorldTestingReport> rows, Integer eligibilityYear) {
        return rows.stream()
                .filter(row -> NullSafeEvaluator.eval(() -> row.getRwtEligibilityYear(), -1).equals(eligibilityYear))
                .toList();
    }

    private static Long calculateTotalListings(List<RealWorldTestingReport> rows) {
        return Long.valueOf(rows.size());
    }

    private static Long calculateActiveListings(List<RealWorldTestingReport> rows) {
        return rows.stream()
                .filter(row -> isStatusActive(row))
                .collect(Collectors.counting());
    }

    private static Long calculateWithdrawnListings(List<RealWorldTestingReport> rows) {
        return rows.stream()
                .filter(row -> isStatusWithdrawn(row))
                .collect(Collectors.counting());
    }

    private static Long calculateWithPlansUrl(List<RealWorldTestingReport> rows) {
        return rows.stream()
                .filter(row -> hasPlansUrl(row))
                .collect(Collectors.counting());
    }

    private static Long calculateWithPlansUrlDate(List<RealWorldTestingReport> rows) {
        return rows.stream()
                .filter(row -> hasPlansUrlDate(row))
                .collect(Collectors.counting());
    }

    private static Long calculateWithResultsUrlDate(List<RealWorldTestingReport> rows) {
        return rows.stream()
                .filter(row -> hasResultsUrlDate(row))
                .collect(Collectors.counting());
    }

    private static Long calculateWithResultsUrl(List<RealWorldTestingReport> rows) {
        return rows.stream()
                .filter(row -> hasResultsUrl(row))
                .collect(Collectors.counting());
    }

    private static Long calculateRwtEligibleDueToIcs(List<RealWorldTestingReport> rows) {
        return rows.stream()
                .filter(row -> isRwtEligibleDueToIcs(row))
                .collect(Collectors.counting());
    }

    private static Boolean isStatusActive(RealWorldTestingReport row) {
        return row.getCurrentStatus().equals(CertificationStatusType.Active.toString());
    }

    private static Boolean isStatusWithdrawn(RealWorldTestingReport row) {
        return row.getCurrentStatus().equals(CertificationStatusType.WithdrawnByAcb.toString())
                || row.getCurrentStatus().equals(CertificationStatusType.WithdrawnByDeveloper.toString());
    }

    private static Boolean hasPlansUrl(RealWorldTestingReport row) {
        return !StringUtils.isEmpty(row.getRwtPlansUrl());
    }

    private static Boolean hasResultsUrl(RealWorldTestingReport row) {
        return !StringUtils.isEmpty(row.getRwtResultsUrl());
    }

    private static Boolean isRwtEligibleDueToIcs(RealWorldTestingReport row) {
        return NullSafeEvaluator.eval(() -> row.getIcs(), false);
    }

    private static Boolean hasPlansUrlDate(RealWorldTestingReport row) {
        return row.getRwtPlansCheckDate() != null;
    }

    private static Boolean hasResultsUrlDate(RealWorldTestingReport row) {
        return row.getRwtResultsCheckDate() != null;
    }
}
