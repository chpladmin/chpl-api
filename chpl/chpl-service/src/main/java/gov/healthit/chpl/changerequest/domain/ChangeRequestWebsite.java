package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
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


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChangeRequestWebsite other = (ChangeRequestWebsite) obj;
        return Objects.equals(id, other.id) && Objects.equals(website, other.website);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, website);
    }
}
