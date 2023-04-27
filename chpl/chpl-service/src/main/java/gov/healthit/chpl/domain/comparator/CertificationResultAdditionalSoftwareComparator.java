package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultAdditionalSoftwareComparator implements Comparator<CertificationResultAdditionalSoftware> {

    @Override
    public int compare(CertificationResultAdditionalSoftware as1, CertificationResultAdditionalSoftware as2) {
        if (as1.getId() != null && as2.getId() != null) {
            return as1.getId().compareTo(as2.getId());
        }
        return 0;
    }
}
