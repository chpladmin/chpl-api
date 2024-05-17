package gov.healthit.chpl.manager.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;

public final class DeveloperStatusEventsHelper {

    private DeveloperStatusEventsHelper() {}

    public static List<DeveloperStatusEvent> getAddedEvents(List<DeveloperStatusEvent> originalEvents,
            final List<DeveloperStatusEvent> updatedEvents) {
        List<DeveloperStatusEvent> originalStatuses =
                cloneDeveloperStatusEventList(originalEvents);
        List<DeveloperStatusEvent> updatedStatuses =
                cloneDeveloperStatusEventList(updatedEvents);

        updatedStatuses.removeAll(originalStatuses);
        return updatedStatuses;
    }

    public static List<DeveloperStatusEvent> getRemovedEvents(List<DeveloperStatusEvent> originalEvents,
            final List<DeveloperStatusEvent> updatedEvents) {
        List<DeveloperStatusEvent> originalStatuses =
                cloneDeveloperStatusEventList(originalEvents);
        List<DeveloperStatusEvent> updatedStatuses =
                cloneDeveloperStatusEventList(updatedEvents);

        originalStatuses.removeAll(updatedStatuses);
        return originalStatuses;
    }

    public static List<DeveloperStatusEvent> cloneDeveloperStatusEventList(List<DeveloperStatusEvent> originals) {
        List<DeveloperStatusEvent> clone = new ArrayList<DeveloperStatusEvent>();
        if (originals == null) {
            return clone;
        } else {
            for (DeveloperStatusEvent original : originals) {
                clone.add(DeveloperStatusEvent.builder()
                        .id(original.getId())
                        .reason(original.getReason())
                        .status(DeveloperStatus.builder()
                                .id(original.getStatus().getId())
                                .name(original.getStatus().getName())
                                .build())
                        .startDay(LocalDate.from(original.getStartDay()))
                        .endDay(original.getEndDay() == null ? null : LocalDate.from(original.getEndDay()))
                        .build());
            }
            return clone;
        }
    }
}
