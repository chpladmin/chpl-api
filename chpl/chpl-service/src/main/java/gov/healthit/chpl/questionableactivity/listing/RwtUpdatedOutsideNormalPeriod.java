package gov.healthit.chpl.questionableactivity.listing;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public abstract class RwtUpdatedOutsideNormalPeriod {
    abstract String getStartMonthAndDay();
    abstract String getEndMonthAndDay();

    public List<QuestionableActivityListingDTO> getQuestionableActivity(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        if ((hasUrlChanged(origListing.getRwtResultsUrl(), newListing.getRwtResultsUrl())
                || hasCheckDateChanged(origListing.getRwtResultsCheckDate(), newListing.getRwtResultsCheckDate()))
                && isCurrentDateOutsideNormalPlanUpdatePeriod()) {

            return List.of(QuestionableActivityListingDTO.builder()
                    .before(getRwtInfoFromListing(origListing))
                    .after(getRwtInfoFromListing(newListing))
                    .build());
        } else {
            return null;
        }
    }

    private String getRwtInfoFromListing(CertifiedProductSearchDetails listing) {
        return (listing.getRwtPlansCheckDate() != null ? listing.getRwtPlansCheckDate().toString() : "")
                + (listing.getRwtResultsCheckDate() != null ? " : " : "")
                + (listing.getRwtPlansUrl() != null ? listing.getRwtPlansUrl() : "");
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
        return Objects.equals(originalUrl, newUrl);
    }

    private Boolean hasCheckDateChanged(LocalDate originalCheckDate, LocalDate newCheckDate) {
        return Objects.equals(originalCheckDate, newCheckDate);
    }

    private Boolean isCurrentDateOutsideNormalPlanUpdatePeriod() {
        return LocalDate.now().isBefore(getNormalUpdatePeriodBegin())
                || LocalDate.now().isAfter(getNormalUpdatePeriodEnd());
    }
}
