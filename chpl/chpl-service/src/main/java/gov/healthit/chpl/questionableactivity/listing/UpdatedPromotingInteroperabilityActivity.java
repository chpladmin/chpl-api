package gov.healthit.chpl.questionableactivity.listing;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListing;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class UpdatedPromotingInteroperabilityActivity implements ListingActivity {
    private ResourcePermissions resourcePermissions;

    @Autowired
    public UpdatedPromotingInteroperabilityActivity(ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        if (!resourcePermissions.doesAuditUserHaveRole(Authority.ROLE_ACB)
                || (CollectionUtils.isEmpty(origListing.getPromotingInteroperabilityUserHistory())
                && CollectionUtils.isEmpty(newListing.getPromotingInteroperabilityUserHistory()))) {
            return null;
        }

        String piUpdatedMessage = "Promoting Interoperability history was updated.";
        QuestionableActivityListing activity = null;
        if (CollectionUtils.isEmpty(origListing.getPromotingInteroperabilityUserHistory())
                && CollectionUtils.isNotEmpty(newListing.getPromotingInteroperabilityUserHistory())) {
            activity = new QuestionableActivityListing();
            activity.setAfter(piUpdatedMessage);
        } else if (CollectionUtils.isNotEmpty(origListing.getPromotingInteroperabilityUserHistory())
                && CollectionUtils.isEmpty(newListing.getPromotingInteroperabilityUserHistory())) {
            activity = new QuestionableActivityListing();
            activity.setAfter(piUpdatedMessage);
        } else if (hasPromotingInteroperabilityHistoryChanged(origListing, newListing)) {
            activity = new QuestionableActivityListing();
            activity.setAfter(piUpdatedMessage);
        }
        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.PROMOTING_INTEROPERABILITY_UPDATED_BY_ACB;
    }

    private boolean hasPromotingInteroperabilityHistoryChanged(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        return subtractLists(existingListing.getPromotingInteroperabilityUserHistory(), updatedListing.getPromotingInteroperabilityUserHistory()).size() > 0
                || subtractLists(updatedListing.getPromotingInteroperabilityUserHistory(), existingListing.getPromotingInteroperabilityUserHistory()).size() > 0;
    }

    private List<PromotingInteroperabilityUser> subtractLists(List<PromotingInteroperabilityUser> listA, List<PromotingInteroperabilityUser> listB) {
        Predicate<PromotingInteroperabilityUser> notInListB = piFromA -> !listB.stream()
                .anyMatch(pi -> doPromotingInteroperabilityValuesMatch(piFromA, pi));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private boolean doPromotingInteroperabilityValuesMatch(PromotingInteroperabilityUser a, PromotingInteroperabilityUser b) {
        return a.getUserCount().equals(b.getUserCount())
                && a.getUserCountDate().isEqual(b.getUserCountDate());
    }

}
