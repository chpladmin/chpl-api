package gov.healthit.chpl.dto;

import gov.healthit.chpl.domain.DeveloperStatusEvent;

/**
 * Maintains the original and updated DeveloperStatusEventDTO.
 * @author TYoung
 *
 */
public class DeveloperStatusEventPair {
    private DeveloperStatusEvent orig;
    private DeveloperStatusEvent updated;

    /**
     * Basic constructor that populates the object.
     * @param orig - DeveloperStatusEventDTO
     * @param updated - DeveloperStatusEventDTO
     */
    public DeveloperStatusEventPair(DeveloperStatusEvent orig,
            DeveloperStatusEvent updated) {

        this.orig = orig;
        this.updated = updated;
    }

    public DeveloperStatusEvent getOrig() {
        return orig;
    }

    public DeveloperStatusEvent getUpdated() {
        return updated;
    }
}
