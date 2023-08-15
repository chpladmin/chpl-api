package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Component("functionalityTestedAllowedByRoleReviewer")
@Log4j2
public class FunctionalityTestedAllowedByRoleReviewer implements ComparisonReviewer {

    private ErrorMessageUtil errorMessages;
    private ResourcePermissions permissions;
    private String jsonRestrictions;

    @Autowired
    public FunctionalityTestedAllowedByRoleReviewer(ResourcePermissions permissions, ErrorMessageUtil errorMessages,
            @Value("${functionalitiesTested.restrictions}") String jsonRestrictions) {

        this.errorMessages = errorMessages;
        this.permissions = permissions;
        this.jsonRestrictions = jsonRestrictions;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        for (CertificationResult updatedCr : updatedListing.getCertificationResults()) {
            Optional<CertificationResult> existingCr = findCertificationResult(existingListing, updatedCr.getId());
            if (existingCr.isPresent()) {
                Optional<List<CertificationResultFunctionalityTested>> listUpdateCrtfs = Optional.ofNullable(updatedCr.getFunctionalitiesTested());
                Optional<List<CertificationResultFunctionalityTested>> listExistingCrtfs = Optional.ofNullable(existingCr.get().getFunctionalitiesTested());

                List<CertificationResultFunctionalityTested> addedCrtfs = getAddedFunctionalitiesTested(listUpdateCrtfs, listExistingCrtfs);

                // Only check removed CertificationResultFunctionalityTested if the criteria is attested to
                List<CertificationResultFunctionalityTested> removedCrtfs = new ArrayList<CertificationResultFunctionalityTested>();
                if (BooleanUtils.isTrue(updatedCr.isSuccess())) {
                    removedCrtfs = getRemovedFunctionalitiesTested(listUpdateCrtfs, listExistingCrtfs);
                }

                List<CertificationResultFunctionalityTested> allEditedCrtfs = Stream.concat(addedCrtfs.stream(), removedCrtfs.stream())
                        .collect(Collectors.toList());

                allEditedCrtfs.stream()
                        .forEach(crtf -> {
                            if (!isFunctionalityTestedChangeAllowedBasedOnRole(updatedCr.getCriterion().getId(), crtf.getFunctionalityTested().getId())) {
                                updatedListing.addBusinessErrorMessage(errorMessages.getMessage("listing.criteria.functionalityTestedPermissionError",
                                        crtf.getFunctionalityTested().getValue(), Util.formatCriteriaNumber(updatedCr.getCriterion())));
                            }
                        });
            }
        }
    }

    private List<CertificationResultFunctionalityTested> getRemovedFunctionalitiesTested(Optional<List<CertificationResultFunctionalityTested>> listA,
            Optional<List<CertificationResultFunctionalityTested>> listB) {
        // This will get the functionalities tested removed - items in listB not in ListA
        return subtractLists(
                listB.isPresent() ? listB.get() : new ArrayList<CertificationResultFunctionalityTested>(),
                listA.isPresent() ? listA.get() : new ArrayList<CertificationResultFunctionalityTested>());
    }

    private List<CertificationResultFunctionalityTested> getAddedFunctionalitiesTested(Optional<List<CertificationResultFunctionalityTested>> listA,
            Optional<List<CertificationResultFunctionalityTested>> listB) {
        // This will get the functionalities tested added - items in listA not in ListB
        return subtractLists(
                listA.isPresent() ? listA.get() : new ArrayList<CertificationResultFunctionalityTested>(),
                listB.isPresent() ? listB.get() : new ArrayList<CertificationResultFunctionalityTested>());
    }

    private List<CertificationResultFunctionalityTested> subtractLists(List<CertificationResultFunctionalityTested> listA,
            List<CertificationResultFunctionalityTested> listB) {

        Predicate<CertificationResultFunctionalityTested> notInListB = crtfFromA -> !listB.stream()
                .anyMatch(crtf -> crtfFromA.matches(crtf));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private Optional<CertificationResult> findCertificationResult(CertifiedProductSearchDetails listing,
            Long certificationResultId) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getId().equals(certificationResultId))
                .findFirst();
    }

    private boolean isFunctionalityTestedChangeAllowedBasedOnRole(Long criteriaId, Long functionalityTestedId) {
        Optional<RestrictedFunctionalityTested> restrictedFunctionalityTested = findRestrictedFunctionalityTested(criteriaId,
                functionalityTestedId);
        if (restrictedFunctionalityTested.isPresent()) {
            return permissions.doesUserHaveRole(restrictedFunctionalityTested.get().getAllowedRoleNames());
        } else {
            return true;
        }
    }

    private Optional<RestrictedFunctionalityTested> findRestrictedFunctionalityTested(Long criterionId, Long functionalityTestedId) {
        Optional<RestrictedCriteriaFunctionalityTested> foundBasedOnCriteriaId = getRestrictedCriteriaFunctionalityTested().stream()
                .filter(x -> x.getCriterionId().equals(criterionId))
                .findAny();

        if (foundBasedOnCriteriaId.isPresent()) {
            // Is there a match on the functionality tested
            return foundBasedOnCriteriaId.get().getRestrictedFunctionalitiesTested().stream()
                    .filter(x -> x.getFunctionalityTestedId().equals(functionalityTestedId))
                    .findAny();
        } else {
            return Optional.empty();
        }
    }

    private List<RestrictedCriteriaFunctionalityTested> getRestrictedCriteriaFunctionalityTested() {
        List<RestrictedCriteriaFunctionalityTested> restrictedCriteria = new ArrayList<RestrictedCriteriaFunctionalityTested>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class,
                    RestrictedCriteriaFunctionalityTested.class);
            restrictedCriteria = mapper.readValue(jsonRestrictions, javaType);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return restrictedCriteria;
    }

    @Data
    @ToString
    static class RestrictedCriteriaFunctionalityTested {
        private Long criterionId;
        private List<RestrictedFunctionalityTested> restrictedFunctionalitiesTested;
    }

    @Data
    @ToString
    static class RestrictedFunctionalityTested {
        private Long functionalityTestedId;
        private List<String> allowedRoleNames;
    }

}
