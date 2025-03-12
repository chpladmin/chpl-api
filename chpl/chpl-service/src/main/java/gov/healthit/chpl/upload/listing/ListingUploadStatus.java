package gov.healthit.chpl.upload.listing;

import java.util.List;

public enum ListingUploadStatus {
    UPLOAD_PROCESSING,
    UPLOAD_SUCCESS,
    UPLOAD_FAILURE,
    CONFIRMATION_PROCESSING,
    CONFIRMED,
    REJECTED;

    public static List<ListingUploadStatus> getFinalStatuses() {
        return List.of(CONFIRMED, REJECTED);
    }

    public boolean isFinalStatus() {
        return !getFinalStatuses().contains(this);
    }
}
