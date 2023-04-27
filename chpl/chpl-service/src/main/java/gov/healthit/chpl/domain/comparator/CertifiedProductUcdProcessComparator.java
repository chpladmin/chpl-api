package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertifiedProductUcdProcessComparator implements Comparator<CertifiedProductUcdProcess> {
    @Override
    public int compare(CertifiedProductUcdProcess ucd1, CertifiedProductUcdProcess ucd2) {
        if (!StringUtils.isEmpty(ucd1.getName()) && !StringUtils.isEmpty(ucd2.getName())) {
            return ucd1.getName().compareTo(ucd2.getName());
        } else if (ucd1.getId() != null && ucd2.getId() != null) {
            return ucd1.getId().compareTo(ucd2.getId());
        }
        return 0;
    }
}
