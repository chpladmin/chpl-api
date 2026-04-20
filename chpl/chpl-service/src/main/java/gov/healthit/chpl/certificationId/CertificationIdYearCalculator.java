package gov.healthit.chpl.certificationId;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
public class CertificationIdYearCalculator {
    private static final String DEFAULT_CERTID_YEAR = "2015";
    private static final int ANNUAL_CERTID_INITIAL_TRANSITION_YEAR = 2025;
    private String annualCertIdChangeMmDd;
    private Pair<MonthDay, MonthDay> cmsIdOverlapRange;
    private DateTimeFormatter dtFormatter;

    @Autowired
    public CertificationIdYearCalculator(@Value("${cmsIdStartDayOfYear}") String cmsIdStartDayOfYear,
            @Value("${cmsIdEndDayOfOverlap}") String cmsIdEndDayOfOverlap) {
        annualCertIdChangeMmDd = cmsIdStartDayOfYear;
        dtFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        DateTimeFormatter monthDayFormatter = DateTimeFormatter.ofPattern("MM/dd");
        cmsIdOverlapRange = Pair.of(MonthDay.parse(cmsIdStartDayOfYear, monthDayFormatter),
                MonthDay.parse(cmsIdEndDayOfOverlap, monthDayFormatter));
    }

    public String getCurrentCertIdYear() {
        return getCurrentCertIdYear(DEFAULT_CERTID_YEAR);
    }

    public String getCurrentCertIdYear(String defaultYear) {
        LocalDate now = LocalDate.now();
        LocalDate initialCmsIdChangeToAnnualFormatDate = getInitialCmsIdTransitionToAnnualFormatDay();

        // Before 9/1/2025 return the "defaultYear" being passed in
        // Between 9/1/2025 and 8/31/2026 inclusive return "2025"
        // Between 9/1/2026 and 8/31/2027 inclusive return "2026"
        // and so on, forever
        if (now.isBefore(initialCmsIdChangeToAnnualFormatDate)) {
            return defaultYear;
        } else {
            LocalDateRange nextCertIdYearDateRange = LocalDateRange.builder()
                    .start(initialCmsIdChangeToAnnualFormatDate)
                    .end(initialCmsIdChangeToAnnualFormatDate.plusYears(1).minusDays(1))
                    .build();
            while (!DateUtil.isDateBetweenInclusive(Pair.of(nextCertIdYearDateRange.getStart(), nextCertIdYearDateRange.getEnd()), now)) {
                nextCertIdYearDateRange = LocalDateRange.builder()
                        .start(nextCertIdYearDateRange.getStart().plusYears(1))
                        .end(nextCertIdYearDateRange.getEnd().plusYears(1))
                        .build();
            }
            return Math.min(nextCertIdYearDateRange.getStart().getYear(), nextCertIdYearDateRange.getEnd().getYear()) + "";
        }
    }

    public String getPreviousCertIdYear() {
        // Before 9/1/2025 return null, there was no concept of previous year
        // Between 9/1/2025 and 8/31/2026 inclusive return "2015"
        // Between 9/1/2026 and 8/31/2027 inclusive return "2025"
        // and so on, forever
        String currentCertIdYear = getCurrentCertIdYear();
        if (currentCertIdYear.equals(DEFAULT_CERTID_YEAR)) {
            return null;
        } else if (currentCertIdYear.equals(ANNUAL_CERTID_INITIAL_TRANSITION_YEAR + "")) {
            return DEFAULT_CERTID_YEAR;
        }
        Integer currentCertIdYearIntValue = Integer.valueOf(currentCertIdYear);
        return (currentCertIdYearIntValue - 1) + "";
    }

    public List<String> getValidCertIdYearsToday() {
        MonthDay today = MonthDay.now();
        if (isDayDuringOverlap(today)) {
            return List.of(getCurrentCertIdYear(), getPreviousCertIdYear());
        }
        return List.of(getCurrentCertIdYear());
    }

    private boolean isDayDuringOverlap(MonthDay day) {
        return (day.equals(cmsIdOverlapRange.getLeft()) || day.isAfter(cmsIdOverlapRange.getLeft()))
                && (day.equals(cmsIdOverlapRange.getRight()) || day.isBefore(cmsIdOverlapRange.getRight()));
    }

    //TODO: Remove with OCD-4928 ???
    public LocalDate getInitialCmsIdTransitionToAnnualFormatDay() {
        return LocalDate.parse(annualCertIdChangeMmDd + "/" + ANNUAL_CERTID_INITIAL_TRANSITION_YEAR, dtFormatter);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class LocalDateRange {
        private LocalDate start;
        private LocalDate end;
    }
}
