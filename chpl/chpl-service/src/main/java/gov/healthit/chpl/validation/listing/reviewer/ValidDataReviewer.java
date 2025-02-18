package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.CertifiedProductUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("validDataReviewer")
public class ValidDataReviewer extends PermissionBasedReviewer {
    private CertifiedProductUtil certifiedProductUtil;

    @Autowired
    public ValidDataReviewer(CertifiedProductUtil certifiedProductUtil, ErrorMessageUtil msgUtil,
            ResourcePermissionsFactory resourcePermissionsFactory) {
        super(msgUtil, resourcePermissionsFactory);
        this.certifiedProductUtil = certifiedProductUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (BooleanUtils.isTrue(cert.getSuccess())) {
                if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                    for (CertificationResultAdditionalSoftware asDto : cert.getAdditionalSoftware()) {
                        if (asDto.getCertifiedProductId() == null
                                && !StringUtils.isEmpty(asDto.getCertifiedProductNumber())) {
                            try {
                                boolean exists = certifiedProductUtil.chplIdExists(asDto.getCertifiedProductNumber());
                                if (!exists) {
                                    addDataCriterionError(listing, cert,
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
