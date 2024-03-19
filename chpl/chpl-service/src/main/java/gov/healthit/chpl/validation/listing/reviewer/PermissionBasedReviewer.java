package gov.healthit.chpl.validation.listing.reviewer;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingPermissionBasedReviewer")
public abstract class PermissionBasedReviewer implements Reviewer {
    protected ErrorMessageUtil msgUtil;
    protected ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    public PermissionBasedReviewer(ErrorMessageUtil msgUtil, ResourcePermissionsFactory resourcePermissionsFactory) {
        this.msgUtil = msgUtil;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    public void addListingWarningsByPermission(CertifiedProductSearchDetails listing, List<String> errorMessages) {
        for (String message : errorMessages) {
            addListingWarningByPermission(listing, message);
        }
    }

    public void addListingWarningByPermission(CertifiedProductSearchDetails listing, String errorMessageName,
            Object... errorMessageArgs) {
        String message = msgUtil.getMessage(errorMessageName, errorMessageArgs);
        addListingWarningByPermission(listing, message);
    }

    public void addListingWarningByPermission(CertifiedProductSearchDetails listing, String message) {
        if (resourcePermissionsFactory.get().isUserRoleAdmin() || resourcePermissionsFactory.get().isUserRoleOnc()) {
            listing.addWarningMessage(message);
            // ACBs do not get any error or warning about removed criteria validation issues
        }
    }

    public void addBusinessCriterionError(
            CertifiedProductSearchDetails listing, CertificationResult certResult,
            String errorMessageName, Object... errorMessageArgs) {
        String message = msgUtil.getMessage(errorMessageName, errorMessageArgs);
        addBusinessCriterionError(listing, certResult, message);
    }

    public void addBusinessCriterionError(
            CertifiedProductSearchDetails listing, CertificationResult certResult,
            String message) {
        if (certResult.getCriterion() != null && (certResult.getCriterion().isRemoved() == null
                || certResult.getCriterion().isRemoved().equals(Boolean.FALSE))) {
            listing.addBusinessErrorMessage(message);
        }
    }

    public void addDataCriterionError(
            CertifiedProductSearchDetails listing, CertificationResult certResult,
            String errorMessageName, Object... errorMessageArgs) {
        String message = msgUtil.getMessage(errorMessageName, errorMessageArgs);
        addDataCriterionError(listing, certResult, message);
    }

    public void addDataCriterionError(
            CertifiedProductSearchDetails listing, CertificationResult certResult,
            String message) {
        if (certResult.getCriterion() != null && (certResult.getCriterion().isRemoved() == null
                || certResult.getCriterion().isRemoved().equals(Boolean.FALSE))) {
            listing.addDataErrorMessage(message);
        }
    }

    public Boolean isCertificationResultAttestedTo(CertificationResult cert) {
        return BooleanUtils.isTrue(cert.getSuccess());
    }

    @Override
    public abstract void review(CertifiedProductSearchDetails listing);
}
