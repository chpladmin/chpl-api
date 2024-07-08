package gov.healthit.chpl.dto;

import gov.healthit.chpl.domain.DeveloperStatusEventDeprecated;

/**
 * Maintains the original and updated DeveloperStatusEventDTO.
 * @author TYoung
 *
 */
public class DeveloperStatusEventPair {
    private DeveloperStatusEventDeprecated orig;
    private DeveloperStatusEventDeprecated updated;

    /**
     * Basic constructor that populates the object.
     * @param orig - DeveloperStatusEventDTO
     * @param updated - DeveloperStatusEventDTO
     */
    public DeveloperStatusEventPair(DeveloperStatusEventDeprecated orig,
            DeveloperStatusEventDeprecated updated) {

        this.orig = orig;
        this.updated = updated;
    }

    public DeveloperStatusEventDeprecated getOrig() {
        return orig;
    }

    public DeveloperStatusEventDeprecated getUpdated() {
        return updated;
    }
}
