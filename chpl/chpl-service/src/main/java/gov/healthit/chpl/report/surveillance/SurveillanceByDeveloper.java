package gov.healthit.chpl.report.surveillance;

import java.time.LocalDate;

import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@Builder
public class SurveillanceByDeveloper {
    private Long developerId;
    private String developerName;
    private String developerDetailsUrl;
    private boolean developerHasActiveListings;
    private Long listingId;
    private String chplProductNumber;
    private String listingDetailsUrl;

    private Long surveillanceId;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate surveillanceStartDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate surveillanceEndDate;
}
