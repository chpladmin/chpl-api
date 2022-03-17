package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.dto.DeveloperStatusEventPair;

/**
 * Providers helper methods to determine added, updated, removed events between 2 lists of DeveloperStatusEventDTOs.
 * @author TYoung
 *
 */
public final class DeveloperStatusEventsHelper {

    private DeveloperStatusEventsHelper() {}

    /**
     * Determine if an event was added.
     * @param originalEvents - List<DeveloperStatusEvent>
     * @param updatedEvents - List<DeveloperStatusEvent>
     * @return List<DeveloperStatusEvent>
     */
    public static List<DeveloperStatusEvent> getAddedEvents(final List<DeveloperStatusEvent> originalEvents,
            final List<DeveloperStatusEvent> updatedEvents) {
        List<DeveloperStatusEvent> originalStatuses =
                cloneDeveloperStatusEventList(originalEvents);
        List<DeveloperStatusEvent> updatedStatuses =
                cloneDeveloperStatusEventList(updatedEvents);

        updatedStatuses.removeAll(originalStatuses);
        return updatedStatuses;
    }

    /**
     * Determine if an event was removed.
     * @param originalEvents - List<DeveloperStatusEvent>
     * @param updatedEvents - List<DeveloperStatusEvent>
     * @return List<DeveloperStatusEvent>
     */
    public static List<DeveloperStatusEvent> getRemovedEvents(final List<DeveloperStatusEvent> originalEvents,
            final List<DeveloperStatusEvent> updatedEvents) {
        List<DeveloperStatusEvent> originalStatuses =
                cloneDeveloperStatusEventList(originalEvents);
        List<DeveloperStatusEvent> updatedStatuses =
                cloneDeveloperStatusEventList(updatedEvents);

        originalStatuses.removeAll(updatedStatuses);
        return originalStatuses;
    }

    /**
     * Determine if an event was updated.
     * @param originalEvents - List<DeveloperStatusEvent>
     * @param updatedEvents - List<DeveloperStatusEvent>
     * @return List<DeveloperStatusEvent>
     */
    public static List<DeveloperStatusEventPair> getUpdatedEvents(final List<DeveloperStatusEvent> originalEvents,
            final List<DeveloperStatusEvent> updatedEvents) {
        List<DeveloperStatusEventPair> statuses = new ArrayList<DeveloperStatusEventPair>();
        for (DeveloperStatusEvent updatedStatusEvent : updatedEvents) {
            for (DeveloperStatusEvent existingStatusEvent : originalEvents) {
                if (updatedStatusEvent.equals(existingStatusEvent)) {
                    if (!nullSafeEquals(updatedStatusEvent.getReason(), existingStatusEvent.getReason())
                            || !nullSafeDateEquals(
                                    updatedStatusEvent.getStatusDate(), existingStatusEvent.getStatusDate())
                            || !nullSafeEquals(updatedStatusEvent.getStatus().getStatus(),
                                    existingStatusEvent.getStatus().getStatus())) {

                        statuses.add(new DeveloperStatusEventPair(existingStatusEvent, updatedStatusEvent));
                    }
                }
            }
        }

        return statuses;
    }

    private static boolean nullSafeDateEquals(final Date obj1, final Date obj2) {
        if (obj1 == null || obj2 == null) {
            return true;
        } else {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(obj1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(obj2);
            if (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                    && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                    && cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private static boolean nullSafeEquals(final Object obj1, final Object obj2) {
        if (obj1 == null || obj2 == null) {
            return true;
        } else {
            return obj1.equals(obj2);
        }
    }

    public static List<DeveloperStatusEvent> cloneDeveloperStatusEventList(
            List<DeveloperStatusEvent> originals) {
        List<DeveloperStatusEvent> clone = new ArrayList<DeveloperStatusEvent>();
        if (originals == null) {
            return clone;
        } else {
            for (DeveloperStatusEvent original : originals) {
                clone.add(DeveloperStatusEvent.builder()
                        .developerId(original.getDeveloperId())
                        .id(original.getId())
                        .reason(original.getReason())
                        .status(DeveloperStatus.builder()
                                .id(original.getStatus().getId())
                                .status(original.getStatus().getStatus())
                                .build())
                        .statusDate(original.getStatusDate())
                        .build());
            }
            return clone;
        }
    }
}
