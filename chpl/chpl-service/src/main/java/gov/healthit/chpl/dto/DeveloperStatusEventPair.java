package gov.healthit.chpl.dto;

public class DeveloperStatusEventPair {
    private DeveloperStatusEventDTO orig;
    private DeveloperStatusEventDTO updated;

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
