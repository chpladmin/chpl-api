package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.concept.PrivacyAndSecurityFrameworkConcept;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("pendingValidDataReviewer")
public class ValidDataReviewer extends PermissionBasedReviewer {

    @Autowired
    public ValidDataReviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().equals(Boolean.TRUE)) {
                if (!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    String formattedPrivacyAndSecurityFramework = CertificationResult
                            .formatPrivacyAndSecurityFramework(cert.getPrivacySecurityFramework());
                    PrivacyAndSecurityFrameworkConcept foundPrivacyAndSecurityFramework = PrivacyAndSecurityFrameworkConcept
                            .getValue(formattedPrivacyAndSecurityFramework);
                    if (foundPrivacyAndSecurityFramework == null) {
                        addErrorOrWarningByPermission(listing, cert,
                                "listing.criteria.invalidPrivacySecurityFramework",
                                Util.formatCriteriaNumber(cert.getCriterion()),
                                formattedPrivacyAndSecurityFramework,
                                PrivacyAndSecurityFrameworkConcept.getFormattedValues());
                    }
                }
                if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                    for (PendingCertificationResultAdditionalSoftwareDTO asDto : cert.getAdditionalSoftware()) {
                        if (!StringUtils.isEmpty(asDto.getChplId()) && asDto.getCertifiedProductId() == null) {
                            addErrorOrWarningByPermission(listing, cert,
                                    "listing.criteria.invalidAdditionalSoftware", asDto.getChplId(),
                                    Util.formatCriteriaNumber(cert.getCriterion()));
                        }
                    }
                }
            }
        }
    }
}
