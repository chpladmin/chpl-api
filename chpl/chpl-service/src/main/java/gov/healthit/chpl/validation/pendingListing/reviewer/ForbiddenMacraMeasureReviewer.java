package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

/**
 * A user is not allowed to use certain macra measures temporarily.
 * THIS CLASS SHOULD BE DELETED AT SOME POINT IN THE NEAR(?) FUTURE.
 * @author kekey
 *
 */
@Component("pendingForbiddenMacraMeasureReviewer")
public class ForbiddenMacraMeasureReviewer implements Reviewer {

    @Autowired private MacraMeasureDAO mmDao;
    private static final String ERROR_MSG = "Currently %s is in draft and not "
            + "able to be used on a certification listing at this time.";
    private static final String[] FORBIDDEN_MACRA_MEASURES = {
            "RT13 EH/CAH Stage 3",
            "RT14 EH/CAH Stage 3",
            "RT15 EH/CAH Stage 3"
    };

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                for (PendingCertificationResultMacraMeasureDTO mm : cert.getG1MacraMeasures()) {
                    for (int i = 0; i < FORBIDDEN_MACRA_MEASURES.length; i++) {
                        //the id should be filled in if it's a valid macra measure
                        //if it's invalid (no ID) we can just ignore it
                        if (mm.getMacraMeasureId() != null) {
                            MacraMeasureDTO foundMacraMeasure = mmDao.getById(mm.getMacraMeasureId());
                            if (FORBIDDEN_MACRA_MEASURES[i].equals(foundMacraMeasure.getValue())) {
                                listing.getErrorMessages().add(
                                        String.format(ERROR_MSG, foundMacraMeasure.getValue()));
                            }
                        }
                    }
                }
            }
            if (cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
                for (PendingCertificationResultMacraMeasureDTO mm : cert.getG2MacraMeasures()) {
                    for (int i = 0; i < FORBIDDEN_MACRA_MEASURES.length; i++) {
                        //the id should be filled in if it's a valid macra measure
                        //if it's invalid (no ID) we can just ignore it
                        if (mm.getMacraMeasureId() != null) {
                            MacraMeasureDTO foundMacraMeasure = mmDao.getById(mm.getMacraMeasureId());
                            if (FORBIDDEN_MACRA_MEASURES[i].equals(foundMacraMeasure.getValue())) {
                                listing.getErrorMessages().add(
                                        String.format(ERROR_MSG, foundMacraMeasure.getValue()));
                            }
                        }
                    }
                }
            }
        }
    }
}
