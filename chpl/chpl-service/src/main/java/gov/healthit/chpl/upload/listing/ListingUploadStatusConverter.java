package gov.healthit.chpl.upload.listing;

import jakarta.persistence.AttributeConverter;

public class ListingUploadStatusConverter implements AttributeConverter<ListingUploadStatus, String> {

    @Override
    public String convertToDatabaseColumn(ListingUploadStatus attribute) {
        return attribute.toString();
    }

    @Override
    public ListingUploadStatus convertToEntityAttribute(String dbData) {
        return ListingUploadStatus.valueOf(dbData);
    }
}
