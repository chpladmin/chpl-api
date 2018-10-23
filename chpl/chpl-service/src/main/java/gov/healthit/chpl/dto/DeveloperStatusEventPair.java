package gov.healthit.chpl.dto;

/**
 * Maintains the original and updated DeveloperStatusEventDTO.
 * @author TYoung
 *
 */
public class DeveloperStatusEventPair {
    private DeveloperStatusEventDTO orig;
    private DeveloperStatusEventDTO updated;

    /**
     * Basic constructor that populates the object.
     * @param orig - DeveloperStatusEventDTO
     * @param updated - DeveloperStatusEventDTO
     */
    public DeveloperStatusEventPair(final DeveloperStatusEventDTO orig,
            final DeveloperStatusEventDTO updated) {

        this.orig = orig;
        this.updated = updated;
    }

    public DeveloperStatusEventDTO getOrig() {
        return orig;
    }

    public DeveloperStatusEventDTO getUpdated() {
        return updated;
    }
}
