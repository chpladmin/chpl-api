package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AddedRemovedSurveillanceNonconformityActivity extends ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<QuestionableActivityListingDTO> questionableActivityListingDTOs = new ArrayList<QuestionableActivityListingDTO>();

        List<SurveillanceNonconformity> origNonconformities = origListing.getSurveillance().stream()
                .flatMap(surv -> surv.getRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .map(nc -> nc)
                .collect(Collectors.toList());

        List<SurveillanceNonconformity> newNonconformities = newListing.getSurveillance().stream()
                .flatMap(surv -> surv.getRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .map(nc -> nc)
                .collect(Collectors.toList());

        subtractNonConformityLists(newNonconformities, origNonconformities).stream()
                .filter(nc -> NonconformityType.getByName(nc.getNonconformityType()).isPresent() ? NonconformityType.getByName(nc.getNonconformityType()).get().getRemoved() : false)
                .forEach(nc -> {
                    QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                    activity.setAfter(String.format("Non-conformity of type %s is removed and was added to surveillance.", nc.getNonconformityType()));
                    activity.setBefore(null);
                    questionableActivityListingDTOs.add(activity);
                });

        return questionableActivityListingDTOs;
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REMOVED_NONCONFORMITY_ADDED;
    }

    private List<SurveillanceNonconformity> subtractNonConformityLists(List<SurveillanceNonconformity> listA, List<SurveillanceNonconformity> listB) {
        Predicate<SurveillanceNonconformity> notInListB = ncFromA -> !listB.stream()
                .anyMatch(nc -> ncFromA.getId().equals(nc.getId()));

        return listA.stream()
                .filter(notInListB)
                .peek(nc -> LOGGER.always().log(nc.toString()))
                .collect(Collectors.toList());
    }

}
