package gov.healthit.chpl.questionableactivity.listing;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AddedRemovedSurveillanceRequirementActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<SurveillanceRequirement> origRequirements = origListing.getSurveillance().stream()
                .flatMap(surv -> surv.getRequirements().stream())
                .map(req -> req)
                .collect(Collectors.toList());

        List<SurveillanceRequirement> newRequirements = newListing.getSurveillance().stream()
                .flatMap(surv -> surv.getRequirements().stream())
                .map(req -> req)
                .collect(Collectors.toList());

        return ListUtils.union(
                checkForRemovedSurveillanceRequirementTypeAdded(origRequirements, newRequirements),
                checkForSurveillanceRequirementsUpdatedwithRemoved(origRequirements, newRequirements));
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REMOVED_REQUIREMENT_ADDED;
    }

    private List<QuestionableActivityListingDTO> checkForRemovedSurveillanceRequirementTypeAdded(
            List<SurveillanceRequirement> origRequirements, List<SurveillanceRequirement> newRequirements) {
        return subtractRequirementLists(newRequirements, origRequirements).stream()
                .filter(req -> isRequirementTypeRemoved(req))
                .map(req -> QuestionableActivityListingDTO.builder()
                        .after(String.format("Surveillance Requirement of type %s is removed and was added to surveillance.",
                                req.getRequirementType().getFormattedTitle()))
                        .build())
                .collect(Collectors.toList());
    }


    private List<QuestionableActivityListingDTO> checkForSurveillanceRequirementsUpdatedwithRemoved(
            List<SurveillanceRequirement> origRequirements, List<SurveillanceRequirement> newRequirements) {
        return origRequirements.stream()
                .filter(req -> hasSurveillanceRequirementBeenUpdatedToRemovedRequirementType(req, newRequirements))
                .map(req -> QuestionableActivityListingDTO.builder()
                        .after(String.format("Surveillance Requirement of type %s is removed and was added to surveillance.",
                                getMatchingSurveillanceRequirement(req, newRequirements).get().getRequirementType().getFormattedTitle()))
                        .build())
                .collect(Collectors.toList());
    }

    private Boolean hasSurveillanceRequirementBeenUpdatedToRemovedRequirementType(SurveillanceRequirement origRequirement, List<SurveillanceRequirement> newRequirements) {
        Optional<SurveillanceRequirement> updatedRequirement = getMatchingSurveillanceRequirement(origRequirement, newRequirements);
        if (updatedRequirement.isPresent() && updatedRequirement.get().getRequirementType() != null) {
            return !updatedRequirement.get().getRequirementType().getId().equals(origRequirement.getRequirementType().getId())
                    && isRequirementTypeRemoved(updatedRequirement.get());
        }
        return false;
    }

    private Optional<SurveillanceRequirement> getMatchingSurveillanceRequirement(SurveillanceRequirement requirement, List<SurveillanceRequirement> requirements) {
        return requirements.stream()
                .filter(req -> req.getId().equals(requirement.getId()))
                .findAny();
    }

    private Boolean isRequirementTypeRemoved(SurveillanceRequirement requirement) {
        if (requirement.getRequirementType() != null) {
            return requirement.getRequirementType().getRemoved();
        } else {
            return false;
        }
    }

    private List<SurveillanceRequirement> subtractRequirementLists(List<SurveillanceRequirement> listA, List<SurveillanceRequirement> listB) {
        Predicate<SurveillanceRequirement> notInListB = reqFromA -> !listB.stream()
                .anyMatch(req -> reqFromA.getId().equals(req.getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }
}
