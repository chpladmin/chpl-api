package gov.healthit.chpl.validation.listing.reviewer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("optionalStandardReviewer")
public class OptionalStandardReviewer extends PermissionBasedReviewer implements Reviewer {
    private OptionalStandardDAO optionalStandardDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public OptionalStandardReviewer(OptionalStandardDAO optionalStandardDAO, ErrorMessageUtil errorMessageUtil, ResourcePermissions resourcePermissions) {
        super(errorMessageUtil, resourcePermissions);
        this.optionalStandardDAO = optionalStandardDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationResult> certificationResultsWithOptionalStandards = listing.getCertificationResults().stream()
                .filter(cr -> BooleanUtils.isTrue(cr.isSuccess())
                        && !CollectionUtils.isEmpty(cr.getOptionalStandards()))
                .collect(Collectors.toList());
        Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap = null;
        try {
            optionalStandardCriteriaMap = optionalStandardDAO.getAllOptionalStandardCriteriaMap().stream()
                    .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));
        } catch (EntityRetrievalException e) {
            listing.addDataErrorMessage("Could not validate Optional Standard");
            return;
        }
        for (CertificationResult cr : certificationResultsWithOptionalStandards) {
            for (CertificationResultOptionalStandard cros : cr.getOptionalStandards()) {
                populateOptionalStandardFields(cros, optionalStandardCriteriaMap);
                if (!isOptionalStandardValidForCriteria(cros.getOptionalStandardId(), cr.getCriterion().getId(), optionalStandardCriteriaMap)) {
                    String error = errorMessageUtil.getMessage("listing.criteria.optionalStandard.invalidCriteria",
                            cros.getCitation(), CertificationCriterionService.formatCriteriaNumber(cr.getCriterion()));
                    addBusinessCriterionError(listing, cr, error);
                }
            }
        }
    }

    private void populateOptionalStandardFields(CertificationResultOptionalStandard cros, Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap) {
        if (cros.getOptionalStandardId() != null) {
            Optional<OptionalStandard> optionalStandard = getOptionalStandard(cros.getOptionalStandardId(), optionalStandardCriteriaMap);
            if (optionalStandard.isPresent()) {
                cros.setCitation(optionalStandard.get().getCitation());
                cros.setDescription(optionalStandard.get().getDescription());
            }
        } else if (!StringUtils.isEmpty(cros.getCitation())) {
            Optional<OptionalStandard> optionalStandard = getOptionalStandard(cros.getCitation(), optionalStandardCriteriaMap);
            if (optionalStandard.isPresent()) {
                cros.setOptionalStandardId(optionalStandard.get().getId());
                cros.setDescription(optionalStandard.get().getDescription());
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

    private Optional<OptionalStandard> getOptionalStandard(Long optionalStandardId, Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap) {
        return optionalStandardCriteriaMap.values().stream()
                .flatMap(List::stream)
                .map(oscm -> oscm.getOptionalStandard())
                .filter(os -> os.getId().equals(optionalStandardId))
                .findAny();
    }

    private Optional<OptionalStandard> getOptionalStandard(String citation, Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap) {
        return optionalStandardCriteriaMap.values().stream()
                .flatMap(List::stream)
                .map(oscm -> oscm.getOptionalStandard())
                .filter(os -> os.getCitation().equals(citation))
                .findAny();
    }
}
