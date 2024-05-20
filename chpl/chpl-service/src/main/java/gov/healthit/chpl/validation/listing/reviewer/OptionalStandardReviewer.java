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
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("optionalStandardReviewer")
public class OptionalStandardReviewer extends PermissionBasedReviewer implements Reviewer {
    private OptionalStandardDAO optionalStandardDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public OptionalStandardReviewer(OptionalStandardDAO optionalStandardDAO, ErrorMessageUtil errorMessageUtil, ResourcePermissionsFactory resourcePermissionsFactory) {
        super(errorMessageUtil, resourcePermissionsFactory);
        this.optionalStandardDAO = optionalStandardDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationResult> certificationResultsWithOptionalStandards = listing.getCertificationResults().stream()
                .filter(cr -> BooleanUtils.isTrue(cr.getSuccess())
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
                if (!isOptionalStandardValidForCriteria(cros.getOptionalStandard().getId(), cr.getCriterion().getId(), optionalStandardCriteriaMap)) {
                    String error = errorMessageUtil.getMessage("listing.criteria.optionalStandard.invalidCriteria",
                            cros.getOptionalStandard().getDisplayValue(), CertificationCriterionService.formatCriteriaNumber(cr.getCriterion()));
                    addBusinessCriterionError(listing, cr, error);
                }
            }
        }
    }

    private void populateOptionalStandardFields(CertificationResultOptionalStandard cros, Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap) {
        if (cros.getOptionalStandard().getId() != null) {
            Optional<OptionalStandard> optionalStandard = getOptionalStandard(cros.getOptionalStandard().getId(), optionalStandardCriteriaMap);
            if (optionalStandard.isPresent()) {
                cros.getOptionalStandard().setCitation(optionalStandard.get().getCitation());
                cros.getOptionalStandard().setDescription(optionalStandard.get().getDescription());
                cros.getOptionalStandard().setDisplayValue(optionalStandard.get().getDisplayValue());
            }
        } else if (!StringUtils.isEmpty(cros.getOptionalStandard().getDisplayValue())) {
            Optional<OptionalStandard> optionalStandard = getOptionalStandard(cros.getOptionalStandard().getDisplayValue(), optionalStandardCriteriaMap);
            if (optionalStandard.isPresent()) {
                cros.getOptionalStandard().setId(optionalStandard.get().getId());
                cros.getOptionalStandard().setCitation(optionalStandard.get().getCitation());
                cros.getOptionalStandard().setDescription(optionalStandard.get().getDescription());
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

    private Optional<OptionalStandard> getOptionalStandard(String displayValue, Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap) {
        return optionalStandardCriteriaMap.values().stream()
                .flatMap(List::stream)
                .map(oscm -> oscm.getOptionalStandard())
                .filter(os -> os.getDisplayValue().equals(displayValue))
                .findAny();
    }
}
