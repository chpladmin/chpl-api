package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultOptionalStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("pendingOptionalStandardReviewer")
public class OptionalStandardReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;
    private OptionalStandardDAO optionalStandardDao;

    @Autowired
    public OptionalStandardReviewer(OptionalStandardDAO optionalStandardDao, ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
        this.optionalStandardDao = optionalStandardDao;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        listing.getCertificationCriterion().stream()
            .filter(cert -> (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().equals(Boolean.TRUE)))
            .filter(cert -> (cert.getOptionalStandards() != null && cert.getOptionalStandards().size() > 0))
            .forEach(certResult -> certResult.getOptionalStandards().stream()
                    .forEach(optionalStandard -> reviewOptionalStandard(listing, certResult, optionalStandard)));
    }

    private void reviewOptionalStandard(PendingCertifiedProductDTO listing, PendingCertificationResultDTO certResult,
            PendingCertificationResultOptionalStandardDTO optionalStandard) {
        if (StringUtils.isEmpty(optionalStandard.getCitation())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingOptionalStandardName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else {
            OptionalStandard foundOptionalStandard = optionalStandardDao.getByCitation(optionalStandard.getCitation());
            if (foundOptionalStandard == null) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.criteria.optionalStandardNotFound",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        optionalStandard.getCitation()));
            }
        }
    }
}
