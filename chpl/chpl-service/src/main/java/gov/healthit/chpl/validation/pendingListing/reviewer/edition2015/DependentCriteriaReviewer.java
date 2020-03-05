package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("pendingDependentCriteriaReviewer")
public class DependentCriteriaReviewer implements Reviewer {

    private Environment env;
    private List<RequiredCriteriaDependency> requiredCriteriaDependencies;

    @Autowired
    public DependentCriteriaReviewer(Environment env) {
        this.env = env;
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
                    Optional<String> err = validateDependentCriterion(attestedToCriteriaIds, dependentCertificationResult,
                            requiredCriteriId);
                    if (err.isPresent()) {
                        errorMessages.add(err.get());
                    }
                });

        return errorMessages;
    }

    private Optional<String> validateDependentCriterion(List<Long> attestedToCriteriaId,
            PendingCertificationResultDTO dependentCertificationResult, Long requiredCriterionId) {
        if (attestedToCriteriaId.contains(requiredCriterionId)) {
            return Optional.empty();
        } else {
            return Optional
                    .of("This is the temporary error message - " + dependentCertificationResult.getCriterion().getNumber());

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

    @Data
    static class RequiredCriteriaDependency {
        private List<Long> requiredCriteria;
        private List<Long> dependentCriteria;
    }

}
