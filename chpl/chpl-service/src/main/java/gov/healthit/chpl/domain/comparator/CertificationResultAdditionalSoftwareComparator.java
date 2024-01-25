package gov.healthit.chpl.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultAdditionalSoftwareComparator implements Serializable,
    Comparator<CertificationResultAdditionalSoftware> {

    private static final long serialVersionUID = -7080055928252322632L;

    @Override
    public int compare(CertificationResultAdditionalSoftware as1, CertificationResultAdditionalSoftware as2) {
        if (as1.getId() != null && as2.getId() != null) {
            return as1.getId().compareTo(as2.getId());
        }
        return 0;
    }
}
