package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeRequestListingUrl implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = -3382178658531362574L;

    @EqualsAndHashCode.Exclude
    private Long id;

    private ChangeRequestListingUrlType changeRequestListingUrlType;
    private String url;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate checkDate;
    private CertifiedProductSearchDetails listing;

    public static ChangeRequestListingUrl cast(Object obj) {
        if (obj instanceof ChangeRequestListingUrl) {
            return (ChangeRequestListingUrl) obj;
        } else {
            throw new RuntimeException("Could not cast object as type ChangeRequestListingUrl");
        }
    }
}
