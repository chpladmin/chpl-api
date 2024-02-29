package gov.healthit.chpl.questionableactivity.listing;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.util.Util;

@Component
public class FutureCertificationStatusAddedActivity implements ListingActivity {
    private static final int DAYS_IN_FUTURE = 30;

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListing activity = null;
        List<CertificationStatusEvent> addedFutureCertificationStatusEvents =
                getAddedFutureCertificationStatusEvents(origListing, newListing);
        if (!CollectionUtils.isEmpty(addedFutureCertificationStatusEvents)) {
            activity = new QuestionableActivityListing();
            activity.setBefore(null);
            activity.setAfter(formatCertificationStatusEvents(addedFutureCertificationStatusEvents));
            activity.setCertificationStatusChangeReason(formatCertificationStatusReasons(addedFutureCertificationStatusEvents));
        }

        return Arrays.asList(activity);
    }

    private String formatCertificationStatusEvents(List<CertificationStatusEvent> events) {
        return Util.joinListGrammatically(events.stream()
                        .map(event -> event.getStatus().getName() + ": " + event.getEventDay())
                        .toList());
    }

    private String formatCertificationStatusReasons(List<CertificationStatusEvent> events) {
        return Util.joinListGrammatically(events.stream()
                        .map(event -> event.getReason() + ": " + event.getEventDay())
                        .toList());
    }

    private List<CertificationStatusEvent> getAddedFutureCertificationStatusEvents(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        return subtractLists(updatedListing.getCertificationEvents(), existingListing.getCertificationEvents()).stream()
                .filter(event -> event.getEventDay().isAfter(LocalDate.now().plusDays(DAYS_IN_FUTURE)))
                .collect(Collectors.toList());
    }

    private List<CertificationStatusEvent> subtractLists(List<CertificationStatusEvent> listA, List<CertificationStatusEvent> listB) {
        Predicate<CertificationStatusEvent> notInListB = eventFromA -> !listB.stream()
                .anyMatch(event -> doValuesMatch(eventFromA, event));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private boolean doValuesMatch(CertificationStatusEvent event1, CertificationStatusEvent event2) {
        return event1.getStatus().getName().equals(event2.getStatus().getName())
                && event1.getEventDay().equals(event2.getEventDay())
                && StringUtils.equalsIgnoreCase(event1.getReason(), event2.getReason());
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.FUTURE_CERTIFICATION_STATUS;
    }
}
