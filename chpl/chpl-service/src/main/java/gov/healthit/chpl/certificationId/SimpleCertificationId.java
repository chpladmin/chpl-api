package gov.healthit.chpl.certificationId;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SimpleCertificationId implements Serializable {
    private static final long serialVersionUID = 2521257609141032011L;
    private String certificationId;
    private Date created;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SimpleCertificationId)) {
            return false;
        }

        SimpleCertificationId anotherId = (SimpleCertificationId) obj;
        if ((this.certificationId == null && anotherId.certificationId != null)
                || (this.certificationId != null && anotherId.certificationId == null)) {
            return false;
        }
        if ((this.created == null && anotherId.created != null)
                || (this.created != null && anotherId.created == null)) {
            return false;
        }
        return this.certificationId.equals(anotherId.certificationId) && this.created.equals(anotherId.created);
    }

    @Override
    public int hashCode() {
        return this.certificationId.hashCode() + this.created.hashCode();
    }
}
