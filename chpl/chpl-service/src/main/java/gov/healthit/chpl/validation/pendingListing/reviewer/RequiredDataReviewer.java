package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("pendingRequiredDataReviewer")
public class RequiredDataReviewer extends PermissionBasedReviewer {
    protected CertificationResultRules certRules;

    @Autowired
    public RequiredDataReviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions,
            CertificationResultRules certRules) {
        super(msgUtil, resourcePermissions);
        this.certRules = certRules;
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        if (listing.getCertificationEditionId() == null && StringUtils.isEmpty(listing.getCertificationEdition())) {
            listing.getErrorMessages().add("Certification edition is required but was not found.");
        }
        if (listing.getCertificationDate() == null) {
            listing.getErrorMessages().add("Certification date was not found.");
        }
        if (listing.getCertificationBodyId() == null) {
            listing.getErrorMessages().add("ONC-ACB is required but was not found.");
        }
        if (ObjectUtils.isEmpty(listing.getUniqueId())) {
            listing.getErrorMessages().add("The product unique id is required.");
        }
        if (ObjectUtils.isEmpty(listing.getDeveloperName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.nameRequired"));
        }
        if (ObjectUtils.isEmpty(listing.getProductName())) {
            listing.getErrorMessages().add("A product name is required.");
        }
        if (ObjectUtils.isEmpty(listing.getProductVersion())) {
            listing.getErrorMessages().add("A product version is required.");
        }
        if (listing.getDeveloperAddress() != null) {
            if (ObjectUtils.isEmpty(listing.getDeveloperAddress().getLine1())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.streetRequired"));
            }
            if (ObjectUtils.isEmpty(listing.getDeveloperAddress().getCity())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.cityRequired"));
            }
            if (ObjectUtils.isEmpty(listing.getDeveloperAddress().getState())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.stateRequired"));
            }
            if (ObjectUtils.isEmpty(listing.getDeveloperAddress().getZipcode())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.zipRequired"));
            }
        } else {
            if (ObjectUtils.isEmpty(listing.getDeveloperStreetAddress())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.streetRequired"));
            }
            if (ObjectUtils.isEmpty(listing.getDeveloperCity())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.cityRequired"));
            }
            if (ObjectUtils.isEmpty(listing.getDeveloperState())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.stateRequired"));
            }
            if (ObjectUtils.isEmpty(listing.getDeveloperZipCode())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.zipRequired"));
            }
        }
        if (ObjectUtils.isEmpty(listing.getDeveloperWebsite())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.websiteRequired"));
        }
        if (ObjectUtils.isEmpty(listing.getDeveloperEmail())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.contact.emailRequired"));
        }
        if (ObjectUtils.isEmpty(listing.getDeveloperPhoneNumber())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.contact.phoneRequired"));
        }
        if (ObjectUtils.isEmpty(listing.getDeveloperContactName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.contact.nameRequired"));
        }

        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() == null) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.criteria.metInvalid",
                                Util.formatCriteriaNumber(cert.getCriterion())));
            } else if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().equals(Boolean.TRUE)) {
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() == null) {
                    addErrorIfCriterionIsNotRemoved(listing, cert,
                            "listing.criteria.missingGap",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }

                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() != null
                        && cert.getGap().booleanValue()) {
                    gapEligibleAndTrue = true;
                }

                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && (cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
                    addErrorIfCriterionIsNotRemoved(listing, cert, "listing.criteria.missingTestProcedure",
                            Util.formatCriteriaNumber(cert.getCriterion()));
                }
            }
        }
    }
}
