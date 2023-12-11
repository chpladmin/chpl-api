package gov.healthit.chpl.domain.developer.hierarchy;

import java.util.Objects;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProduct;
import lombok.Data;

@Data
public class SimpleListing extends CertifiedProduct {
    private static final long serialVersionUID = 6827193932953676099L;

    private CertificationBody acb;
    private Integer surveillanceCount;
    private Integer openSurveillanceCount;
    private Integer closedSurveillanceCount;
    private Integer openSurveillanceNonConformityCount;
    private Integer closedSurveillanceNonConformityCount;

    public SimpleListing() {
        super();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SimpleListing)) {
            return false;
        }
        SimpleListing otherListing = (SimpleListing) obj;
        return Objects.equals(this.getId(), otherListing.getId());
    }

    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }

        return this.getId().hashCode();
    }
}
