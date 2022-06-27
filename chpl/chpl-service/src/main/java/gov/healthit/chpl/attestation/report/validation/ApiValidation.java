package gov.healthit.chpl.attestation.report.validation;

import java.util.List;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class ApiValidation extends ValidationRule<AttestationValidationContext> {

    @Override
    public boolean isValid(AttestationValidationContext context) {
        List<Long> apiCriteriaIds = context.getApiCriteria().stream()
                .map(crit -> crit.getId())
                .toList();

        return context.getListings().stream()
                .filter(listing -> context.getActiveStatuses().contains(listing.getCertificationStatus().getName()))
                .flatMap(listing -> listing.getCriteriaMet().stream())
                .map(criteriaMet -> criteriaMet.getId())
                .filter(criteriaMetId -> apiCriteriaIds.contains(criteriaMetId))
                .findAny()
                .isPresent();
    }
}
