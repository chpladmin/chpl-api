package gov.healthit.chpl.questionableactivity.listing;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.util.DateUtil;

@Component
public class UploadedAfterCertificationDateActivity implements ListingActivity {

    private int numDaysAllowedAfterCertificationDate;

    @Autowired
    public UploadedAfterCertificationDateActivity(
            @Value("${questionableActivityUploadAfterCertificationDateDays}") String questionableActivityUploadAfterCertificationDateDays) {
        numDaysAllowedAfterCertificationDate = Integer.parseInt(questionableActivityUploadAfterCertificationDateDays);
    }

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        LocalDate today = LocalDate.now();
        LocalDate listingCertificationDate = DateUtil.toLocalDate(newListing.getCertificationDate());
        if (listingCertificationDate != null && today.isAfter(listingCertificationDate.plusDays(numDaysAllowedAfterCertificationDate))) {
            return List.of(QuestionableActivityListing.builder().build());
        } else {
            return null;
        }
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.UPLOADED_AFTER_CERTIFICATION_DATE;
    }

}
