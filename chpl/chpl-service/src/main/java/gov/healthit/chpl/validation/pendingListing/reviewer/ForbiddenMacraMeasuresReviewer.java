package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

/**
 * A user is not allowed to use certain macra measures temporarily.
 * THIS CLASS SHOULD BE DELETED AT SOME POINT IN THE NEAR(?) FUTURE.
 * @author kekey
 *
 */
@Component("pendingForbiddenMacraMeasuresReviewer")
public class ForbiddenMacraMeasuresReviewer implements Reviewer {

    private static final String[] FORBIDDEN_MACRA_MEASURES = {
            "RT13 EH/CAH Stage 3",
            "RT14 EH/CAH Stage 3",
            "RT15 EH/CAH Stage 3"
    };

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                for (PendingCertificationResultMacraMeasureDTO mm : cert.getG1MacraMeasures()) {
                    for (int i = 0; i < FORBIDDEN_MACRA_MEASURES.length; i++) {
                        if (FORBIDDEN_MACRA_MEASURES[i].equals(mm.getMacraMeasure().getValue())) {
                            listing.getErrorMessages().add("Criteria " + cert.getNumber()
                                + " is using macra measure " + mm.getMacraMeasure().getValue()
                                + " which is not allowed.");
                        }
                    }
                }
            }
        }
    }
}
