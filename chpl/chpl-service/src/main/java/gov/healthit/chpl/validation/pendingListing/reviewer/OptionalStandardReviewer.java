package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultOptionalStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.service.CertificationCriterionService;
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
            Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap = null;
            try {
                optionalStandardCriteriaMap = optionalStandardDao.getAllOptionalStandardCriteriaMap().stream()
                        .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));
            } catch (EntityRetrievalException e) {
                listing.getErrorMessages().add("Could not validate Optional Standard");
                return;
            }
            OptionalStandard foundOptionalStandard = optionalStandardDao.getByCitation(optionalStandard.getCitation());
            if (foundOptionalStandard == null) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.criteria.optionalStandardNotFound",
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        optionalStandard.getCitation()));
            } else if (!isOptionalStandardValidForCriteria(foundOptionalStandard.getId(), certResult.getCriterion().getId(), optionalStandardCriteriaMap)) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.optionalStandard.invalidCriteria",
                        foundOptionalStandard.getCitation(), Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private boolean isOptionalStandardValidForCriteria(Long osId, Long criteriaId, Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap) {
        if (optionalStandardCriteriaMap.containsKey(criteriaId)) {
            return optionalStandardCriteriaMap.get(criteriaId).stream()
                    .filter(oscm -> oscm.getOptionalStandard().getId().equals(osId))
                    .findAny()
                    .isPresent();
        } else {
            return false;
        }
    }
}
