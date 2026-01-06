package gov.healthit.chpl.certificationId;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class CertificationIdYearCalculatorTest {

    private DateTimeFormatter toPropertyValueFormatter = DateTimeFormatter.ofPattern("MM/dd");

    @Test
    public void todayEqualsCmsIdAnnualFormatSwitch_yearIsThisYear() {
        LocalDate now = LocalDate.now();

        Environment env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty(ArgumentMatchers.eq("cmsIdStartDayOfYear")))
            .thenReturn(toPropertyValueFormatter.format(now));
        CertificationIdYearCalculator certIdYearCalculator = new CertificationIdYearCalculator(env);
        assertEquals(now.getYear() + "", certIdYearCalculator.getCurrentCertIdYear());
        assertEquals(now.getYear() + "", certIdYearCalculator.getCurrentCertIdYear("2016"));
    }

    @Test
    public void todayAfterCmsIdAnnualFormatSwitch_yearIsMinOfYesterdayAndToday() {
        LocalDate now = LocalDate.now();
        LocalDate yesterday = now.minusDays(1);

        Environment env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty(ArgumentMatchers.eq("cmsIdStartDayOfYear")))
            .thenReturn(toPropertyValueFormatter.format(yesterday));
        CertificationIdYearCalculator certIdYearCalculator = new CertificationIdYearCalculator(env);
        Integer expectedYear = Math.min(now.getYear(), yesterday.getYear());
        assertEquals(expectedYear + "", certIdYearCalculator.getCurrentCertIdYear());
        assertEquals(expectedYear + "", certIdYearCalculator.getCurrentCertIdYear("2016"));
    }
}
