package gov.healthit.chpl.validation.certifiedProduct;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;

@Service
public class CertifiedtProductTestFunctionalityValidator {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedtProductTestFunctionalityValidator.class);
    private TestFunctionalityDAO testFunctionalityDAO;
    private CertificationCriterionDAO certificationCriterionDAO;
    private MessageSource messageSource;
    private PracticeTypeDAO practiceTypeDAO;

    @Autowired
    public CertifiedtProductTestFunctionalityValidator(
            final TestFunctionalityDAO testFunctionalityDAO,
            final CertificationCriterionDAO certificationCriterionDAO,
            final PracticeTypeDAO practiceTypeDAO,
            final MessageSource messageSource) {
        this.testFunctionalityDAO = testFunctionalityDAO;
        this.certificationCriterionDAO = certificationCriterionDAO;
        this.practiceTypeDAO = practiceTypeDAO;
        this.messageSource = messageSource;
    }

    public Set<String> getTestFunctionalityValidationErrors(final PendingCertifiedProductDTO certifiedProduct) {
        Set<String> errors = new HashSet<String>();

        if (certifiedProduct.getCertificationCriterion() != null) {
            for (PendingCertificationResultDTO cr : certifiedProduct.getCertificationCriterion()) {
                if (cr.getTestFunctionality() != null) {
                    for (PendingCertificationResultTestFunctionalityDTO crtf : cr.getTestFunctionality()) {
                          errors.addAll(getTestingFunctionalityErrorMessages(crtf, cr, certifiedProduct));
                    }
                }
            }
        }

        return errors;
    }

    private Set<String> getTestingFunctionalityErrorMessages(final PendingCertificationResultTestFunctionalityDTO crtf,
            final PendingCertificationResultDTO cr, final PendingCertifiedProductDTO cp) {

        Set<String> errors = new HashSet<String>();
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getNumber());
        PracticeTypeDTO pt = practiceTypeDAO.getByName(cp.getPracticeType());

        if (!isTestFunctionalityPracticeTypeValid(pt.getId(), tf)) {
            errors.add(getTestFunctionalityPracticeTypeErrorMessage(crtf, cr, cp));
        }

        String criterionNumber = cr.getNumber();
        if (!isTestFunctionalityCritierionValid(criterionNumber, tf)) {
            errors.add(getTestFunctionalityCriterionErrorMessage(crtf, cr, cp));
        }

        return errors;
    }

    public Set<String> getTestFunctionalityValidationErrors(final CertifiedProductSearchDetails certifiedProduct) {

        Set<String> errors = new HashSet<String>();

        if (certifiedProduct.getCertificationResults() != null) {
            for (CertificationResult cr : certifiedProduct.getCertificationResults()) {
                if (cr.getTestFunctionality() != null) {
                    for (CertificationResultTestFunctionality crtf : cr.getTestFunctionality()) {
                        errors.addAll(getTestingFunctionalityErrorMessages(crtf, cr, certifiedProduct));
                    }
                }
            }
        }

        return errors;
    }

    private Set<String> getTestingFunctionalityErrorMessages(final CertificationResultTestFunctionality crtf,
            final CertificationResult cr, final CertifiedProductSearchDetails cp) {

        Set<String> errors = new HashSet<String>();
        TestFunctionalityDTO tf = getTestFunctionality(crtf.getName());

        Long practiceTypeId = Long.valueOf(cp.getPracticeType().get("id").toString());
        if (!isTestFunctionalityPracticeTypeValid(practiceTypeId, tf)) {
            errors.add(getTestFunctionalityPracticeTypeErrorMessage(crtf, cr, cp));
        }

        String criterionNumber = cr.getNumber();
        if (!isTestFunctionalityCritierionValid(criterionNumber, tf)) {
            errors.add(getTestFunctionalityCriterionErrorMessage(crtf, cr, cp));
        }
        return errors;
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

        return String.format(
                messageSource.getMessage(
                        new DefaultMessageSourceResolvable("listing.criteria.testFunctionalityPracticeTypeMismatch"),
                        LocaleContextHolder.getLocale()),
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

        return String.format(
                messageSource.getMessage(
                        new DefaultMessageSourceResolvable("listing.criteria.testFunctionalityCriterionMismatch"),
                        LocaleContextHolder.getLocale()),
                criteriaNumber, testFunctionalityNumber, validCriterion, currentCriterion);
    }
}
