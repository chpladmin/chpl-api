package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.contact.PointOfContact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public @Data class ChangeRequestDeveloperDetails implements Serializable {
    private static final long serialVersionUID = -5572794875421124955L;

    private Long id;
    private Boolean selfDeveloper;
    private Address address;
    private PointOfContact contact;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChangeRequestDeveloperDetails)) {
            return false;
        }
        ChangeRequestDeveloperDetails anotherCr = (ChangeRequestDeveloperDetails) obj;
        if ((this.id != null && anotherCr.id != null
                && this.id.longValue() == anotherCr.id.longValue())
                || (this.id == null && anotherCr.id == null)) {
            return ObjectUtils.equals(this.selfDeveloper, anotherCr.selfDeveloper)
                    && ((this.address == null && anotherCr.address == null)
                            || this.address.equals(anotherCr.address))
                    && ((this.contact == null && anotherCr.contact == null)
                            || this.contact.equals(anotherCr.contact));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (this.id != null) {
            hashCode += this.id.hashCode();
        }
        if (this.selfDeveloper != null) {
            hashCode += this.selfDeveloper.hashCode();
        }
        if (this.address != null) {
            hashCode += this.address.hashCode();
        }
        if (this.contact != null) {
            hashCode += this.contact.hashCode();
        }
        return hashCode;
    }
}
