package gov.healthit.chpl.questionableactivity.listing;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CuresUpdateDesignationRemoved implements ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        if (curesUpdateChangedTrueToFalse(origListing, newListing)) {
            return List.of(QuestionableActivityListingDTO.builder()
                    .before(Boolean.TRUE.toString())
                    .after(Boolean.FALSE.toString())
                    .build());
        } else {
            return null;
        }
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CURES_UPDATE_REMOVED;
    }

    private boolean curesUpdateChangedTrueToFalse(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        return BooleanUtils.isTrue(origListing.getCuresUpdate())
                && BooleanUtils.isFalse(newListing.getCuresUpdate());
    }
}
