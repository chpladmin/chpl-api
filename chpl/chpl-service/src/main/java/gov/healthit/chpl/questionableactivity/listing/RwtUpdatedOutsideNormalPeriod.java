package gov.healthit.chpl.questionableactivity.listing;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public abstract class RwtUpdatedOutsideNormalPeriod {
    abstract String getStartMonthAndDay();
    abstract String getEndMonthAndDay();

    public List<QuestionableActivityListing> getQuestionableActivity(String origUrl, String newUrl, LocalDate origCheckDate, LocalDate newCheckDate) {
        if ((hasUrlChanged(origUrl, newUrl)
                || hasCheckDateChanged(origCheckDate, newCheckDate))
                && isCurrentDateOutsideNormalPlanUpdatePeriod()) {

            return List.of(QuestionableActivityListing.builder()
                    .before(getFormatRwtInfo(origUrl, origCheckDate))
                    .after(getFormatRwtInfo(newUrl, newCheckDate))
                    .build());
        } else {
            return null;
        }
    }

    private String getFormatRwtInfo(String url, LocalDate checkDate) {
        return "{"
                + (checkDate != null ? checkDate.toString() : "NULL")
                + "; "
                + (url != null ? url : "NULL")
                + "}";
    }

    private LocalDate getNormalUpdatePeriodBegin() {
        String[] dateParts = getStartMonthAndDay().split("/");
        return LocalDate.of(
                LocalDate.now().getYear(),
                Integer.valueOf(dateParts[0]),
                Integer.valueOf(dateParts[1]));
     }

    private LocalDate getNormalUpdatePeriodEnd() {
        String[] dateParts = getEndMonthAndDay().split("/");
        return LocalDate.of(
                LocalDate.now().getYear(),
                Integer.valueOf(dateParts[0]),
                Integer.valueOf(dateParts[1]));
     }

    private Boolean hasUrlChanged(String originalUrl, String newUrl) {
        return !Objects.equals(originalUrl, newUrl);
    }

    private Boolean hasCheckDateChanged(LocalDate originalCheckDate, LocalDate newCheckDate) {
        return !Objects.equals(originalCheckDate, newCheckDate);
    }

    private Boolean isCurrentDateOutsideNormalPlanUpdatePeriod() {
        return LocalDate.now().isBefore(getNormalUpdatePeriodBegin())
                || LocalDate.now().isAfter(getNormalUpdatePeriodEnd());
    }
}
