package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertifiedProduct;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertifiedProductComparator implements Comparator<CertifiedProduct> {
    @Override
    public int compare(CertifiedProduct cp1, CertifiedProduct cp2) {
        if (!StringUtils.isEmpty(cp1.getChplProductNumber()) && !StringUtils.isEmpty(cp2.getChplProductNumber())) {
            return cp1.getChplProductNumber().compareTo(cp2.getChplProductNumber());
        } else if (cp1.getId() != null && cp2.getId() != null) {
            return cp1.getId().compareTo(cp2.getId());
        }
        return 0;
    }
}
