package gov.healthit.chpl.scheduler.job.subscriptions.subjects.formatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.certifiedproduct.service.CertificationStatusEventsService;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class CertificationStatusChangedFormatter extends ObservationSubjectFormatter {
    private static final String DESCRIPTION_UNFORMATTED = "Certification status changed from '%s' on %s (with reason '%s') "
            + "to '%s' on %s (with reason '%s')";
    private static final int DAYS_IN_WEEK = 7;

    private CertificationStatusEventsService certStatusEventService;

    @Autowired
    public CertificationStatusChangedFormatter(@Qualifier("activityDAO") ActivityDAO activityDao,
            ListingActivityUtil listingActivityUtil,
            CertificationStatusEventsService certStatusEventService) {
        super(activityDao, listingActivityUtil);
        this.certStatusEventService = certStatusEventService;
    }

    @Override
    public List<List<String>> toListsOfStrings(SubscriptionObservation observation) {
        ActivityDTO activity = getActivity(observation.getActivityId());

        CertifiedProductSearchDetails before = getListing(activity.getOriginalData());
        CertifiedProductSearchDetails after = getListing(activity.getNewData());

        if (before == null || after == null) {
            LOGGER.error("There was a problem turning activityID " + activity.getId() + " into listing details objects.");
            return null;
        }

        LocalDate today = LocalDate.now();
        List<List<String>> formattedObservations = new ArrayList<List<String>>();
        List<CertificationStatusEvent> addedStatusEvents = certStatusEventService.getAddedCertificationStatusEvents(before, after);

        //Include a status event added with status change of today or the past 7 days
        //Note: If we tried to use this for a "daily" notification we would need to do something
        //different here. Looking at status changes over the past 7 days takes advantage of
        //the notifications going out weekly. And even then, if someone future-dated two certification
        //status events that occurred within the same week a subscriber might get notified of one
        //of those status changes twice. That seems like something not horrible and an extreme edge-case
        //so I'm living with it.
        addedStatusEvents.stream()
            .filter(addedStatusEvent -> addedStatusEvent.getEventDay().isEqual(today)
                    || (addedStatusEvent.getEventDay().isAfter(today.minusDays(DAYS_IN_WEEK))
                            && addedStatusEvent.getEventDay().isBefore(today)))
            .forEach(addedStatusEvent -> {
                //get the status of the listing on the day prior to this status event
                CertificationStatusEvent previousStatusEvent = after.getStatusOnDate(DateUtil.toDate(addedStatusEvent.getEventDay().minusDays(1)));

                formattedObservations.add(Stream.of(observation.getSubscription().getSubject().getSubject(),
                    String.format(DESCRIPTION_UNFORMATTED, previousStatusEvent.getStatus().getName(),
                            previousStatusEvent.getEventDay(),
                            previousStatusEvent.getReason(),
                            addedStatusEvent.getStatus().getName(),
                            addedStatusEvent.getEventDay(),
                            addedStatusEvent.getReason()),
                    DateUtil.formatInEasternTime(activity.getActivityDate())).toList());
            });

        return formattedObservations;
    }
}
