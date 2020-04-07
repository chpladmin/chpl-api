package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.PrivacyAndSecurityFrameworkConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("validDataReviewer")
public class ValidDataReviewer extends PermissionBasedReviewer {

    private ChplProductNumberUtil chplNumberUtil;

    @Autowired
    public ValidDataReviewer(@Lazy ChplProductNumberUtil chplNumberUtil, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.chplNumberUtil = chplNumberUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess().equals(Boolean.TRUE)) {
                if (!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    String formattedPrivacyAndSecurityFramework = CertificationResult
                            .formatPrivacyAndSecurityFramework(cert.getPrivacySecurityFramework());
                    PrivacyAndSecurityFrameworkConcept foundPrivacyAndSecurityFramework = PrivacyAndSecurityFrameworkConcept
                            .getValue(formattedPrivacyAndSecurityFramework);
                    if (foundPrivacyAndSecurityFramework == null) {
                        addCriterionErrorOrWarningByPermission(listing, cert,
                                "listing.criteria.invalidPrivacySecurityFramework",
                                Util.formatCriteriaNumber(cert.getCriterion()),
                                formattedPrivacyAndSecurityFramework, PrivacyAndSecurityFrameworkConcept.getFormattedValues());
                    }
                }

                if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                    for (CertificationResultAdditionalSoftware asDto : cert.getAdditionalSoftware()) {
                        if (asDto.getCertifiedProductId() == null
                                && !StringUtils.isEmpty(asDto.getCertifiedProductNumber())) {
                            try {
                                boolean exists = chplNumberUtil.chplIdExists(asDto.getCertifiedProductNumber());
                                if (!exists) {
                                    addCriterionErrorOrWarningByPermission(listing, cert,
                                            "listing.criteria.invalidAdditionalSoftware", asDto.getCertifiedProductNumber(),
                                            Util.formatCriteriaNumber(cert.getCriterion()));
                                }
                            } catch (EntityRetrievalException e) {
                            }
                        }
                    }
                }
            }
        }
    }
}
