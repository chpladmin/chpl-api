package gov.healthit.chpl.certificationId;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

public class CertificationIdYearCalculatorTest {

    private DateTimeFormatter toPropertyValueFormatter = DateTimeFormatter.ofPattern("MM/dd");

    @Test
    public void todayEqualsCmsIdAnnualFormatSwitch_yearIsThisYear() {
        int thisYear = LocalDate.now().getYear();
        MonthDay today = MonthDay.now();
        CertificationIdYearCalculator certIdYearCalculator = new CertificationIdYearCalculator(
                toPropertyValueFormatter.format(today),
                "09/01", "12/31");
        assertEquals(thisYear + "", certIdYearCalculator.getCurrentCertIdYear());
        assertEquals(thisYear + "", certIdYearCalculator.getCurrentCertIdYear("2016"));
    }

    @Test
    public void todayAfterCmsIdAnnualFormatSwitch_yearIsMinOfYesterdayAndToday() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        MonthDay yesterdayMonthDay = MonthDay.of(yesterday.getMonth(), yesterday.getDayOfMonth());

        CertificationIdYearCalculator certIdYearCalculator = new CertificationIdYearCalculator(
                toPropertyValueFormatter.format(yesterdayMonthDay),
                "09/01", "12/31");
        Integer expectedYear = Math.min(today.getYear(), yesterday.getYear());
        assertEquals(expectedYear + "", certIdYearCalculator.getCurrentCertIdYear());
        assertEquals(expectedYear + "", certIdYearCalculator.getCurrentCertIdYear("2016"));
    }
}
