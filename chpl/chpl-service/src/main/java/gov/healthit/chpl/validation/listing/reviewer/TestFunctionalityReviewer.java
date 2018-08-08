package gov.healthit.chpl.validation.listing.reviewer;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class TestFunctionalityReviewer implements Reviewer {
    private static final String EDITION_2014 = "2014";
    
    @Autowired private TestFunctionalityDAO testFunctionalityDAO;
    @Autowired private CertificationCriterionDAO certificationCriterionDAO;
    @Autowired private ErrorMessageUtil msgUtil;
    
    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null) {
            for (CertificationResult cr : listing.getCertificationResults()) {
                if (cr.getTestFunctionality() != null) {
                    for (CertificationResultTestFunctionality crtf : cr.getTestFunctionality()) {
                        listing.getErrorMessages().addAll(
                                getTestingFunctionalityErrorMessages(crtf, cr, listing));
                    }
                }
            }
        }
    }

    private Set<String> getTestingFunctionalityErrorMessages(final CertificationResultTestFunctionality crtf,
            final CertificationResult cr, final CertifiedProductSearchDetails listing) {

        Set<String> errors = new HashSet<String>();
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getName());

        Long practiceTypeId = Long.valueOf(listing.getPracticeType().get("id").toString());
        if (!isTestFunctionalityPracticeTypeValid(practiceTypeId, tf)) {
            errors.add(getTestFunctionalityPracticeTypeErrorMessage(crtf, cr, listing));
        }

        String criterionNumber = cr.getNumber();
        if (!isTestFunctionalityCritierionValid(criterionNumber, tf)) {
            errors.add(getTestFunctionalityCriterionErrorMessage(crtf, cr, listing));
        }
        return errors;
    }

    private Boolean isTestFunctionalityCritierionValid(final String certificationCriterionName,
            final TestFunctionalityDTO tf) {

        if (tf.getCertificationCriterion() != null) {
            CertificationCriterionDTO cc =
                    certificationCriterionDAO.getByNameAndYear(certificationCriterionName, EDITION_2014);
            if (!tf.getCertificationCriterion().getId().equals(cc.getId())) {
                return false;
            }
        }
        return true;
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

    private TestFunctionalityDTO getTestFunctionality(final String number) {
        Long editionId = 2L;
        return testFunctionalityDAO.getByNumberAndEdition(number, editionId);
    }

    private String getTestFunctionalityPracticeTypeErrorMessage(final CertificationResultTestFunctionality crtf,
            final CertificationResult cr, final CertifiedProductSearchDetails cp) {

        TestFunctionalityDTO tf = getTestFunctionality(crtf.getName());

        return getTestFunctionalityPracticeTypeErrorMessage(
                cr.getNumber(),
                crtf.getName(),
                tf.getPracticeType().getName(),
                cp.getPracticeType().get("name").toString());
    }

    private String getTestFunctionalityPracticeTypeErrorMessage(final String criteriaNumber,
            final String testFunctionalityNumber, final String validPracticeTypeName,
            final String currentPracticeTypeName) {

        return msgUtil.getMessage("listing.criteria.testFunctionalityPracticeTypeMismatch",
                criteriaNumber, testFunctionalityNumber, validPracticeTypeName, currentPracticeTypeName);
    }

    private String getTestFunctionalityCriterionErrorMessage(final CertificationResultTestFunctionality crtf,
            final CertificationResult cr, final CertifiedProductSearchDetails cp) {

        TestFunctionalityDTO tf = getTestFunctionality(crtf.getName());
        return getTestFunctionalityCriterionErrorMessage(
                cr.getNumber(),
                crtf.getName(),
                tf.getCertificationCriterion().getNumber(),
                cr.getNumber());
    }

    private String getTestFunctionalityCriterionErrorMessage(final String criteriaNumber,
            final String testFunctionalityNumber, final String validCriterion, final String currentCriterion) {

        return msgUtil.getMessage("listing.criteria.testFunctionalityCriterionMismatch",
                criteriaNumber, testFunctionalityNumber, validCriterion, currentCriterion);
    }
}
