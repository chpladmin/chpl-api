package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Component("pendingTestFunctionalityAllowedByRoleReviewer")
@Log4j2
public class TestFunctionalityAllowedByRoleReviewer implements Reviewer {

    private FF4j ff4j;
    private Environment env;
    private List<RestrictedCriteriaTestFunctionality> restrictedCriteriaTestFunctionality;
    private ResourcePermissions resourcePermissions;

    @Autowired
    public TestFunctionalityAllowedByRoleReviewer(FF4j ff4j, Environment env, ResourcePermissions resourcePermissions) {
        this.ff4j = ff4j;
        this.env = env;
        this.resourcePermissions = resourcePermissions;
    }

    @PostConstruct
    public void setup() {
        restrictedCriteriaTestFunctionality = getRestrictedCriteriaTestFunctionality();
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            for (PendingCertificationResultDTO cr : listing.getCertificationCriterion()) {
                if (cr.getTestFunctionality() != null) {
                    cr.getTestFunctionality().removeIf(
                            crtf -> findRestrictedTestFunctionality(
                                    cr.getCriterion().getId(),
                                    crtf.getTestFunctionalityId()).isPresent());
                }
            }
        }
    }

    private Optional<RestrictedTestFunctionality> findRestrictedTestFunctionality(Long criteriaId, Long testFunctionalityId) {
        Optional<RestrictedCriteriaTestFunctionality> foundBasedOnCriteriaId = restrictedCriteriaTestFunctionality.stream()
                .filter(x -> x.getCriteriaId().equals(criteriaId))
                .findAny();

        if (foundBasedOnCriteriaId.isPresent()) {
            // Is there a match on the test functionality
            return foundBasedOnCriteriaId.get().getRestrictedTestFunctionalities().stream()
                    .filter(x -> x.getTestFunctionalityId().equals(testFunctionalityId)
                            && !resourcePermissions.doesUserHaveRole(x.allowedRoleNames))
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
