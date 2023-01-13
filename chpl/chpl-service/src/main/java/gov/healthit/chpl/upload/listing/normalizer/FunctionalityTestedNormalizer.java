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

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedDAO;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class FunctionalityTestedNormalizer {
    private static final String PRACTICE_TYPE_ID_KEY = "id";

    private FunctionalityTestedDAO functionalityTestedDao;
    private FunctionalityTestedManager functionalityTestedManager;
    private ResourcePermissions resourcePermissions;
    private List<RestrictedCriteriaFunctionalityTested> restrictedCriteriaFunctionalitiesTested;

    @Autowired
    public FunctionalityTestedNormalizer(FunctionalityTestedDAO functionalityTestedDao,
            FunctionalityTestedManager functionalityTestedManager,
            ResourcePermissions resourcePermissions,
            @Value("${functionalitiesTested.restrictions}") String jsonRestrictions) {
        this.functionalityTestedDao = functionalityTestedDao;
        this.functionalityTestedManager = functionalityTestedManager;
        this.resourcePermissions = resourcePermissions;
        initRestrictedCriteriaFunctionalitiesTested(jsonRestrictions);
    }

    private void initRestrictedCriteriaFunctionalitiesTested(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class,
                    RestrictedCriteriaFunctionalityTested.class);
            restrictedCriteriaFunctionalitiesTested = mapper.readValue(json, javaType);
        } catch (Exception ex) {
            LOGGER.error("Unable to convert functionalitiesTested.restrictions to Java object.", ex);
        }
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInFunctionalitiesTestedData(listing, certResult));

            listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getFunctionalitiesTested() != null && certResult.getFunctionalitiesTested().size() > 0)
                .forEach(certResult -> removeRestrictedFunctionalitiesTestedBasedOnUserRule(certResult));
        }

    }

    private void fillInFunctionalitiesTestedData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        populateAllowedFunctionalitiesTested(listing, certResult);
        populateFunctionalitiesTestedIds(listing, certResult.getFunctionalitiesTested());
    }

    @Deprecated
    private void populateAllowedFunctionalitiesTested(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        certResult.setAllowedTestFunctionalities(getAvailableFunctionalitiesTested(listing, certResult));
    }

    @Deprecated
    private List<FunctionalityTested> getAvailableFunctionalitiesTested(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        String edition = MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY);
        Long practiceTypeId = MapUtils.getLong(listing.getPracticeType(), PRACTICE_TYPE_ID_KEY);
        if (certResult != null && certResult.getCriterion() != null
                && certResult.getCriterion().getId() != null) {
            return functionalityTestedManager.getFunctionalitiesTested(certResult.getCriterion().getId(), edition, practiceTypeId);
        }
        return new ArrayList<FunctionalityTested>();
    }

    private void populateFunctionalitiesTestedIds(CertifiedProductSearchDetails listing,
            List<CertificationResultFunctionalityTested> functionalitiesTested) {
        if (functionalitiesTested != null && functionalitiesTested.size() > 0) {
            functionalitiesTested.stream()
                .forEach(functionalityTested -> populateFunctionalityTestedId(listing, functionalityTested));
        }
    }

    private void populateFunctionalityTestedId(CertifiedProductSearchDetails listing,
            CertificationResultFunctionalityTested functionalityTested) {
        if (!StringUtils.isEmpty(functionalityTested.getName())
                && MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY) != null) {
            Long editionId = null;
            try {
                editionId = MapUtils.getLong(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_ID_KEY);
            } catch (NumberFormatException ex) {
                LOGGER.error("Could not get edition id as a number.", ex);
            }

            if (editionId != null) {
                FunctionalityTested foundFunctionalityTested =
                        functionalityTestedDao.getByNumberAndEdition(functionalityTested.getName(), editionId);
                if (foundFunctionalityTested != null) {
                    functionalityTested.setFunctionalityTestedId(foundFunctionalityTested.getId());
                }
            }
        }
    }

    private void removeRestrictedFunctionalitiesTestedBasedOnUserRule(CertificationResult certResult) {
        Iterator<CertificationResultFunctionalityTested> functionalityTestedIter = certResult.getFunctionalitiesTested().listIterator();
        while (functionalityTestedIter.hasNext()) {
            CertificationResultFunctionalityTested currFunctionalityTested = functionalityTestedIter.next();
            if (currFunctionalityTested.getFunctionalityTestedId() != null) {
                Optional<RestrictedFunctionalityTested> restrictedFunctionalityTested
                    = findRestrictedFunctionalityTested(certResult.getCriterion().getId(), currFunctionalityTested.getFunctionalityTestedId());
                if (restrictedFunctionalityTested.isPresent()) {
                    functionalityTestedIter.remove();
                }
            }
        }
    }

    private Optional<RestrictedFunctionalityTested> findRestrictedFunctionalityTested(Long criterionId, Long functionalityTestedId) {
        Optional<RestrictedCriteriaFunctionalityTested> foundBasedOnCriteriaId = restrictedCriteriaFunctionalitiesTested.stream()
                .filter(restrictedFunctionalityTested -> restrictedFunctionalityTested.getCriterionId().equals(criterionId))
                .findAny();

        if (foundBasedOnCriteriaId.isPresent()) {
            // Is there a match on the functionality tested
            return foundBasedOnCriteriaId.get().getRestrictedFunctionalitiesTested().stream()
                    .filter(restrictedFunctionalityTested -> restrictedFunctionalityTested.getFunctionalityTestedId().equals(functionalityTestedId)
                            && !resourcePermissions.doesUserHaveRole(restrictedFunctionalityTested.allowedRoleNames))
                    .findAny();
        } else {
            return Optional.empty();
        }
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
