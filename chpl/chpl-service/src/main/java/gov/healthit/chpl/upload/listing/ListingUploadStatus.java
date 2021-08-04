package gov.healthit.chpl.upload.listing;

import org.springframework.util.StringUtils;

public enum ListingUploadStatus {
    PROCESSING("Processing"), SUCCESSFUL("Successful"), FAILED("Failed");

    private String name;

    ListingUploadStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (!StringUtils.isEmpty(this.getName())) {
            return this.getName();
        }
        return name();
    }

    public static ListingUploadStatus getValue(String value) {
        if (value == null) {
            return null;
        }

        ListingUploadStatus result = null;
        ListingUploadStatus[] values = ListingUploadStatus.values();
        for (int i = 0; i < values.length && result == null; i++) {
            if (value.equalsIgnoreCase(values[i].toString())) {
                result = values[i];
            }
        }
        return result;
    }
}
