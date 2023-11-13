package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertifiedProductTestingLabComparator implements Comparator<CertifiedProductTestingLab> {

    @Override
    public int compare(CertifiedProductTestingLab atl1, CertifiedProductTestingLab atl2) {
        if (!StringUtils.isEmpty(atl1.getTestingLab().getName()) && !StringUtils.isEmpty(atl2.getTestingLab().getName())) {
            return atl1.getTestingLab().getName().compareTo(atl2.getTestingLab().getName());
        } else if (atl1.getTestingLab().getId() != null && atl2.getTestingLab().getId() != null) {
            return atl1.getTestingLab().getId().compareTo(atl2.getTestingLab().getId());
        }
        return 0;
    }
}
