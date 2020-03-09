package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("pendingDependentCriteriaReviewer")
public class DependentCriteriaReviewer implements Reviewer {

    private Environment env;
    private CertificationCriterionDAO certificationCriterionDAO;
    private ErrorMessageUtil errorMessageUtil;

    private List<RequiredCriteriaDependency> requiredCriteriaDependencies;
    private Map<Long, CertificationCriterionDTO> criteria = new HashMap<Long, CertificationCriterionDTO>();

    @Autowired
    public DependentCriteriaReviewer(Environment env, CertificationCriterionDAO certificationCriterionDAO,
            ErrorMessageUtil errorMessageUtil) {
        this.env = env;
        this.certificationCriterionDAO = certificationCriterionDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @PostConstruct
    public void postConstruct() {
        this.requiredCriteriaDependencies = getRequiredCriteriaDependencies();
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        List<PendingCertificationResultDTO> attestedToCriteria = getAttestedToCriteria(listing);

        attestedToCriteria.stream()
                .forEach(attestedToCriterion -> {
                    List<Long> requiredCriteriaForAttestedToCriterion = getRequiredCriteriaForCeritificationResult(
                            attestedToCriterion);
                    listing.getErrorMessages().addAll(
                            validateDependentCriteria(attestedToCriteria, attestedToCriterion,
                                    requiredCriteriaForAttestedToCriterion));
                });
    }

    private List<String> validateDependentCriteria(List<PendingCertificationResultDTO> attestedToCriteria,
            PendingCertificationResultDTO dependentCertificationResult, List<Long> requiredCriteria) {
        List<String> errorMessages = new ArrayList<String>();

        // Get just a list of the criterion ids, rather than the whole object...
        List<Long> attestedToCriteriaIds = attestedToCriteria.stream()
                .map(attested -> attested.getCriterion().getId())
                .collect(Collectors.toList());

        requiredCriteria.stream()
                .forEach(requiredCriteriId -> {
                    Optional<String> err = validateDependentCriterion(attestedToCriteriaIds,
                            dependentCertificationResult.getCriterion().getId(),
                            requiredCriteriId);
                    if (err.isPresent()) {
                        errorMessages.add(err.get());
                    }
                });

        return errorMessages;
    }

    private Optional<String> validateDependentCriterion(List<Long> attestedToCriteriaId,
            Long dependentCriterionId, Long requiredCriterionId) {
        if (attestedToCriteriaId.contains(requiredCriterionId)) {
            return Optional.empty();
        } else {

            return Optional
                    .of(errorMessageUtil.getMessage(
                            "listing.criteria.dependentCriteriaRequired",
                            getCertificationCriterion(dependentCriterionId).getNumber(),
                            getCertificationCriterion(requiredCriterionId).getNumber()));

        }
    }

    private List<Long> getRequiredCriteriaForCeritificationResult(PendingCertificationResultDTO pendingCertificationResultDTO) {
        return requiredCriteriaDependencies.stream()
                .filter(item -> item.getDependentCriteria().contains(pendingCertificationResultDTO.getCriterion().getId()))
                .flatMap(item -> item.getRequiredCriteria().stream())
                .collect(Collectors.toList());
    }

    private List<PendingCertificationResultDTO> getAttestedToCriteria(PendingCertifiedProductDTO listing) {
        return listing.getCertificationCriterion().stream()
                .filter(cc -> cc.getMeetsCriteria())
                .collect(Collectors.toList());
    }

    private List<RequiredCriteriaDependency> getRequiredCriteriaDependencies() {
        List<RequiredCriteriaDependency> requiredCriteriaDependencies = new ArrayList<RequiredCriteriaDependency>();
        String json = env.getProperty("requiredCriteriaDependencies");
        try {
            ObjectMapper mapper = new ObjectMapper();
            CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class,
                    RequiredCriteriaDependency.class);
            requiredCriteriaDependencies = mapper.readValue(json, javaType);
        } catch (Exception e) {
            LOGGER.error("Could not convert JSON to RequiredCriteriaDependency objects", e);
            return null;
        }
        return requiredCriteriaDependencies;
    }

    private CertificationCriterionDTO getCertificationCriterion(Long criterionId) {
        // Did we already look this one up before?
        if (criteria.containsKey(criterionId)) {
            return criteria.get(criterionId);
        } else {
            // Get the criterion from the db, put it in the map and return it.
            try {
                CertificationCriterionDTO dto = certificationCriterionDAO.getById(criterionId);
                criteria.put(criterionId, dto);
                return dto;
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve criterion with id: " + criterionId, e);
                CertificationCriterionDTO dto = new CertificationCriterionDTO();
                dto.setNumber("UNKNOWN");
                return dto;
            }
        }
    }

    @Data
    static class RequiredCriteriaDependency {
        private List<Long> requiredCriteria;
        private List<Long> dependentCriteria;
    }

}
