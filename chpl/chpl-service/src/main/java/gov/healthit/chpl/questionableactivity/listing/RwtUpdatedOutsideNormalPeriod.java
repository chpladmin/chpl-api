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

            List x =  List.of(QuestionableActivityListingDTO.builder()
                    .before(getRwtInfoFromListing(origListing))
                    .after(getRwtInfoFromListing(newListing))
                    .build());

            LOGGER.info("Questionable Activities Created: {}", x.size());

            return x;
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
        LocalDate x = LocalDate.of(
                LocalDate.now().getYear(),
                Integer.valueOf(dateParts[0]),
                Integer.valueOf(dateParts[1]));
        LOGGER.info("Normal Update Period Begin {}", x.toString());
        return x;
     }

    private LocalDate getNormalUpdatePeriodEnd() {
        String[] dateParts = getEndMonthAndDay().split("/");
        LocalDate x = LocalDate.of(
                LocalDate.now().getYear(),
                Integer.valueOf(dateParts[0]),
                Integer.valueOf(dateParts[1]));
        LOGGER.info("Normal Update Period END {}", x.toString());
        return x;
     }

    private Boolean hasUrlChanged(String originalUrl, String newUrl) {
        Boolean x = Objects.equals(originalUrl, newUrl);
        LOGGER.info("Has URL Changed: {}", x);
        return x;
    }

    private Boolean hasCheckDateChanged(LocalDate originalCheckDate, LocalDate newCheckDate) {
        Boolean x = Objects.equals(originalCheckDate, newCheckDate);
        LOGGER.info("Has Date Changed: {}", x);
        return x;
    }

    private Boolean isCurrentDateOutsideNormalPlanUpdatePeriod() {
        Boolean x = LocalDate.now().isBefore(getNormalUpdatePeriodBegin())
                || LocalDate.now().isAfter(getNormalUpdatePeriodEnd());
        LOGGER.info("Is Current Date Outside Normal Plan Update Period: {}", x);
        return x;
    }
}
