package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Component("testFunctionalityAllowedByRoleReviewer")
@Log4j2
public class TestFunctionalityAllowedByRoleReviewer implements ComparisonReviewer {

    private FF4j ff4j;
    private Environment env;

    @Autowired
    public TestFunctionalityAllowedByRoleReviewer(FF4j ff4j, Environment env) {
        this.ff4j = ff4j;
        this.env = env;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        log.info("*****************  Made it to the reviewer *****************");

        for (CertificationResult updatedCr : updatedListing.getCertificationResults()) {
            Optional<CertificationResult> existingCr = findCertificationResult(existingListing, updatedCr.getId());
            if (existingCr.isPresent()) {
                Optional<List<CertificationResultTestFunctionality>> listUpdateCrtfs = Optional
                        .ofNullable(updatedCr.getTestFunctionality());
                Optional<List<CertificationResultTestFunctionality>> listExistingCrtfs = Optional
                        .ofNullable(existingCr.get().getTestFunctionality());

                List<CertificationResultTestFunctionality> addedCrtfs = getAddedCrtfs(listUpdateCrtfs, listExistingCrtfs);

                addedCrtfs.stream()
                        .forEach(x -> log.info("Added this CRTF: " + x.toString()));
                addedCrtfs.stream()
                        .forEach(x -> isTestFunctionalityChangeAllowedBasedOnRole(updatedCr, x));

                List<CertificationResultTestFunctionality> removedCrtfs = getRemovedCrtfs(listUpdateCrtfs, listExistingCrtfs);

                removedCrtfs.stream()
                        .forEach(x -> log.info("Removed this CRTF: " + x.toString()));

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

    private boolean isTestFunctionalityChangeAllowedBasedOnRole(CertificationResult cr,
            CertificationResultTestFunctionality crtf) {
        Optional<RestrictedTestFunctionality> restrictedTestFunctionality = findRestrictedTestFunctionality(
                cr.getCriterion().getId(), crtf.getTestFunctionalityId());
        if (restrictedTestFunctionality.isPresent()) {
            log.info("Not Allowed based on Roles: " + restrictedTestFunctionality.get().toString());
        } else {
            log.info("No rule based on {" + cr.getCriterion().getId() + ", " + crtf.getTestFunctionalityId() + "}");
        }
        return true;
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
