package gov.healthit.chpl.certificationId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CertificationIdYearCalculator {
    private Environment env;

    @Autowired
    public CertificationIdYearCalculator(Environment env) {
        this.env = env;
    }

    public String getCurrentCertIdYear(String defaultYear) {
        LocalDate now = LocalDate.now();
        LocalDate startDateForCertIdThisCalendarYear = getCmsIdTransitionDay();
        if (now.isEqual(startDateForCertIdThisCalendarYear)
                || now.isAfter(startDateForCertIdThisCalendarYear)) {
            return now.getYear() + "";
        }
        return defaultYear;
    }

    public LocalDate getCmsIdTransitionDay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String mmdd = env.getProperty("cmsIdStartDayOfYear");
        String mmddyyyy = mmdd + "/" + LocalDate.now().getYear();
        return LocalDate.parse(mmddyyyy, formatter);
    }
}
