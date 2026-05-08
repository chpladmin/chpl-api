package gov.healthit.chpl.report.nonconformity;

import java.time.LocalDate;

import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@Builder
public class NonconformitiesByDeveloperAndType {
    private int id;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate nonconformityOpenDay;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate nonconformityCloseDay;

    private String nonconformityTypeName;
    private String nonconformityClassification;
    private Long developerId;
    private String developerName;
    private Long listingId;
    private String chplProductNumber;
}
