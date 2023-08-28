package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("privacyAndSecurityCriteriaReviewerPreErdPhase2")
public class PrivacyAndSecurityCriteriaReviewerPreErdPhase2 implements ComparisonReviewer {
    private Environment env;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionDAO certificationCriterionDao;
    private ValidationUtils validationUtils;

    private List<CertificationCriterion> privacyAndSecurityCriteria = new ArrayList<CertificationCriterion>();
    private List<CertificationCriterion> privacyAndSecurityRequiredCriteria = new ArrayList<CertificationCriterion>();

    @PostConstruct
    public void postConstruct() {
        privacyAndSecurityCriteria = Arrays.asList(env.getProperty("privacyAndSecurityCriteria").split(",")).stream()
                .map(id -> getCertificationCriterion(Long.parseLong(id)))
                .filter(criteria -> BooleanUtils.isFalse(criteria.getRemoved()))
                .collect(Collectors.toList());

        privacyAndSecurityRequiredCriteria = Arrays
                .asList(env.getProperty("privacyAndSecurityRequiredCriteria").split(",")).stream()
                .map(id -> getCertificationCriterion(Long.parseLong(id)))
                .collect(Collectors.toList());
    }

    @Autowired
    public PrivacyAndSecurityCriteriaReviewerPreErdPhase2(CertificationCriterionDAO certificationCriterionDao, Environment env,
            ErrorMessageUtil errorMessageUtil, ValidationUtils validationUtils) {
        this.certificationCriterionDao = certificationCriterionDao;
        this.env = env;
        this.errorMessageUtil = errorMessageUtil;
        this.validationUtils = validationUtils;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        try {
            if (isActiveOrSuspendedCertificationStatusType(updatedListing)) {
                List<CertificationCriterion> existingAttestedToCriteria = validationUtils.getAttestedCriteria(existingListing);
                List<CertificationCriterion> updatedAttestedToCriteria = validationUtils.getAttestedCriteria(updatedListing);

                List<CertificationCriterion> addedCriteria = new ArrayList<CertificationCriterion>(updatedAttestedToCriteria);
                addedCriteria.removeAll(existingAttestedToCriteria);

                if (!addedCriteria.isEmpty()) {
                    LOGGER.debug("Criteria of some kind were added");
                    updatedListing.addAllBusinessErrorMessages(validationUtils.checkSubordinateCriteriaAllRequired(
                            privacyAndSecurityCriteria, privacyAndSecurityRequiredCriteria,
                            updatedAttestedToCriteria, errorMessageUtil).stream()
                            .collect(Collectors.toSet()));
                } else {
                    LOGGER.debug("No criteria of any kind were added, no further review required");
                }
            } else {
                LOGGER.debug("Certification Status is not Active or Suspended, no further review required");
            }
        } catch (ValidationException e) {
            LOGGER.warn("Treating null or empty Status as not Active or Suspended.", e);
        }
    }

    private boolean isActiveOrSuspendedCertificationStatusType(CertifiedProductSearchDetails listing) throws ValidationException {
        if (listing.getCurrentStatus() != null && listing.getCurrentStatus().getStatus() != null
                && StringUtils.isNotEmpty(listing.getCurrentStatus().getStatus().getName())) {
            return CertificationStatusUtil.getActiveStatusNames()
                    .contains(listing.getCurrentStatus().getStatus().getName());
        } else {
            throw new ValidationException("Listing status could not be obtained and thus the listing edit "
                    + getClass().getSimpleName() + " review cannot/should not be completed.");
        }
    }

    private CertificationCriterion getCertificationCriterion(Long certificationCriterionId) {
        try {
            return certificationCriterionDao.getById(certificationCriterionId);
        } catch (Exception e) {
            return null;
        }
    }
}
