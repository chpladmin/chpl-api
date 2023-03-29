package gov.healthit.chpl.certifiedproduct.service.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertifiedProductAccessibilityStandardComparator implements Comparator<CertifiedProductAccessibilityStandard> {
    @Override
    public int compare(CertifiedProductAccessibilityStandard as1, CertifiedProductAccessibilityStandard as2) {
        if (!StringUtils.isEmpty(as1.getAccessibilityStandardName()) && !StringUtils.isEmpty(as2.getAccessibilityStandardName())) {
            return as1.getAccessibilityStandardName().compareTo(as2.getAccessibilityStandardName());
        } else if (as1.getAccessibilityStandardId() != null && as2.getAccessibilityStandardId() != null) {
            return as1.getAccessibilityStandardId().compareTo(as2.getAccessibilityStandardId());
        }
        return 0;
    }
}
