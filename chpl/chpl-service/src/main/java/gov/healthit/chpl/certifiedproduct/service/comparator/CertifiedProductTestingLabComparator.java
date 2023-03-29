package gov.healthit.chpl.certifiedproduct.service.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertifiedProductTestingLabComparator implements Comparator<CertifiedProductTestingLab> {

    @Override
    public int compare(CertifiedProductTestingLab atl1, CertifiedProductTestingLab atl2) {
        if (!StringUtils.isEmpty(atl1.getTestingLabName()) && !StringUtils.isEmpty(atl2.getTestingLabName())) {
            return atl1.getTestingLabName().compareTo(atl2.getTestingLabName());
        } else if (atl1.getTestingLabId() != null && atl2.getTestingLabId() != null) {
            return atl1.getTestingLabId().compareTo(atl2.getTestingLabId());
        }
        return 0;
    }
}
