package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Component("testFunctionalityAllowedByRoleReviewer")
@Log4j2
public class TestFunctionalityAllowedByRoleReviewer implements ComparisonReviewer {

    private FF4j ff4j;
    private Environment env;
    private ErrorMessageUtil errorMessages;
    private ResourcePermissions permissions;

    @Autowired
    public TestFunctionalityAllowedByRoleReviewer(FF4j ff4j, Environment env, ResourcePermissions permissions,
            ErrorMessageUtil errorMessages) {
        this.ff4j = ff4j;
        this.env = env;
        this.errorMessages = errorMessages;
        this.permissions = permissions;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            for (CertificationResult updatedCr : updatedListing.getCertificationResults()) {
                Optional<CertificationResult> existingCr = findCertificationResult(existingListing, updatedCr.getId());
                if (existingCr.isPresent()) {
                    Optional<List<CertificationResultTestFunctionality>> listUpdateCrtfs = Optional
                            .ofNullable(updatedCr.getTestFunctionality());
                    Optional<List<CertificationResultTestFunctionality>> listExistingCrtfs = Optional
                            .ofNullable(existingCr.get().getTestFunctionality());

                    List<CertificationResultTestFunctionality> addedCrtfs = getAddedCrtfs(listUpdateCrtfs, listExistingCrtfs);
                    List<CertificationResultTestFunctionality> removedCrtfs = getRemovedCrtfs(listUpdateCrtfs, listExistingCrtfs);
                    List<CertificationResultTestFunctionality> allEditedCrtfs = Stream
                            .concat(addedCrtfs.stream(), removedCrtfs.stream())
                            .collect(Collectors.toList());

                    allEditedCrtfs.stream()
                            .forEach(crtf -> {
                                if (!isTestFunctionalityChangeAllowedBasedOnRole(updatedCr.getCriterion().getId(),
                                        crtf.getTestFunctionalityId())) {
                                    updatedListing.getErrorMessages()
                                            .add(errorMessages.getMessage("listing.criteria.testFunctionalityPermissionError",
                                                    crtf.getName(), Util.formatCriteriaNumber(updatedCr.getCriterion())));
                                }
                            });
                }
            }
        }
    }

    private List<CertificationResultTestFunctionality> getRemovedCrtfs(Optional<List<CertificationResultTestFunctionality>> listA,
            Optional<List<CertificationResultTestFunctionality>> listB) {
        // This will get the test functionalities removed - items in listB not in ListA
        return subtractLists(
                listB.isPresent() ? listB.get() : new ArrayList<CertificationResultTestFunctionality>(),
                listA.isPresent() ? listA.get() : new ArrayList<CertificationResultTestFunctionality>());
    }

    private List<CertificationResultTestFunctionality> getAddedCrtfs(Optional<List<CertificationResultTestFunctionality>> listA,
            Optional<List<CertificationResultTestFunctionality>> listB) {
        // This will get the test functionalities added - items in listA not in ListB
        return subtractLists(
                listA.isPresent() ? listA.get() : new ArrayList<CertificationResultTestFunctionality>(),
                listB.isPresent() ? listB.get() : new ArrayList<CertificationResultTestFunctionality>());
    }

    private List<CertificationResultTestFunctionality> subtractLists(List<CertificationResultTestFunctionality> listA,
            List<CertificationResultTestFunctionality> listB) {

        Predicate<CertificationResultTestFunctionality> notInListB = crtfFromA -> !listB.stream()
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

    private boolean isTestFunctionalityChangeAllowedBasedOnRole(Long criteriaId, Long testFunctionalityId) {
        Optional<RestrictedTestFunctionality> restrictedTestFunctionality = findRestrictedTestFunctionality(criteriaId,
                testFunctionalityId);
        if (restrictedTestFunctionality.isPresent()) {
            return permissions.doesUserHaveRole(restrictedTestFunctionality.get().getAllowedRoleNames());
        } else {
            return true;
        }
    }

    private Optional<RestrictedTestFunctionality> findRestrictedTestFunctionality(Long criteriaId, Long testFunctionalityId) {
        Optional<RestrictedCriteriaTestFunctionality> foundBasedOnCriteriaId = getRestrictedCriteriaTestFunctionality().stream()
                .filter(x -> x.getCriteriaId().equals(criteriaId))
                .findAny();

        if (foundBasedOnCriteriaId.isPresent()) {
            // Is there a match on the test functionality
            return foundBasedOnCriteriaId.get().getRestrictedTestFunctionalities().stream()
                    .filter(x -> x.getTestFunctionalityId().equals(testFunctionalityId))
                    .findAny();
        } else {
            return Optional.empty();
        }
    }

    private List<RestrictedCriteriaTestFunctionality> getRestrictedCriteriaTestFunctionality() {
        List<RestrictedCriteriaTestFunctionality> restrictedCriteria = new ArrayList<RestrictedCriteriaTestFunctionality>();
        String json = env.getProperty("testFunctionalities.restrictions");
        try {
            ObjectMapper mapper = new ObjectMapper();
            CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class,
                    RestrictedCriteriaTestFunctionality.class);
            restrictedCriteria = mapper.readValue(json, javaType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return restrictedCriteria;
    }

    @Data
    @ToString
    static class RestrictedCriteriaTestFunctionality {
        private Long criteriaId;
        private List<RestrictedTestFunctionality> restrictedTestFunctionalities;
    }

    @Data
    @ToString
    static class RestrictedTestFunctionality {
        private Long testFunctionalityId;
        private List<String> allowedRoleNames;
    }

}
