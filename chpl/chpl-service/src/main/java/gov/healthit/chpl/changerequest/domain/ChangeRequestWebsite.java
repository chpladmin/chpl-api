package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

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
public class ChangeRequestWebsite implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = -5572794875424284955L;

    @EqualsAndHashCode.Exclude
    private Long id;

    private String website;


    public static ChangeRequestWebsite cast(Object obj) {
        if (obj instanceof ChangeRequestWebsite) {
            return (ChangeRequestWebsite) obj;
        } else {
            throw new RuntimeException("Could not cast object as type ChangeRequestWebsite");
        }
    }

    //TODO - need to implement equals
}
