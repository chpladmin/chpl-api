package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import gov.healthit.chpl.criteriaattribute.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.criteriaattribute.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.criteriaattribute.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.criteriaattribute.functionalitytested.FunctionalityTestedManager;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
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
            clearDataForUnattestedCriteria(listing);
            listing.getCertificationResults().stream()
                .forEach(certResult -> fillInFunctionalitiesTestedData(listing, certResult));

            listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getFunctionalitiesTested() != null && certResult.getFunctionalitiesTested().size() > 0)
                .forEach(certResult -> removeRestrictedFunctionalitiesTestedBasedOnUserRule(certResult));
        }

    }

    private void clearDataForUnattestedCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> (certResult.isSuccess() == null || BooleanUtils.isFalse(certResult.isSuccess()))
                    && certResult.getFunctionalitiesTested() != null
                    && certResult.getFunctionalitiesTested().size() > 0)
            .forEach(unattestedCertResult -> unattestedCertResult.getFunctionalitiesTested().clear());
    }

    private void fillInFunctionalitiesTestedData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        populateAllowedFunctionalitiesTested(listing, certResult);
        populateFunctionalitiesTestedIds(listing, certResult, certResult.getFunctionalitiesTested());
    }

    @Deprecated
    private void populateAllowedFunctionalitiesTested(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        certResult.setAllowedTestFunctionalities(getAvailableFunctionalitiesTested(listing, certResult));
    }

    @Deprecated
    private List<FunctionalityTested> getAvailableFunctionalitiesTested(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        Long practiceTypeId = MapUtils.getLong(listing.getPracticeType(), PRACTICE_TYPE_ID_KEY);
        if (certResult != null && certResult.getCriterion() != null
                && certResult.getCriterion().getId() != null) {
            return functionalityTestedManager.getFunctionalitiesTested(certResult.getCriterion().getId(), practiceTypeId);
        }
        return new ArrayList<FunctionalityTested>();
    }

    private void populateFunctionalitiesTestedIds(CertifiedProductSearchDetails listing, CertificationResult certResult, List<CertificationResultFunctionalityTested> functionalitiesTested) {
        if (functionalitiesTested != null && functionalitiesTested.size() > 0) {
            functionalitiesTested.stream()
                .filter(functionalityTested -> functionalityTested.getId() == null)
                .forEach(functionalityTested -> populateFunctionalityTestedId(listing, certResult, functionalityTested));
        }
    }

    private void populateFunctionalityTestedId(CertifiedProductSearchDetails listing,
            CertificationResult certResult,
            CertificationResultFunctionalityTested functionalityTested) {
        if (!StringUtils.isEmpty(functionalityTested.getFunctionalityTested().getValue())) {
            FunctionalityTested foundFunctionalityTested =
                    getFunctionalityTested(functionalityTested.getFunctionalityTested().getValue(), certResult.getCriterion().getId());
            if (foundFunctionalityTested != null) {
                //functionalityTested.setFunctionalityTestedId(foundFunctionalityTested.getId());
                functionalityTested.setFunctionalityTested(foundFunctionalityTested);
            }
        }
    }

    private FunctionalityTested getFunctionalityTested(String functionalityTestedNumber, Long criterionId) {
        Map<Long, List<FunctionalityTested>> funcTestedMappings = functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps();
        if (!funcTestedMappings.containsKey(criterionId)) {
            return null;
        }
        List<FunctionalityTested> functionalityTestedForCriterion = funcTestedMappings.get(criterionId);
        Optional<FunctionalityTested> funcTestedOpt = functionalityTestedForCriterion.stream()
            .filter(funcTested -> funcTested.getRegulatoryTextCitation().equalsIgnoreCase(functionalityTestedNumber))
            .findAny();
        return funcTestedOpt.isPresent() ? funcTestedOpt.get() : null;
    }

    private void removeRestrictedFunctionalitiesTestedBasedOnUserRule(CertificationResult certResult) {
        Iterator<CertificationResultFunctionalityTested> functionalityTestedIter = certResult.getFunctionalitiesTested().listIterator();
        while (functionalityTestedIter.hasNext()) {
            CertificationResultFunctionalityTested currFunctionalityTested = functionalityTestedIter.next();
            if (currFunctionalityTested.getFunctionalityTested().getId() != null) {
                Optional<RestrictedFunctionalityTested> restrictedFunctionalityTested
                    = findRestrictedFunctionalityTested(certResult.getCriterion().getId(), currFunctionalityTested.getFunctionalityTested().getId());
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
