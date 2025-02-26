package gov.healthit.chpl.validation.surveillance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.validation.surveillance.reviewer.NewSurveillanceEditionReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.ReadReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceDetailsReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceNonconformityReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceRequirementReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.WriteReviewer;

@Component("surveillanceCreationValidator")
public class SurveillanceCreationValidator {

    private List<ReadReviewer> readReviewers;
    private List<WriteReviewer> writeReviewers;

    @Autowired
    public SurveillanceCreationValidator(SurveillanceDetailsReviewer survDetailsReviewer,
            SurveillanceRequirementReviewer survReqReviewer,
            SurveillanceNonconformityReviewer survNcReviewer,
            @Qualifier("surveillanceUnsupportedCharacterReviewer") UnsupportedCharacterReviewer charReviewer,
            NewSurveillanceEditionReviewer editionReviewer) {
        readReviewers = new ArrayList<ReadReviewer>();
        readReviewers.add(survDetailsReviewer);
        readReviewers.add(survReqReviewer);
        readReviewers.add(survNcReviewer);
        readReviewers.add(charReviewer);
        writeReviewers = new ArrayList<WriteReviewer>();
        writeReviewers.add(editionReviewer);
    }

    public void validate(CertifiedProductSearchDetails listing, Surveillance updatedSurv) {
        for (ReadReviewer reviewer : readReviewers) {
            reviewer.review(updatedSurv);
        }
        for (WriteReviewer reviewer : writeReviewers) {
            reviewer.review(listing, updatedSurv);
        }
    }
}
