package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MacraMeasure;

/**
 * A user is not allowed to use certain macra measures temporarily.
 * THIS CLASS SHOULD BE DELETED AT SOME POINT IN THE NEAR(?) FUTURE.
 * @author kekey
 *
 */
@Component("forbiddenMacraMeasuresReviewer")
public class ForbiddenMacraMeasuresReviewer implements Reviewer {

    private static final String[] FORBIDDEN_MACRA_MEASURES = {
            "RT13 EH/CAH Stage 3",
            "RT14 EH/CAH Stage 3",
            "RT15 EH/CAH Stage 3"
    };

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                for (MacraMeasure mm : cert.getG1MacraMeasures()) {
                    for (int i = 0; i < FORBIDDEN_MACRA_MEASURES.length; i++) {
                        if (FORBIDDEN_MACRA_MEASURES[i].equals(mm.getAbbreviation())) {
                            listing.getErrorMessages().add("Criteria " + cert.getNumber()
                                + " is using macra measure " + mm.getAbbreviation()
                                + " which is not allowed.");
                        }
                    }
                }
            }
        }
    }
}
