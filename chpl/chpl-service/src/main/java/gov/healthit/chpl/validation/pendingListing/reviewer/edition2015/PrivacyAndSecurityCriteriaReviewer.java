package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("pendingPrivacyAndSecurityCriteriaReviewer")
public class PrivacyAndSecurityCriteriaReviewer implements Reviewer {

    private Environment env;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionDAO certificationCriterionDao;

    private List<CertificationCriterion> privacyAndSecurityCriteria = new ArrayList<CertificationCriterion>();
    private List<CertificationCriterion> privacyAndSecurityRequiredCriteria = new ArrayList<CertificationCriterion>();

    @PostConstruct
    public void postConstruct() {
        privacyAndSecurityCriteria = Arrays.asList(env.getProperty("privacyAndSecurityCriteria").split(",")).stream()
                .map(id -> getCertificationCriterion(Long.parseLong(id)))
                .collect(Collectors.toList());

        privacyAndSecurityRequiredCriteria = Arrays
                .asList(env.getProperty("privacyAndSecurityRequiredCriteria").split(",")).stream()
                .map(id -> getCertificationCriterion(Long.parseLong(id)))
                .collect(Collectors.toList());
    }

    @Autowired
    public PrivacyAndSecurityCriteriaReviewer(CertificationCriterionDAO certificationCriterionDao, Environment env,
            ErrorMessageUtil errorMessageUtil) {
        this.certificationCriterionDao = certificationCriterionDao;
        this.env = env;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        List<CertificationCriterion> attestedToCriteria = listing.getCertificationCriterion().stream()
                .filter(cc -> cc.getMeetsCriteria())
                .map(cc -> new CertificationCriterion(cc.getCriterion()))
                .collect(Collectors.toList());

        listing.getErrorMessages().addAll(
                ValidationUtils.checkSubordinateCriteriaAllRequired(
                        privacyAndSecurityCriteria,
                        privacyAndSecurityRequiredCriteria,
                        attestedToCriteria, errorMessageUtil));
    }

    private CertificationCriterion getCertificationCriterion(Long certificationCriterionId) {
        try {
            return new CertificationCriterion(certificationCriterionDao.getById(certificationCriterionId));
        } catch (Exception e) {
            return null;
        }
    }
}
