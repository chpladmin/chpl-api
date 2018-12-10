package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.concept.PrivacyAndSecurityFrameworkConcept;
import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Component("pendingValidDataReviewer")
public class ValidDataReviewer implements Reviewer {

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                String formattedPrivacyAndSecurityFramework = CertificationResult
                        .formatPrivacyAndSecurityFramework(cert.getPrivacySecurityFramework());
                PrivacyAndSecurityFrameworkConcept foundPrivacyAndSecurityFramework = PrivacyAndSecurityFrameworkConcept
                        .getValue(formattedPrivacyAndSecurityFramework);
                if (foundPrivacyAndSecurityFramework == null) {
                    listing.getErrorMessages().add("Certification " + cert.getNumber()
                    + " contains Privacy and Security Framework value '" + formattedPrivacyAndSecurityFramework
                    + "' which must match one of " + PrivacyAndSecurityFrameworkConcept.getFormattedValues());
                }
            }
            if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                for (PendingCertificationResultAdditionalSoftwareDTO asDto : cert.getAdditionalSoftware()) {
                    if (!StringUtils.isEmpty(asDto.getChplId()) && asDto.getCertifiedProductId() == null) {
                        listing.getErrorMessages().add("No CHPL product was found matching additional software "
                                + asDto.getChplId() + " for " + cert.getNumber());
                    }
                }
            }
        }
    }
}
