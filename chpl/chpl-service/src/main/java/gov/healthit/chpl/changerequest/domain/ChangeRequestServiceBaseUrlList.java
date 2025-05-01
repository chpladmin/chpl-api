package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
public class ChangeRequestServiceBaseUrlList implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = -3382178658531362574L;

    @EqualsAndHashCode.Exclude
    private Long id;

    private String serviceBaseUrlList;
    private Long listingId;

    public static ChangeRequestServiceBaseUrlList cast(Object obj) {
        if (obj instanceof ChangeRequestServiceBaseUrlList) {
            return (ChangeRequestServiceBaseUrlList) obj;
        } else {
            throw new RuntimeException("Could not cast object as type ChangeRequestServiceBaseUrlList");
        }
    }
}
