package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.PrivacyAndSecurityFrameworkConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductManager;

@Component("validDataReviewer")
public class ValidDataReviewer implements Reviewer {

    @Autowired private CertifiedProductManager cpManager;

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
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
                for (CertificationResultAdditionalSoftware asDto : cert.getAdditionalSoftware()) {
                    if (asDto.getCertifiedProductId() == null
                            && !StringUtils.isEmpty(asDto.getCertifiedProductNumber())) {
                        try {
                            boolean exists = cpManager.chplIdExists(asDto.getCertifiedProductNumber());
                            if (!exists) {
                                listing.getErrorMessages().add("No CHPL product was found matching additional software "
                                        + asDto.getCertifiedProductNumber() + " for " + cert.getNumber());
                            }
                        } catch (EntityRetrievalException e) { }
                    }
                }
            }
        }
    }
}
