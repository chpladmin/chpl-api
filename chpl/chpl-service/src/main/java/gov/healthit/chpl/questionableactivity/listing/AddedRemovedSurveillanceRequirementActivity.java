package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementOptions;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.manager.DimensionalDataManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AddedRemovedSurveillanceRequirementActivity implements ListingActivity {
    private SurveillanceRequirementOptions surveillanceRequirementOptions;

    @Autowired
    public AddedRemovedSurveillanceRequirementActivity(DimensionalDataManager dimensionalDataManager) {
        this.surveillanceRequirementOptions = dimensionalDataManager.getSurveillanceRequirementOptions();
    }

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<QuestionableActivityListingDTO> questionableActivityListingDTOs = new ArrayList<QuestionableActivityListingDTO>();

        List<SurveillanceRequirement> origRequirements = origListing.getSurveillance().stream()
                .flatMap(surv -> surv.getRequirements().stream())
                .map(req -> req)
                .collect(Collectors.toList());

        List<SurveillanceRequirement> newRequirements = newListing.getSurveillance().stream()
                .flatMap(surv -> surv.getRequirements().stream())
                .map(req -> req)
                .collect(Collectors.toList());

        subtractRequirementLists(newRequirements, origRequirements).stream()
                .filter(req -> isSurveillanceRequirementRemoved(req))
                .forEach(req -> {
                    QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                    activity.setAfter(String.format("Surveillance Requirement of type %s is removed and was added to surveillance.", req.getRequirement()));
                    activity.setBefore(null);
                    questionableActivityListingDTOs.add(activity);
                });

        return questionableActivityListingDTOs;

    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REMOVED_REQUIREMENT_ADDED;
    }

    private Boolean isSurveillanceRequirementRemoved(SurveillanceRequirement requirement) {
        return surveillanceRequirementOptions.getTransparencyOptions().stream()
                .filter(req -> req.getItem().equals(requirement.getRequirement()) && req.getRemoved())
                .findAny()
                .isPresent();
    }

    private List<SurveillanceRequirement> subtractRequirementLists(List<SurveillanceRequirement> listA, List<SurveillanceRequirement> listB) {
        Predicate<SurveillanceRequirement> notInListB = reqFromA -> !listB.stream()
                .anyMatch(req -> reqFromA.getId().equals(req.getId()));

        return listA.stream()
                .filter(notInListB)
                .peek(nc -> LOGGER.always().log(nc.toString()))
                .collect(Collectors.toList());
    }
}
