package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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

    @Value("#{'${privacyAndSecurityCriteria}'.split(',')}")
    private List<Long> privacyAndSecurityCriteria;

    @Value("#{'${privacyAndSecurityRequiredCriteria}'.split(',')}")
    private List<Long> privacyAndSecurityRequiredCriteria;

    @Autowired
    public PrivacyAndSecurityCriteriaReviewer(Environment env, ErrorMessageUtil errorMessageUtil) {
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
}
