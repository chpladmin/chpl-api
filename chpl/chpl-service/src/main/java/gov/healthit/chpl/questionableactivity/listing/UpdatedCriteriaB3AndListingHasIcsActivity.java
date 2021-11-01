package gov.healthit.chpl.questionableactivity.listing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class UpdatedCriteriaB3AndListingHasIcsActivity implements ListingActivity {
    private static final String B3_CHANGE_DATE = "questionableActivity.b3ChangeDate";
    private static final String B3_CRITERIA_NUMER = "170.315 (b)(3)";

    private Environment env;

    @Autowired
    public UpdatedCriteriaB3AndListingHasIcsActivity(Environment env) {
        this.env = env;
    }

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        CertificationResult originalB3 = getB3Criteria(origListing);
        CertificationResult newB3 = getB3Criteria(newListing);
        Date b3ChangeDate = null;
        Date currentDate = new Date();
        try {
            b3ChangeDate = getB3ChangeDate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        if (currentDate.after(b3ChangeDate)
                && isB3CriteriaNew(originalB3, newB3)
                && hasICS(newListing)) {
            activity = new QuestionableActivityListingDTO();
            activity.setAfter(B3_CRITERIA_NUMER);
        }
        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CRITERIA_B3_ADDED_TO_EXISTING_LISTING_WITH_ICS;
    }

    private Date getB3ChangeDate() throws ParseException {
        String dateAsString = env.getProperty(B3_CHANGE_DATE);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.parse(dateAsString);
    }

    private CertificationResult getB3Criteria(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(result -> result.getNumber().equals(B3_CRITERIA_NUMER))
                .findFirst()
                .orElse(null);
    }

    private boolean isB3CriteriaNew(CertificationResult origB3, CertificationResult newB3) {
        return origB3 != null && newB3 != null && !origB3.isSuccess() && newB3.isSuccess();
    }

    private boolean hasICS(CertifiedProductSearchDetails listing) {
        if (listing != null && listing.getIcs() != null && listing.getIcs().getInherits() != null) {
            return listing.getIcs().getInherits();
        } else {
            return false;
        }
    }

}
