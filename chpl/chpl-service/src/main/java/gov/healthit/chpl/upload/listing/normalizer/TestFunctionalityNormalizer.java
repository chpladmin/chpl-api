package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.manager.TestingFunctionalityManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class TestFunctionalityNormalizer {
    private static final String PRACTICE_TYPE_ID_KEY = "id";

    private TestFunctionalityDAO testFunctionalityDao;
    private TestingFunctionalityManager testFunctionalityManager;
    private ResourcePermissions resourcePermissions;
    private List<RestrictedCriteriaTestFunctionality> restrictedCriteriaTestFunctionality;

    @Autowired
    public TestFunctionalityNormalizer(TestFunctionalityDAO testFunctionalityDao,
            TestingFunctionalityManager testFunctionalityManager,
            ResourcePermissions resourcePermissions,
            @Value("${testFunctionalities.restrictions}") String jsonRestrictions) {
        this.testFunctionalityDao = testFunctionalityDao;
        this.testFunctionalityManager = testFunctionalityManager;
        this.resourcePermissions = resourcePermissions;
        initRestrictedCriteriaTestFunctionality(jsonRestrictions);
    }

    private void initRestrictedCriteriaTestFunctionality(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class,
                    RestrictedCriteriaTestFunctionality.class);
            restrictedCriteriaTestFunctionality = mapper.readValue(json, javaType);
        } catch (Exception ex) {
            LOGGER.error("Unable to convert testFunctionalities.restrictions to Java object.", ex);
        }
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInTestFunctionalityData(listing, certResult));

            listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getTestFunctionality() != null && certResult.getTestFunctionality().size() > 0)
                .forEach(certResult -> removeRestrictedTestFunctionalityBasedOnUserRule(certResult));
        }

    }

    private void fillInTestFunctionalityData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        populateAllowedTestFunctionalities(listing, certResult);
        populateTestFunctionalityIds(listing, certResult.getTestFunctionality());
    }

    private void populateAllowedTestFunctionalities(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        certResult.setAllowedTestFunctionalities(getAvailableTestFunctionalities(listing, certResult));
    }

    private List<TestFunctionality> getAvailableTestFunctionalities(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        String edition = MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY);
        Long practiceTypeId = MapUtils.getLong(listing.getPracticeType(), PRACTICE_TYPE_ID_KEY);
        if (certResult != null && certResult.getCriterion() != null
                && certResult.getCriterion().getId() != null) {
            return testFunctionalityManager.getTestFunctionalities(certResult.getCriterion().getId(), edition, practiceTypeId);
        }
        return new ArrayList<TestFunctionality>();
    }

    private void populateTestFunctionalityIds(CertifiedProductSearchDetails listing,
            List<CertificationResultTestFunctionality> testFunctionalities) {
        if (testFunctionalities != null && testFunctionalities.size() > 0) {
            testFunctionalities.stream()
                .forEach(testFunctionality -> populateTestFunctionalityId(listing, testFunctionality));
        }
    }

    private void populateTestFunctionalityId(CertifiedProductSearchDetails listing,
            CertificationResultTestFunctionality testFunctionality) {
        if (!StringUtils.isEmpty(testFunctionality.getName())
                && MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY) != null) {
            Long editionId = null;
            try {
                editionId = MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY);
            } catch (NumberFormatException ex) {
                LOGGER.error("Could not get edition id as a number.", ex);
            }

            if (editionId != null) {
                TestFunctionalityDTO testFunctionalityDto =
                        testFunctionalityDao.getByNumberAndEdition(testFunctionality.getName(), editionId);
                if (testFunctionalityDto != null) {
                    testFunctionality.setTestFunctionalityId(testFunctionalityDto.getId());
                }
            }
        }
    }

    private void removeRestrictedTestFunctionalityBasedOnUserRule(CertificationResult certResult) {
        Iterator<CertificationResultTestFunctionality> testFunctionalityIter = certResult.getTestFunctionality().listIterator();
        while (testFunctionalityIter.hasNext()) {
            CertificationResultTestFunctionality currTestFunctionality = testFunctionalityIter.next();
            if (currTestFunctionality.getTestFunctionalityId() != null) {
                Optional<RestrictedTestFunctionality> restrictedTestFunctionality
                    = findRestrictedTestFunctionality(certResult.getCriterion().getId(), currTestFunctionality.getTestFunctionalityId());
                if (restrictedTestFunctionality.isPresent()) {
                    testFunctionalityIter.remove();
                }
            }
        }
    }

    private Optional<RestrictedTestFunctionality> findRestrictedTestFunctionality(Long criteriaId, Long testFunctionalityId) {
        Optional<RestrictedCriteriaTestFunctionality> foundBasedOnCriteriaId = restrictedCriteriaTestFunctionality.stream()
                .filter(restrictedTestFunctionality -> restrictedTestFunctionality.getCriteriaId().equals(criteriaId))
                .findAny();

        if (foundBasedOnCriteriaId.isPresent()) {
            // Is there a match on the test functionality
            return foundBasedOnCriteriaId.get().getRestrictedTestFunctionalities().stream()
                    .filter(restrictedTestFunctionality -> restrictedTestFunctionality.getTestFunctionalityId().equals(testFunctionalityId)
                            && !resourcePermissions.doesUserHaveRole(restrictedTestFunctionality.allowedRoleNames))
                    .findAny();
        } else {
            return Optional.empty();
        }
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
