package gov.healthit.chpl.questionableactivity.listing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import gov.healthit.chpl.util.DateUtil;

@Component
public class UpdatedCertificationStatusHistoryActivity implements ListingActivity {
    private CertificationStatusEventComparator certStatusEventComparator;

    public UpdatedCertificationStatusHistoryActivity() {
        this.certStatusEventComparator = new CertificationStatusEventComparator();
    }

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        //If this activity can be determined to be ONLY a certification status change
        //where the previous status gets added as the first one in the history then
        //this is not questionable - it is expected.
        if (isCertificationStatusChangeOnly(origListing, newListing)) {
            return null;
        }
        List<QuestionableActivityListingDTO> activities = new ArrayList<QuestionableActivityListingDTO>();

        //sort events oldest first
        List<CertificationStatusEvent> origEvents = origListing.getCertificationEvents();
        List<CertificationStatusEvent> updatedEvents = newListing.getCertificationEvents();
        Collections.sort(origEvents, certStatusEventComparator);
        Collections.sort(updatedEvents, certStatusEventComparator);

        if (!StringUtils.equalsIgnoreCase(origListing.getCurrentStatus().getStatus().getName(),
                newListing.getCurrentStatus().getStatus().getName())) {
            CertificationStatusEvent origListingCurrentStatusEvent = origListing.getCurrentStatus();
            CertificationStatusEvent newListingPrevStatusEvent = getPreviousStatus(newListing);
            if (ObjectUtils.allNotNull(origListingCurrentStatusEvent, newListingPrevStatusEvent)
                    && origListingCurrentStatusEvent.matches(newListingPrevStatusEvent)) {
                //This activity includes a "current status change" where the most recent status event
                //is different and the newListing's first historical status event is equal to the "origListings"
                //current status. We can exclude that first historical status event from the "newListing"
                //from being checked for questionable activity because it is expected (i.e. NOT questionable)
                origEvents.remove(origEvents.size() - 1);
                updatedEvents.remove(updatedEvents.size() - 1);
                updatedEvents.remove(updatedEvents.size() - 2);
            }
        } else if (origListing.getCurrentStatus().getStatus().getName().equals(CertificationStatusType.Active.getName())
                && newListing.getCurrentStatus().getStatus().getName().equals(CertificationStatusType.Active.getName())
                && !origListing.getCurrentStatus().getEventDate().equals(newListing.getCurrentStatus().getEventDate())) {
            //certification date change is handled by a different activity so we don't need to compare
            //this status event entry if that is the case
            origEvents.remove(origEvents.size() - 1);
            updatedEvents.remove(updatedEvents.size() - 1);
        }

        List<CertificationStatusEvent> removedStatusEvents = origEvents.stream()
            .filter(origEvent -> !hasMatchingCertificationStatusEvent(origEvent, updatedEvents))
            .collect(Collectors.toList());

        List<CertificationStatusEvent> addedStatusEvents = updatedEvents.stream()
                .filter(updatedEvent -> !hasMatchingCertificationStatusEvent(updatedEvent, origEvents))
                .collect(Collectors.toList());

        removedStatusEvents.stream()
            .forEach(removedStatusEvent -> activities.add(QuestionableActivityListingDTO.builder()
                    .before(removedStatusEvent.getStatus().getName()
                            + " (" + DateUtil.toLocalDate(removedStatusEvent.getEventDate()) + ")")
                    .after(null)
                    .certificationStatusChangeReason(removedStatusEvent.getReason())
                    .build()));

        addedStatusEvents.stream()
            .forEach(addedStatusEvent -> activities.add(QuestionableActivityListingDTO.builder()
                .before(null)
                .after(addedStatusEvent.getStatus().getName()
                        + " (" + DateUtil.toLocalDate(addedStatusEvent.getEventDate()) + ")")
                .certificationStatusChangeReason(addedStatusEvent.getReason())
                .build()));

        if (CollectionUtils.isEmpty(activities)) {
            return null;
        }
        return activities;
    }

    private boolean isCertificationStatusChangeOnly(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        // If the origListing has a different certification status than the newListing
        // and the newListing has one additional certification status event than the origListing
            // If the newListing "additional" certification status event is equivalent to the origListing
            // "current" certification status event
            // AND there are no other changes
        if (!StringUtils.equalsIgnoreCase(origListing.getCurrentStatus().getStatus().getName(),
                newListing.getCurrentStatus().getStatus().getName())
                && (origListing.getCertificationEvents().size() == (newListing.getCertificationEvents().size() - 1))) {
            CertificationStatusEvent origListingCurrentStatusEvent = origListing.getCurrentStatus();
            CertificationStatusEvent newListingPrevStatusEvent = getPreviousStatus(newListing);
            if (ObjectUtils.allNotNull(origListingCurrentStatusEvent, newListingPrevStatusEvent)
                    && origListingCurrentStatusEvent.matches(newListingPrevStatusEvent)
                    && allOtherStatusEventsAreEqual(origListing, newListing, newListing.getCurrentStatus())) {
                return true;
            }
        }
        return false;
    }

    private CertificationStatusEvent getPreviousStatus(CertifiedProductSearchDetails listing) {
        if (CollectionUtils.isEmpty(listing.getCertificationEvents()) || listing.getCertificationEvents().size() == 1) {
            return null;
        }
        Collections.sort(listing.getCertificationEvents(), new CertificationStatusEventComparator());
        return listing.getCertificationEvents().get(listing.getCertificationEvents().size() - 2);
    }

    private boolean allOtherStatusEventsAreEqual(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing,
            CertificationStatusEvent exceptEvent) {
        List<CertificationStatusEvent> prevEvents = origListing.getCertificationEvents();
        List<CertificationStatusEvent> currEvents = newListing.getCertificationEvents();
        Collections.sort(prevEvents, new CertificationStatusEventComparator());
        Collections.sort(currEvents, new CertificationStatusEventComparator());

        Optional<CertificationStatusEvent> mismatch1 = currEvents.stream()
            .filter(currEvent -> !currEvent.matches(exceptEvent))
            .filter(currEvent -> !hasMatchingCertificationStatusEvent(currEvent, prevEvents))
            .findAny();

        Optional<CertificationStatusEvent> mismatch2 = prevEvents.stream()
            .filter(prevEvent -> !hasMatchingCertificationStatusEvent(prevEvent, currEvents))
            .findAny();

        return mismatch1.isEmpty() && mismatch2.isEmpty();
    }

    private boolean hasMatchingCertificationStatusEvent(CertificationStatusEvent certStatusEvent, List<CertificationStatusEvent> allCertStatusEvents) {
        return allCertStatusEvents.stream()
                .filter(event -> matchesValues(event, certStatusEvent))
                .findAny().isPresent();
    }

    private boolean matchesValues(CertificationStatusEvent event1, CertificationStatusEvent event2) {
        return event1.getEventDate().equals(event2.getEventDate())
                && StringUtils.equals(event1.getReason(), event2.getReason())
                && event1.getStatus().getName().equalsIgnoreCase(event2.getStatus().getName());
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY;
    }

    static class CertificationStatusEventComparator implements Comparator<CertificationStatusEvent>, Serializable {
        private static final long serialVersionUID = 1315674742856524797L;

        @Override
        public int compare(CertificationStatusEvent a, CertificationStatusEvent b) {
            return a.getEventDate().longValue() < b.getEventDate().longValue()
                    ? -1
                            : a.getEventDate().longValue() == b.getEventDate().longValue() ? 0 : 1;
        }
    }
}
