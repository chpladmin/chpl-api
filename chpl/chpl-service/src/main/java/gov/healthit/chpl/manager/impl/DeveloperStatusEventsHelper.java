package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventPair;

public class DeveloperStatusEventsHelper {
    public static List<DeveloperStatusEventDTO> getAddedEvents(List<DeveloperStatusEventDTO> originalEvents, List<DeveloperStatusEventDTO> updatedEvents) {
        List<DeveloperStatusEventDTO> originalStatuses =
                cloneDeveloperStatusEventList(originalEvents);
        List<DeveloperStatusEventDTO> updatedStatuses =
                cloneDeveloperStatusEventList(updatedEvents);

        updatedStatuses.removeAll(originalStatuses);
        return updatedStatuses;
    }
    
    public static List<DeveloperStatusEventDTO> getRemovedEvents(List<DeveloperStatusEventDTO> originalEvents, List<DeveloperStatusEventDTO> updatedEvents) {
        List<DeveloperStatusEventDTO> originalStatuses =
                cloneDeveloperStatusEventList(originalEvents);
        List<DeveloperStatusEventDTO> updatedStatuses =
                cloneDeveloperStatusEventList(updatedEvents);

        originalStatuses.removeAll(updatedStatuses);
        return originalStatuses;
    }
    
    public static List<DeveloperStatusEventPair> getUpdatedEvents(List<DeveloperStatusEventDTO> originalEvents, List<DeveloperStatusEventDTO> updatedEvents) {
        List<DeveloperStatusEventPair> statuses = new ArrayList<DeveloperStatusEventPair>();
        
        for (DeveloperStatusEventDTO updatedStatusEvent : updatedEvents) {
            for (DeveloperStatusEventDTO existingStatusEvent : originalEvents) {
                if (updatedStatusEvent.equals(existingStatusEvent)) {
                    if (!nullSafeEquals(updatedStatusEvent.getReason(), existingStatusEvent.getReason())
                            || !nullSafeDateEquals(updatedStatusEvent.getStatusDate(), existingStatusEvent.getStatusDate())
                            || !nullSafeEquals(updatedStatusEvent.getStatus().getStatusName(),
                                    existingStatusEvent.getStatus().getStatusName())) {

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
            if (cal1.get(Calendar.MONTH) == cal1.get(Calendar.MONTH)
                    && cal1.get(Calendar.YEAR) == cal1.get(Calendar.YEAR)
                    && cal1.get(Calendar.DATE) == cal1.get(Calendar.DATE)) {
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
    
        
    
    
    
    private static List<DeveloperStatusEventDTO> cloneDeveloperStatusEventList(List<DeveloperStatusEventDTO> original) {
        List<DeveloperStatusEventDTO> clone = new ArrayList<DeveloperStatusEventDTO>();
        if (original == null) {
            return clone; 
        } else { 
            for (DeveloperStatusEventDTO event : original) {
                clone.add(new DeveloperStatusEventDTO(event));
            }
            return clone;
        }
    }
}
