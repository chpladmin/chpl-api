package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Confirms that the test functionality is valid for the criteria that is applying it.
 * @author kekey
 *
 */
@Component("pendingTestFunctionalityReviewer")
public class TestFunctionalityReviewer implements Reviewer {

    @Autowired
    private TestFunctionalityDAO testFunctionalityDAO;

    @Autowired
    private CertificationCriterionDAO certificationCriterionDAO;

    @Autowired
    private ErrorMessageUtil msgUtil;

    @Autowired
    private PracticeTypeDAO practiceTypeDAO;

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        if (listing.getCertificationCriterion() != null) {
            for (PendingCertificationResultDTO cr : listing.getCertificationCriterion()) {
                if (cr.getTestFunctionality() != null) {
                    for (PendingCertificationResultTestFunctionalityDTO crtf : cr.getTestFunctionality()) {
                        listing.getErrorMessages().addAll(
                                getTestingFunctionalityErrorMessages(crtf, cr, listing));
                    }
                }
            }
        }
    }

    private Set<String> getTestingFunctionalityErrorMessages(final PendingCertificationResultTestFunctionalityDTO crtf,
            final PendingCertificationResultDTO cr, final PendingCertifiedProductDTO cp) {

        Set<String> errors = new HashSet<String>();
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber());
        if (tf == null) {
            errors.add(msgUtil.getMessage("listing.criteria.testFunctionalityNotFound",
                    cr.getNumber(), crtf.getNumber()));
        } else {
            PracticeTypeDTO pt = practiceTypeDAO.getByName(cp.getPracticeType());

            if (!isTestFunctionalityPracticeTypeValid(pt.getId(), tf)) {
                errors.add(getTestFunctionalityPracticeTypeErrorMessage(crtf, cr, cp));
            }

            String criterionNumber = cr.getNumber();
            if (!isTestFunctionalityCritierionValid(criterionNumber, tf)) {
                errors.add(getTestFunctionalityCriterionErrorMessage(crtf, cr, cp));
            }
        }
        return errors;
    }

    private TestFunctionalityDTO getTestFunctionality(final String number) {
        Long editionId = 2L;
        return testFunctionalityDAO.getByNumberAndEdition(number, editionId);
    }

    private Boolean isTestFunctionalityPracticeTypeValid(final Long practiceTypeId,
            final TestFunctionalityDTO tf) {

        if (tf.getPracticeType() != null) {
            if (!practiceTypeId.equals(tf.getPracticeType().getId())) {
                return false;
            }
        }
        return true;
    }

    private Boolean isTestFunctionalityCritierionValid(final String certificationCriterionName,
            final TestFunctionalityDTO tf) {

        if (tf.getCertificationCriterion() != null) {
            CertificationCriterionDTO cc =
                    certificationCriterionDAO.getByNameAndYear(certificationCriterionName, "2014");
            if (!tf.getCertificationCriterion().getId().equals(cc.getId())) {
                return false;
            }
        }
        return true;
    }

    private String getTestFunctionalityPracticeTypeErrorMessage(
            final PendingCertificationResultTestFunctionalityDTO crtf, final PendingCertificationResultDTO cr,
            final PendingCertifiedProductDTO cp) {

        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber());

        return getTestFunctionalityPracticeTypeErrorMessage(
                cr.getNumber(),
                crtf.getNumber(),
                tf.getPracticeType().getName(),
                cp.getPracticeType());
    }

    private String getTestFunctionalityPracticeTypeErrorMessage(final String criteriaNumber,
            final String testFunctionalityNumber, final String validPracticeTypeName,
            final String currentPracticeTypeName) {

        return msgUtil.getMessage("listing.criteria.testFunctionalityPracticeTypeMismatch",
                criteriaNumber, testFunctionalityNumber, validPracticeTypeName, currentPracticeTypeName);
    }

    private String getTestFunctionalityCriterionErrorMessage(final PendingCertificationResultTestFunctionalityDTO crtf,
            final PendingCertificationResultDTO cr, final PendingCertifiedProductDTO cp) {

        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber());

        return getTestFunctionalityCriterionErrorMessage(
                cr.getNumber(),
                crtf.getNumber(),
                tf.getCertificationCriterion().getNumber(),
                cr.getNumber());
    }

    private String getTestFunctionalityCriterionErrorMessage(final String criteriaNumber,
            final String testFunctionalityNumber, final String validCriterion, final String currentCriterion) {

        return msgUtil.getMessage("listing.criteria.testFunctionalityCriterionMismatch",
                criteriaNumber, testFunctionalityNumber, validCriterion, currentCriterion);
    }
}
