package gov.healthit.chpl.entity.developer;

import jakarta.persistence.AttributeConverter;

public class DeveloperStatusTypeConverter implements AttributeConverter<DeveloperStatusType, String> {

    @Override
    public String convertToDatabaseColumn(DeveloperStatusType attribute) {
        return attribute.getName();
    }

    @Override
    public DeveloperStatusType convertToEntityAttribute(String dbData) {
        return DeveloperStatusType.getValue(dbData);
    }

}
